package com.lumispring.framework.security.model

import com.lumispring.framework.security.model.entity.SysRole
import com.lumispring.framework.security.model.vo.UserVO
import java.time.LocalDateTime

/**
 * 登录会话快照，写入 Redis。不含 Jackson 脱敏序列化，避免把掩码手机号存进缓存。
 */
data class AuthSession(
    val id: Long,
    val username: String? = null,
    val nickname: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val avatar: String? = null,
    val status: Int? = null,
    val userType: Int? = null,
    val roles: List<String> = emptyList(),
    val permissions: List<String> = emptyList(),
    val token: String? = null,
    val createTime: LocalDateTime? = null,
    val updateTime: LocalDateTime? = null
) {
    fun isAdmin(): Boolean = roles.contains(SysRole.ROLE_ADMIN)

    fun toUserVO(): UserVO = UserVO(
        id = id,
        username = username,
        nickname = nickname,
        email = email,
        phone = phone,
        avatar = avatar,
        status = status,
        userType = userType,
        roles = roles,
        permissions = permissions,
        token = token,
        createTime = createTime,
        updateTime = updateTime
    )

    companion object {
        fun from(user: UserVO, token: String): AuthSession = AuthSession(
            id = user.id ?: throw IllegalArgumentException("user id required"),
            username = user.username,
            nickname = user.nickname,
            email = user.email,
            phone = user.phone,
            avatar = user.avatar,
            status = user.status,
            userType = user.userType,
            roles = user.roles.orEmpty(),
            permissions = user.permissions.orEmpty(),
            token = token,
            createTime = user.createTime,
            updateTime = user.updateTime
        )
    }
}
