package com.lumispring.framework.security.model.dto

data class UserQueryDto(
    var id: Long? = null,
    var username: String? = null,
    var nickName: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var status: Int? = null,
    var roles: List<String>? = null
)