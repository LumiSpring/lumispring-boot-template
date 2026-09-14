package com.lumispring.framework.base.extension

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.lumispring.framework.base.model.ErrorCode
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date

class Jwt {

    companion object {
        fun builder(): JwtBuilder{
            return JwtBuilder()
        }
    }

    class JwtBuilder {
        private var sign: String = ""

        /**
         * 盐值
         */
        fun sign(sign: String) = this.apply { this.sign = sign }


        /**
         * 生成token
         * @param data 载荷数据
         * @param expireDate 过期时间，默认7天
         * @param algorithm 算法，默认HMAC256
         */
        fun generateToken(data: Any?, expireDate: LocalDateTime = now().plusDays(7), algorithm: Algorithm = Algorithm.HMAC256(sign)): String {
            val dataMap = try {
                data.toSMap()
            }catch (e: Exception){
                throw ErrorCode.SERVICE_ERROR.exception(log = "jwt生成异常，请检测传入的数据类型是否正确，需要对象或者Map类型，当前传入date：${data}")
            }

            val builder = JWT.create()
            dataMap?.forEach { (k,v) ->
                when(v){
                    is String -> {
                        builder.withClaim(k,v)
                    }
                    is Int -> {
                        builder.withClaim(k,v)
                    }
                    is Double -> {
                        builder.withClaim(k,v)
                    }
                    is Long -> {
                        builder.withClaim(k,v)
                    }
                    is Boolean -> {
                        builder.withClaim(k,v)
                    }
                    is Date -> {
                        builder.withClaim(k,v)
                    }
                    is LocalDate -> {
                        builder.withClaim(k,v.toDate())
                    }
                    is LocalDateTime -> {
                        builder.withClaim(k,v.toDate())
                    }
                    is List<*> -> {
                        builder.withClaim(k,v)
                    }
                    else -> {
                        builder.withClaim(k,v.toString())
                    }
                }
            }
            // 设置过期时间
            builder.withExpiresAt(expireDate.toDate())
            return builder.sign(algorithm)
        }


        /**
         * 解析token
         */
        fun parseToken(token: String, algorithm: Algorithm = Algorithm.HMAC256(sign)): SMap?{
            val verify = JWT.require(algorithm).build().verify(token)
            return verify.claims.entries.associate { (k,v) ->
                k to v.toString()
            }
        }
    }
}