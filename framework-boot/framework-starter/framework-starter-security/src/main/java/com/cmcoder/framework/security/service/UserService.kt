package com.lumispring.framework.security.service

import com.baomidou.mybatisplus.extension.service.IService
import com.lumispring.framework.security.extension.currentToken
import com.lumispring.framework.security.extension.currentUserId
import com.lumispring.framework.security.model.dto.*
import com.lumispring.framework.security.model.entity.SysUser
import com.lumispring.framework.security.model.vo.TokenVO
import com.lumispring.framework.security.model.vo.UserVO

/**
 * 用户服务接口
 */
interface UserService : IService<SysUser> {

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @param expire 过期时间，单位秒，默认值为配置中的过期时间
     * @return Token信息
     */
    fun login(loginDTO: LoginDTO, expire: Long? = null): TokenVO

    /**
     * 创建用户
     * @param userDto 用户信息
     */
    fun create(userDto: UserCreateDTO): UserVO

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return Token信息
     */
    fun register(registerDTO: RegisterDTO): TokenVO

    /**
     * 当前登录用户登出
     */
    fun logout()

    /**
     * 踢出当前用户的所有Token
     */
    fun logoutAll()

    /**
     * 刷新Token到期时间
     * @param expire 过期时间，单位秒，默认值为配置中的过期时间
     * @param refreshToken 刷新Token，如果为空则使用当前Token刷新
     * @return 新的Token
     */
    fun refreshToken(expire: Long? = null, refreshToken: String? = currentToken()): TokenVO

    /**
     * 获取当前登录用户
     *
     * @return 用户信息
     */
    fun getCurrentUser(): UserVO?

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    fun getUserById(id: Long): UserVO?


    /**
     * 根据ID列表查询用户
     * @param ids ID列表
     * @return 用户信息列表
     */
    fun listUsersByIds(ids: List<Long>): List<UserVO>

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    fun getUserByUsername(username: String): SysUser?

    /**
     * 更新用户信息
     *
     * @param updateDTO 更新信息
     * @return 更新后的用户信息
     */
    fun updateUser(updateDTO: UserUpdateDTO): UserVO

    /**
     * 修改密码
     *
     * @param passwordDTO 密码信息
     */
    fun updatePassword(passwordDTO: PasswordUpdateDTO)

    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     * @return 更新后的用户信息
     */
    fun updateAvatar(userId: Long, avatarUrl: String): UserVO


    /**
     * 校验用户名是否存在
     *
     * @param username 用户名
     * @return true-存在，false-不存在
     */
    fun checkUsernameExists(username: String): Boolean

    /**
     * 校验昵称是否存在
     *
     * @param nickName 昵称
     * @return true-存在，false-不存在
     */
    fun checkNickNameExists(nickName: String): Boolean

    /**
     * 校验邮箱是否存在
     *
     * @param email 邮箱
     * @return true-存在，false-不存在
     */
    fun checkEmailExists(email: String): Boolean

    /**
     * 校验手机号是否存在
     *
     * @param phone 手机号
     * @return true-存在，false-不存在
     */
    fun checkPhoneExists(phone: String): Boolean

    /**
     * 记录用户缓存
     * @param userVo 用户信息
     * @param token Token
     * @param expire 过期时间，单位秒，默认值为配置中的过期时间
     */
    fun recordUserCache(userVo: UserVO, token: String, expire: Long? = null)

    /**
     * 刷新用户的所有缓存信息
     * @param userId 用户ID
     */
    fun refreshUserCache(userId: Long): UserVO?

    /**
     * 清理用户的所有缓存
     * @param userId 用户ID
     */
    fun cleanUserCache(userId: Long): Boolean

    /**
    *  清理Token缓存
     * @param token token
     */
    fun cleanTokenCache(token: String)

    /**
     * 删除用户
     *
     * @param userId 用户ID
     */
    fun deleteUser(userId: Long)

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 状态：0-禁用，1-启用
     */
    fun updateUserStatus(userId: Long, status: Int)

    /**
     * 查询用户列表
     * @param query 查询参数
     * @return 用户列表
     */
    fun listUsers(query: UserQueryDto): List<UserVO>

    /**
     * 加密密码
     * @param password 密码
     * @return 加密后的密码
     */
    fun encryptPassword(password: String): String

    /**
     * 校验密码
     * @param password 明文密码
     * @param encryptPassword 加密后的密码
     */
    fun checkPassword(password: String, encryptPassword: String): Boolean

    /**
     * 查询用户登录记录
     * @param userId 用户ID，默认值为当前登录用户
     * @return 登录记录列表
     */
    fun getLoginRecords(userId: Long? = currentUserId()): UserLoginRecordsDTO

    /**
     * 获取用户未过期Token
     * @param userId 用户ID，默认值为当前登录用户
     * @return 未过期Token列表
     */
    fun getUserAvailableTokens(userId: Long? = currentUserId()): Set<String>
}
