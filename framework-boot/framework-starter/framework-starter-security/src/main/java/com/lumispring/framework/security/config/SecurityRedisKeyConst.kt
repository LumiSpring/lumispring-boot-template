package com.lumispring.framework.security.config

interface SecurityRedisKeyConst {

    companion object {
        /**
         * 根据Token获取用户信息
         */
        val USER_INFO_BY_TOKEN_PREFIX: String = "auth:user:token"

        /**
         * 根据用户获取用户的登录记录
         */
        val USER_LOGIN_RECORDS: String = "auth:records:user"

        /**
         * 根据用户获取用户Token
         */
        val USER_LOGIN_TOKENS_ZSET: String = "auth:tokens:user"
    }
}