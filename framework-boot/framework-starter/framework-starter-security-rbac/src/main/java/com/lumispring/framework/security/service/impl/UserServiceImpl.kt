package com.lumispring.framework.security.service.impl

import cn.hutool.crypto.digest.BCrypt
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl
import com.lumispring.framework.base.extension.checkNotNullOrEmpty
import com.lumispring.framework.base.extension.format
import com.lumispring.framework.base.extension.isNotNullOrEmpty
import com.lumispring.framework.base.extension.randomUuid
import com.lumispring.framework.base.extension.throwIf
import com.lumispring.framework.base.extension.toObject
import com.lumispring.framework.base.extension.toTimestamp
import com.lumispring.framework.base.model.ErrorCode
import com.lumispring.framework.database.redis.core.RedisClient
import com.lumispring.framework.security.config.SecurityProperties
import com.lumispring.framework.security.config.SecurityRedisKeyConst
import com.lumispring.framework.security.extension.currentToken
import com.lumispring.framework.security.extension.currentUser
import com.lumispring.framework.security.extension.currentUserId
import com.lumispring.framework.security.extension.isAdmin
import com.lumispring.framework.security.mapper.PermissionMapper
import com.lumispring.framework.security.mapper.RoleMapper
import com.lumispring.framework.security.mapper.UserMapper
import com.lumispring.framework.security.mapper.UserPermissionMapper
import com.lumispring.framework.security.mapper.UserRoleMapper
import com.lumispring.framework.security.model.AuthSession
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
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 用户服务实现类
 */
@Service
class UserServiceImpl(
    private val userMapper: UserMapper,
    private val roleMapper: RoleMapper,
    private val userRoleMapper: UserRoleMapper,
    private val userPermissionMapper: UserPermissionMapper,
    private val permissionMapper: PermissionMapper,
    private val operationLogService: OperationLogService,
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

    private fun generateToken(): String {
        while (true) {
            val token = randomUuid()
            if (!RedisClient.exists(buildUserTokenRedisKey(token))) return token
        }
    }

    /**
     * 根据账号查找用户，允许用户名、邮箱或手机号登录
     */
    private fun findUserByAccount(account: String): SysUser? {
        return userMapper.selectOne(KtQueryWrapper(SysUser::class.java).eq(SysUser::username, account))
            ?: userMapper.selectOne(KtQueryWrapper(SysUser::class.java).eq(SysUser::email, account))
            ?: userMapper.selectOne(KtQueryWrapper(SysUser::class.java).eq(SysUser::phone, account))
    }

    private fun hasAdminRole(userId: Long): Boolean {
        return userRoleMapper.selectRoleCodesByUserId(userId).contains(SysRole.ROLE_ADMIN)
    }

    private fun hasOtherEnabledAdmin(excludeUserId: Long): Boolean {
        val adminRole = roleMapper.selectByRoleCode(SysRole.ROLE_ADMIN) ?: return false
        val adminRoleId = adminRole.id ?: return false
        return userRoleMapper.selectUserIdsByRoleId(adminRoleId)
            .filter { it != excludeUserId }
            .any { userId -> userMapper.selectById(userId)?.isEnabled() == true }
    }

    override fun login(loginDTO: LoginDTO, expire: Long?): TokenVO {
        // 1. 校验用户账号是否存在
        val sysUser = findUserByAccount(loginDTO.username)
            ?: throw ErrorCode.USER_LOGIN_NOT_EXIST_ERROR.exception()

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
        val token = generateToken()
        val ttl = expire ?: securityProperties.timeout

        recordUserCache(userVo, token, ttl)
        resAddCookie("token", token, ttl.toInt())

        return TokenVO(
            accessToken = token,
            tokenType = "Bearer",
            expiresIn = ttl,
            userInfo = userVo
        )
    }

    @Transactional(rollbackFor = [Exception::class])
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

            roles.forEach {
                userRoleMapper.insert(
                    SysUserRole(
                        userId = sysUser.id,
                        roleId = it.id,
                        createTime = LocalDateTime.now()
                    )
                )
            }
        }

        return convertToVO(sysUser)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun register(registerDTO: RegisterDTO): TokenVO {
        if (registerDTO.password != registerDTO.confirmPassword) {
            throw ErrorCode.USER_REGISTER_ERROR.exception("两次输入的密码不一致")
        }
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
            val userId = currentUserId()!!
            val loginRecordKey = buildUserLoginRecordRedisKey(userId, token)
            val loginRecord = RedisClient.get<LoginRecordDTO>(loginRecordKey)
            RedisClient.del(buildUserTokenRedisKey(token))
            RedisClient.zRem(buildUserTokensZSetRedisKey(userId), token)
            LocalDateTime.now().let {
                loginRecord?.logoutTime = it.format()
                loginRecord?.logoutTimestamp = it.toTimestamp()
            }
            loginRecord?.let {
                RedisClient.set(loginRecordKey, it, securityProperties.timeout)
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
        val session = RedisClient.get<AuthSession>(buildUserTokenRedisKey(refreshToken))
        session.checkNotNullOrEmpty("用户信息不存在或已过期", ErrorCode.USER_LOGIN_ERROR)
        val userVo = getUserById(session.id)
            ?: throw ErrorCode.USER_LOGIN_NOT_EXIST_ERROR.exception()
        if (!userVo.isEnabled()) {
            cleanTokenCache(refreshToken)
            throw ErrorCode.USER_LOGIN_ERROR.exception("用户已被禁用")
        }

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
        val ttl = expire ?: securityProperties.timeout
        val expireTime = now.plusSeconds(ttl)
        val session = AuthSession.from(userVo, token)

        RedisClient.set(buildUserTokenRedisKey(token), session, ttl)

        val loginRecordKey = buildUserLoginRecordRedisKey(userVo.id!!, token)
        val loginRecord = RedisClient.get<LoginRecordDTO>(loginRecordKey)
        if (loginRecord.isNotNullOrEmpty()) {
            loginRecord.expireTimestamp = expireTime.toTimestamp()
            loginRecord.expireTime = expireTime.format()
            RedisClient.set(loginRecordKey, loginRecord, ttl)
        } else {
            RedisClient.set(
                loginRecordKey,
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
                ),
                ttl
            )
        }
        RedisClient.zAdd(buildUserTokensZSetRedisKey(userVo.id), token, expireTime.toTimestamp().toDouble())
    }

    override fun refreshUserCache(userId: Long): UserVO {
        val userVo = getUserById(userId)
            ?: throw ErrorCode.USER_LOGIN_NOT_EXIST_ERROR.exception()

        val tokens = getUserAvailableTokens(userId)
        tokens.forEach { token ->
            val key = buildUserTokenRedisKey(token)
            val ttl = RedisClient.ttl(key) ?: -1
            val session = AuthSession.from(userVo, token)
            if (ttl > 0) {
                RedisClient.set(key, session, ttl)
            } else {
                RedisClient.set(key, session)
            }
        }

        return userVo
    }

    override fun cleanUserCache(userId: Long): Boolean {
        val tokens = getUserAvailableTokens(userId)
        tokens.forEach { token ->
            RedisClient.del(buildUserTokenRedisKey(token))
            RedisClient.del(buildUserLoginRecordRedisKey(userId, token))
        }
        RedisClient.del(buildUserTokensZSetRedisKey(userId))
        return true
    }

    override fun cleanTokenCache(token: String) {
        val session = RedisClient.get<AuthSession>(buildUserTokenRedisKey(token))
        session?.let {
            RedisClient.zRem(buildUserTokensZSetRedisKey(it.id), token)
        }
        RedisClient.del(buildUserTokenRedisKey(token))
    }

    @Transactional(rollbackFor = [Exception::class])
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

    @Transactional(rollbackFor = [Exception::class])
    override fun updatePassword(passwordDTO: PasswordUpdateDTO) {
        val userId = passwordDTO.userId ?: currentUserId()!!
        throwIf(userId != currentUserId() && !isAdmin(), "无修改权限", errorCode = ErrorCode.AUTH_ERROR)

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
        operationLogService.log(
            module = SysOperationLog.MODULE_USER,
            action = SysOperationLog.ACTION_UPDATE,
            targetId = userId,
            targetName = user.username,
            detail = mapOf("action" to "password")
        )
        cleanUserCache(userId)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun resetPassword(userId: Long, passwordDTO: PasswordResetDTO) {
        val user = userMapper.selectById(userId)
            ?: throw ErrorCode.USER_LOGIN_NOT_EXIST_ERROR.exception()

        if (passwordDTO.newPassword != passwordDTO.confirmPassword) {
            throw ErrorCode.USER_REGISTER_ERROR.exception("两次输入的新密码不一致")
        }

        userMapper.updateById(
            SysUser(
                id = userId,
                password = encryptPassword(passwordDTO.newPassword)
            )
        )
        operationLogService.log(
            module = SysOperationLog.MODULE_USER,
            action = SysOperationLog.ACTION_UPDATE,
            targetId = userId,
            targetName = user.username,
            detail = mapOf("action" to "reset-password")
        )
        cleanUserCache(userId)
    }

    override fun updateAvatar(userId: Long, avatarUrl: String): UserVO {
        val updateUser = SysUser(
            id = userId,
            avatar = avatarUrl
        )
        userMapper.updateById(updateUser)
        return refreshUserCache(userId)
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

    @Transactional(rollbackFor = [Exception::class])
    override fun deleteUser(userId: Long) {
        val user = userMapper.selectById(userId)
            ?: throw ErrorCode.USER_LOGIN_NOT_EXIST_ERROR.exception()

        if (userId == currentUserId()) {
            throw ErrorCode.AUTH_ERROR.exception("不能删除当前登录用户")
        }

        if (hasAdminRole(userId) && !hasOtherEnabledAdmin(userId)) {
            throw ErrorCode.AUTH_ERROR.exception("不能删除最后一个管理员用户")
        }

        userRoleMapper.deleteByUserId(userId)
        userPermissionMapper.deleteByUserId(userId)

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

    @Transactional(rollbackFor = [Exception::class])
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

        if (status == SysUser.STATUS_DISABLED && hasAdminRole(userId) && !hasOtherEnabledAdmin(userId)) {
            throw ErrorCode.AUTH_ERROR.exception("不能禁用最后一个管理员")
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
        val roleUserIds = if (query.roles.isNotNullOrEmpty()) {
            query.roles!!.flatMap { userRoleMapper.selectUserIdsByRoleCode(it) }.distinct()
        } else {
            null
        }
        if (roleUserIds != null && roleUserIds.isEmpty()) {
            return emptyList()
        }

        val queryWrapper = KtQueryWrapper(SysUser::class.java).apply {
            query.id?.let { eq(SysUser::id, it) }
            query.username?.takeIf { it.isNotBlank() }?.let { like(SysUser::username, it) }
            query.nickName?.takeIf { it.isNotBlank() }?.let { like(SysUser::nickname, it) }
            query.email?.takeIf { it.isNotBlank() }?.let { like(SysUser::email, it) }
            query.phone?.takeIf { it.isNotBlank() }?.let { like(SysUser::phone, it) }
            query.status?.let { eq(SysUser::status, it) }
            roleUserIds?.let { `in`(SysUser::id, it) }
            orderByDesc(SysUser::createTime)
        }

        return userMapper.selectList(queryWrapper).map { convertToVO(it) }
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
        val tokens = getUserAvailableTokens(userId)
        val records = tokens.associate { token ->
            val key = buildUserLoginRecordRedisKey(userId, token)
            key to RedisClient.get<LoginRecordDTO>(key)
        }
        return UserLoginRecordsDTO(
            userId = userId,
            userInfo = user,
            records = records
        )
    }

    override fun getUserAvailableTokens(userId: Long?): Set<String> {
        if (userId == null) throw ErrorCode.USER_LOGIN_NOT_EXIST_ERROR.exception()
        val tokens = RedisClient.zRangeByScore<String>(
            buildUserTokensZSetRedisKey(userId),
            LocalDateTime.now().toTimestamp().toDouble(),
            Double.MAX_VALUE
        )
        return tokens.filter { token->
            RedisClient.exists(buildUserTokenRedisKey(token))
        }.toSet()
    }

    /**
     * 转换为视图对象
     */
    private fun convertToVO(sysUser: SysUser): UserVO {
        val userId = sysUser.id
        val roles = userId?.let { userRoleMapper.selectRoleCodesByUserId(it) } ?: emptyList()
        val permissions = userId?.let { permissionMapper.selectPermissionCodesByUserId(it) } ?: emptyList()

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
            permissions = permissions,
            createTime = sysUser.createTime,
            updateTime = sysUser.updateTime
        )
    }
}
