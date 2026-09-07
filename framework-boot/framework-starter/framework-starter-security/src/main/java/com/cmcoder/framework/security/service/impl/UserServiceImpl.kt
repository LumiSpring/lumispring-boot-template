package com.lumispring.framework.security.service.impl

import cn.hutool.crypto.digest.BCrypt
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.lumispring.framework.base.extension.checkNotNullOrEmpty
import com.lumispring.framework.base.extension.format
import com.lumispring.framework.base.extension.isNotNullOrEmpty
import com.lumispring.framework.base.extension.md5
import com.lumispring.framework.base.extension.randomUuid
import com.lumispring.framework.base.extension.throwIf
import com.lumispring.framework.base.extension.toJsonString
import com.lumispring.framework.base.extension.toObject
import com.lumispring.framework.base.extension.toTimestamp
import com.lumispring.framework.base.model.BusinessException
import com.lumispring.framework.base.model.ErrorCode
import com.lumispring.framework.security.config.SecurityProperties
import com.lumispring.framework.security.config.SecurityRedisKeyConst
import com.lumispring.framework.security.extension.currentToken
import com.lumispring.framework.security.extension.currentUser
import com.lumispring.framework.security.extension.currentUserId
import com.lumispring.framework.security.extension.isAdmin
import com.lumispring.framework.security.mapper.RoleMapper
import com.lumispring.framework.security.mapper.UserMapper
import com.lumispring.framework.security.mapper.UserRoleMapper
import com.lumispring.framework.security.model.dto.*
import com.lumispring.framework.security.model.entity.SysRole
import com.lumispring.framework.security.model.entity.SysOperationLog
import com.lumispring.framework.security.model.entity.SysUser
import com.lumispring.framework.security.model.entity.SysUserRole
import com.lumispring.framework.security.model.vo.TokenVO
import com.lumispring.framework.security.model.vo.UserVO
import com.lumispring.framework.security.service.UserService
import com.lumispring.framework.security.service.OperationLogService
import com.lumispring.framework.web.extension.reqGetHeaders
import com.lumispring.framework.web.extension.reqGetIp
import com.lumispring.framework.web.extension.reqGetReferer
import com.lumispring.framework.web.extension.reqGetUserAgent
import com.lumispring.framework.web.extension.resAddCookie
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * 用户服务实现类
 */
@Service
@Transactional(transactionManager = "securityTransactionManager")
class UserServiceImpl(
    private val userMapper: UserMapper,
    private val roleMapper: RoleMapper,
    private val userRoleMapper: UserRoleMapper,
    private val operationLogService: OperationLogService,
    @Qualifier("securityRedisTemplate")
    private val redisTemplate: RedisTemplate<String, String>,
    private val securityProperties: SecurityProperties
) : ServiceImpl<UserMapper, SysUser>(), UserService {

    private fun buildUserTokenRedisKey(token: String): String {
        return "${SecurityRedisKeyConst.USER_INFO_BY_TOKEN_PREFIX}:${token}"
    }

    private fun buildUserLoginRecordRedisKey(userId: Long, token: String): String {
        return "${SecurityRedisKeyConst.USER_LOGIN_RECORDS}:${userId}:${token}"
    }

    private fun buildUserTokensZSetRedisKey(userId: Long): String {
        return "${SecurityRedisKeyConst.USER_LOGIN_TOKENS_ZSET}:${userId}"
    }

    private fun generateToken(userVO: UserVO): String {
        while (true) {
            val token = "${userVO.toJsonString().md5()}${randomUuid()}"
            if (!redisTemplate.hasKey(buildUserTokenRedisKey(token))) return token
        }
    }

    override fun login(loginDTO: LoginDTO, expire: Long?): TokenVO {
        // 1. 查询用户
        val sysUser = userMapper.selectOne(
            KtQueryWrapper(SysUser::class.java)
                .eq(SysUser::username, loginDTO.username)
        ) ?: throw ErrorCode.USER_LOGIN_NOT_EXIST_ERROR.exception()

        // 2. 校验用户状态
        if (!sysUser.isEnabled()) {
            throw ErrorCode.USER_LOGIN_ERROR.exception("用户已被禁用")
        }

        // 3. 校验密码
        if (!checkPassword(loginDTO.password, sysUser.password!!)) {
            throw ErrorCode.USER_LOGIN_PASSWORD_ERROR.exception()
        }

        // 4. 登录并生成Token
        val userVo = convertToVO(sysUser)
        val token = generateToken(userVo)

        // 5. 将用户信息存入到Redis中
        recordUserCache(userVo, token)

        resAddCookie(token, token, securityProperties.timeout.toInt())

        // 6. 返回Token信息
        return TokenVO(
            accessToken = token,
            tokenType = "Bearer",
            expiresIn = securityProperties.timeout,
            userInfo = userVo
        )
    }

    override fun create(userDto: UserCreateDTO): UserVO {
        // 校验用户名是否已存在
        if (checkUsernameExists(userDto.username)) {
            throw ErrorCode.USER_NAME_EXIST_ERROR.exception()
        }

        if (checkNickNameExists(userDto.nickname ?: userDto.username)) {
            throw ErrorCode.USER_REGISTER_ERROR.exception("昵称已被使用")
        }

        // 校验手机号是否已存在
        userDto.phone?.let {
            if (checkPhoneExists(it)) {
                throw ErrorCode.USER_REGISTER_ERROR.exception("手机号已被注册")
            }
        }

        // 加密密码
        val encryptedPassword = BCrypt.hashpw(userDto.password)

        // 创建用户
        val sysUser = SysUser(
            username = userDto.username,
            password = encryptedPassword,
            nickname = userDto.nickname ?: userDto.username,
            email = userDto.email,
            phone = userDto.phone,
            status = SysUser.STATUS_ENABLED,
            userType = SysUser.TYPE_NORMAL
        )
        userMapper.insert(sysUser)

        // 如果用户角色不为空， 则需要分配角色
        if (userDto.roles.isNotNullOrEmpty()) {
            val roles = roleMapper.selectList(
                KtQueryWrapper(SysRole::class.java)
                    .eq(SysRole::status, SysRole.STATUS_ENABLED)
                    .`in`(SysRole::roleCode, userDto.roles)
            )

            userRoleMapper.insert(roles.map {
                SysUserRole(
                    userId = sysUser.id,
                    roleId = it.id,
                    createTime = LocalDateTime.now()
                )
            })
        }

        return convertToVO(sysUser)
    }

    override fun register(registerDTO: RegisterDTO): TokenVO {
        val userCreateDto = registerDTO.toObject<UserCreateDTO>() ?: throw ErrorCode.USER_REGISTER_ERROR.exception("注册信息转换失败")
        // 为新用户分配默认角色
        userCreateDto.roles = listOf(SysRole.ROLE_USER)
        create(userCreateDto)

        return login(LoginDTO(
            username = registerDTO.username,
            password = registerDTO.password
        ))
    }

    override fun logout() {
        if (currentUserId().isNotNullOrEmpty()) {
            val token = currentToken()!!
            redisTemplate.delete(buildUserTokenRedisKey(token))
            redisTemplate.opsForZSet().remove(buildUserTokensZSetRedisKey(currentUserId()!!), token)
            // 获取登录记录并修改登出时间
            val loginRecordKey = buildUserLoginRecordRedisKey(currentUserId()!!, token)
            val loginRecord = redisTemplate.opsForValue().get(loginRecordKey)?.toObject<LoginRecordDTO>()
            LocalDateTime.now().let {
                loginRecord?.logoutTime = it.format()
                loginRecord?.logoutTimestamp = it.toTimestamp()
            }
            loginRecord?.let {
                redisTemplate.opsForValue().set(loginRecordKey, loginRecord.toJsonString())
            }
        }
    }

    override fun logoutAll() {
        if (currentUserId().isNotNullOrEmpty()) {
            cleanUserCache(currentUserId()!!)
        }
    }

    override fun refreshToken(expire: Long?, refreshToken: String?): TokenVO {
        refreshToken.checkNotNullOrEmpty("token不能为空", ErrorCode.USER_LOGIN_ERROR)
        val userVo = redisTemplate.opsForValue().get(buildUserTokenRedisKey(refreshToken)).toObject<UserVO>()
        userVo.checkNotNullOrEmpty("用户信息不存在或已过期", ErrorCode.USER_LOGIN_ERROR)

        recordUserCache(userVo, refreshToken, expire)

        return TokenVO(
            accessToken = refreshToken,
            expiresIn = expire ?: securityProperties.timeout,
            userInfo = userVo
        )
    }

    override fun getCurrentUser(): UserVO? {
        return currentUser()
    }

    override fun getUserById(id: Long): UserVO? {
        val user = userMapper.selectById(id)
        return user?.let { convertToVO(it) }
    }

    override fun listUsersByIds(ids: List<Long>): List<UserVO> {
        return userMapper.selectList(
            KtQueryWrapper(SysUser::class.java).`in`(SysUser::id, ids)
        ).map { convertToVO(it) }
    }

    override fun getUserByUsername(username: String): SysUser? {
        return userMapper.selectOne(
            KtQueryWrapper(SysUser::class.java)
                .eq(SysUser::username, username)
        )
    }

    override fun recordUserCache(userVo: UserVO, token: String, expire: Long?) {
        val now = LocalDateTime.now()
        val expireTime = now.plusSeconds(expire ?: securityProperties.timeout)

        // 记录用户Token缓存中的用户信息
        redisTemplate.opsForValue().set(
            buildUserTokenRedisKey(token),
            userVo.toJsonString(),
            expire ?: securityProperties.timeout,
            TimeUnit.SECONDS
        )

        // 查看该Token是否已存在登录记录
        val loginRecordKey = buildUserLoginRecordRedisKey(userVo.id!!, token)
        val loginRecord = redisTemplate.opsForValue().get(loginRecordKey)?.toObject<LoginRecordDTO>()
        if (loginRecord.isNotNullOrEmpty()) {
            // 已有记录，更新一下过期时间
            loginRecord.expireTimestamp = expireTime.toTimestamp()
            loginRecord.expireTime = expireTime.format()
            redisTemplate.opsForValue().set(loginRecordKey, loginRecord.toJsonString())
        } else {
            // 没有记录，则记录用户登录Token的设备信息
            redisTemplate.opsForValue().set(
                buildUserLoginRecordRedisKey(userVo.id, token),
                LoginRecordDTO(
                    userId = userVo.id,
                    token = token,
                    ip = reqGetIp(),
                    userAgent = reqGetUserAgent(),
                    referer = reqGetReferer(),
                    loginTime = now.format(),
                    loginTimestamp = now.toTimestamp(),
                    headers = reqGetHeaders(),
                    expireTimestamp = expireTime.toTimestamp(),
                    expireTime = expireTime.format(),
                ).toJsonString()
            )
        }
        // 记录用户Token到ZSet
        redisTemplate.opsForZSet().add(buildUserTokensZSetRedisKey(userVo.id), token, expireTime.toTimestamp().toDouble())
    }

    override fun refreshUserCache(userId: Long) : UserVO {
        val userVo = getUserById(userId)
            ?: throw ErrorCode.USER_LOGIN_NOT_EXIST_ERROR.exception()

        // 获取用户所有可用Token
        val tokens = getUserAvailableTokens(userId)

        // 更新用户所有Token缓存中的用户信息
        tokens.forEach { token->
            val key = buildUserTokenRedisKey(token)
            val expire = redisTemplate.getExpire(key, TimeUnit.SECONDS)
            redisTemplate.opsForValue().set(key, userVo.toJsonString(), expire, TimeUnit.SECONDS)
        }

        return userVo
    }

    override fun cleanUserCache(userId: Long): Boolean {
        val tokens = getUserAvailableTokens(userId)
        if (tokens.isEmpty()) return true
        tokens.forEach { token->
            redisTemplate.delete(buildUserTokenRedisKey(token))
            redisTemplate.opsForZSet().remove(buildUserTokensZSetRedisKey(userId), token)
        }
        return true
    }

    override fun cleanTokenCache(token: String) {
        val userVo = redisTemplate.opsForValue().get(buildUserTokenRedisKey(token))?.toObject<UserVO>()
        userVo?.let {
            redisTemplate.opsForZSet().remove(buildUserTokensZSetRedisKey(it.id!!), token)
        }
        redisTemplate.delete(buildUserTokenRedisKey(token))
    }

    override fun updateUser(updateDTO: UserUpdateDTO): UserVO {
        throwIf(currentUserId() != updateDTO.userId && !isAdmin(), "无修改权限", errorCode = ErrorCode.AUTH_ERROR)

        val userId = updateDTO.userId ?: currentUserId()!!

        // 查询原用户信息
        val user = userMapper.selectById(userId)
            ?: throw ErrorCode.USER_LOGIN_NOT_EXIST_ERROR.exception()

        // 查询昵称是否被占用
        updateDTO.nickname?.let { newNickName ->
            if (newNickName != user.nickname) {
                if (checkNickNameExists(newNickName)) {
                    throw ErrorCode.USER_REGISTER_ERROR.exception("昵称已被使用")
                }
            }
        }

        // 校验邮箱是否已被其他用户使用
        updateDTO.email?.let { newEmail ->
            if (newEmail != user.email) {
                if (checkEmailExists(newEmail)) {
                    throw ErrorCode.USER_REGISTER_ERROR.exception("邮箱已被其他用户使用")
                }
            }
        }

        // 校验手机号是否已被其他用户使用
        updateDTO.phone?.let { newPhone ->
            if (newPhone != user.phone) {
                if (checkPhoneExists(newPhone)) {
                    throw ErrorCode.USER_REGISTER_ERROR.exception("手机号已被其他用户使用")
                }
            }
        }

        // 更新用户信息
        val updateUser = SysUser(
            id = userId,
            nickname = updateDTO.nickname ?: user.nickname,
            email = updateDTO.email ?: user.email,
            phone = updateDTO.phone ?: user.phone,
            avatar = updateDTO.avatar ?: user.avatar
        )

        userMapper.updateById(updateUser)

        // 返回更新后的用户信息
        return refreshUserCache(userId)
    }

    override fun updatePassword(passwordDTO: PasswordUpdateDTO) {
        val userId = passwordDTO.userId ?: currentUserId()!!

        // 查询用户
        val user = userMapper.selectById(userId)
            ?: throw ErrorCode.USER_LOGIN_NOT_EXIST_ERROR.exception()

        // 校验旧密码
        if (!checkPassword(passwordDTO.oldPassword, user.password!!)) {
            throw ErrorCode.USER_LOGIN_PASSWORD_ERROR.exception("旧密码不正确")
        }

        // 校验两次新密码是否一致
        if (passwordDTO.newPassword != passwordDTO.confirmPassword) {
            throw ErrorCode.USER_REGISTER_ERROR.exception("两次输入的新密码不一致")
        }

        // 加密新密码
        val encryptedPassword = encryptPassword(passwordDTO.newPassword)

        // 更新密码
        val updateUser = SysUser(
            id = userId,
            password = encryptedPassword
        )
        userMapper.updateById(updateUser)
    }

    override fun updateAvatar(userId: Long, avatarUrl: String): UserVO {
        val updateUser = SysUser(
            id = userId,
            avatar = avatarUrl
        )
        userMapper.updateById(updateUser)

        return getUserById(userId)
            ?: throw ErrorCode.USER_LOGIN_NOT_EXIST_ERROR.exception()
    }

    override fun checkUsernameExists(username: String): Boolean {
        return userMapper.selectCount(
            KtQueryWrapper(SysUser::class.java)
                .eq(SysUser::username, username)
        ) > 0
    }

    override fun checkNickNameExists(nickName: String): Boolean {
        return userMapper.selectCount(
            KtQueryWrapper(SysUser::class.java)
                .eq(SysUser::nickname, nickName)
        ) > 0
    }

    override fun checkEmailExists(email: String): Boolean {
        return userMapper.selectCount(
            KtQueryWrapper(SysUser::class.java)
                .eq(SysUser::email, email)
        ) > 0
    }

    override fun checkPhoneExists(phone: String): Boolean {
        return userMapper.selectCount(
            KtQueryWrapper(SysUser::class.java)
                .eq(SysUser::phone, phone)
        ) > 0
    }

    override fun deleteUser(userId: Long) {
        // 1. 查询用户是否存在
        val user = userMapper.selectById(userId)
            ?: throw ErrorCode.USER_LOGIN_NOT_EXIST_ERROR.exception()

        // 2. 不能删除管理员用户（可选：根据业务需求调整）
        if (user.isAdmin()) {
            throw ErrorCode.AUTH_ERROR.exception("不能删除管理员用户")
        }

        // 3. 删除用户角色关联
        userRoleMapper.delete(
            KtQueryWrapper(SysUserRole::class.java)
                .eq(SysUserRole::userId, userId)
        )

        // 4. 记录操作日志（保存删除前的数据快照）
        operationLogService.log(
            module = SysOperationLog.MODULE_USER,
            action = SysOperationLog.ACTION_DELETE,
            targetId = user.id,
            targetName = user.username,
            detail = user
        )

        // 5. 物理删除用户
        userMapper.deleteById(userId)

        // 6. 清除用户缓存
        cleanUserCache(userId)
    }

    override fun updateUserStatus(userId: Long, status: Int) {
        // 1. 校验状态值是否合法
        if (status != SysUser.STATUS_ENABLED && status != SysUser.STATUS_DISABLED) {
            throw ErrorCode.ARGUMENT_NOT_VALID_ERROR.exception("状态值不合法")
        }

        // 2. 查询用户是否存在
        val user = userMapper.selectById(userId)
            ?: throw ErrorCode.USER_LOGIN_NOT_EXIST_ERROR.exception()

        // 3. 不能禁用当前登录用户自己
        if (userId == currentUserId() && status == SysUser.STATUS_DISABLED) {
            throw ErrorCode.AUTH_ERROR.exception("不能禁用当前登录用户")
        }

        // 4. 更新用户状态
        val updateUser = SysUser(
            id = userId,
            status = status
        )
        userMapper.updateById(updateUser)

        // 5. 如果禁用用户，清除其登录缓存
        if (status == SysUser.STATUS_DISABLED) {
            cleanUserCache(userId)
        }
    }

    override fun listUsers(query: UserQueryDto): List<UserVO> {
        // 构建查询条件
        val queryWrapper = KtQueryWrapper(SysUser::class.java).apply {
            // ID 精确查询
            query.id?.let { eq(SysUser::id, it) }

            // 用户名模糊查询
            query.username?.takeIf { it.isNotBlank() }?.let {
                like(SysUser::username, "%$it%")
            }

            // 昵称模糊查询
            query.nickName?.takeIf { it.isNotBlank() }?.let {
                like(SysUser::nickname, "%$it%")
            }

            // 邮箱模糊查询
            query.email?.takeIf { it.isNotBlank() }?.let {
                like(SysUser::email, "%$it%")
            }

            // 手机号模糊查询
            query.phone?.takeIf { it.isNotBlank() }?.let {
                like(SysUser::phone, "%$it%")
            }

            // 状态精确查询
            query.status?.let { eq(SysUser::status, it) }

            // 按创建时间倒序排列（最新的在前）
            orderByDesc(SysUser::createTime)
        }

        // 执行查询
        val users = userMapper.selectList(queryWrapper)

        // 如果指定了角色筛选，需要进一步过滤
        return if (query.roles.isNotNullOrEmpty()) {
            // 查询有指定角色的用户ID列表
            val userIds = query.roles!!.map { userRoleMapper.selectUserIdsByRoleCode(it) }.flatten().distinct()
            users.filter { it.id in userIds }.map { convertToVO(it) }
        } else {
            users.map { convertToVO(it) }
        }
    }

    override fun encryptPassword(password: String): String {
        return BCrypt.hashpw(password)
    }

    override fun checkPassword(password: String, encryptPassword: String): Boolean {
        return BCrypt.checkpw(password, encryptPassword)
    }

    override fun getLoginRecords(userId: Long?): UserLoginRecordsDTO {
        if (userId == null) throw ErrorCode.USER_LOGIN_NOT_EXIST_ERROR.exception()
        val user = getUserById(userId) ?: throw ErrorCode.USER_LOGIN_NOT_EXIST_ERROR.exception()
        val redisKeys = redisTemplate.keys("${SecurityRedisKeyConst.USER_LOGIN_RECORDS}:${userId}:*")
        val records = redisKeys.associateWith { key ->
            redisTemplate.opsForValue().get(key)?.toObject<LoginRecordDTO>()
        }
        return UserLoginRecordsDTO(
            userId = userId,
            userInfo = user,
            records = records
        )
    }

    override fun getUserAvailableTokens(userId: Long?): Set<String> {
        if (userId == null) throw ErrorCode.USER_LOGIN_NOT_EXIST_ERROR.exception()
        val tokens = redisTemplate.opsForZSet().rangeByScore(buildUserTokensZSetRedisKey(userId), LocalDateTime.now().toTimestamp().toDouble(), Double.MAX_VALUE) ?: emptySet()
        return tokens.filter { token->
            // 如果不存在，则说明已经登出了
            // 需要过滤出尚未登出的Token
            redisTemplate.hasKey(buildUserTokenRedisKey(token))
        }.toSet()
    }

    /**
     * 转换为视图对象
     */
    private fun convertToVO(sysUser: SysUser): UserVO {
        // 查询用户角色
        val roles = sysUser.id?.let { userRoleMapper.selectRoleCodesByUserId(it) } ?: emptyList()

        return UserVO(
            id = sysUser.id,
            username = sysUser.username,
            nickname = sysUser.nickname,
            email = sysUser.email,
            phone = sysUser.phone,
            avatar = sysUser.avatar,
            status = sysUser.status,
            userType = sysUser.userType,
            roles = roles,
            createTime = sysUser.createTime,
            updateTime = sysUser.updateTime
        )
    }
}
