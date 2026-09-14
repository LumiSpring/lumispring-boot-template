package com.lumispring.framework.security.model.dto

/**
 * 记录用户登录时的设备信息和过期时间
 * @param userId 用户ID
 * @param token 用户登录Token
 * @param ip 用户登录IP
 * @param userAgent 用户登录浏览器
 * @param referer 用户登录来源
 * @param loginTime 用户登录时间
 * @param loginTimestamp 用户登录时间戳
 * @param logoutTime 用户退出时间
 * @param logoutTimestamp 用户退出时间戳
 * @param expireTimestamp 过期时间戳
 * @param expireTime 过期时间
 * @param headers 用户登录请求头
 */
data class LoginRecordDTO(
    var userId: Long? = null,
    var token: String? = null,
    var ip: String? = null,
    var userAgent: String? = null,
    var referer: String? = null,
    var loginTime: String? = null,
    var loginTimestamp: Long? = null,
    var logoutTime: String? = null,
    var logoutTimestamp: Long? = null,
    var expireTimestamp: Long? = null,
    var expireTime: String? = null,
    var headers: Map<String, String>? = null,
)