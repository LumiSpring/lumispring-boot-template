package com.lumispring.framework.web.extension

import com.lumispring.framework.base.extension.isNullOrEmpty

/**
 * 获取请求ip
 */
fun reqGetIp():String{
    return currentRequest().let { httpServletRequest ->
        var ipAddress = httpServletRequest?.getHeader("X-Forwarded-For") ?: httpServletRequest?.getHeader("x-forwarded-for")
        if (ipAddress.isNullOrEmpty() || "unknown".equals(ipAddress, ignoreCase = true)) {
            ipAddress = httpServletRequest?.getHeader("Proxy-Client-IP") ?: httpServletRequest?.getHeader("proxy-client-ip")
        }
        if (ipAddress.isNullOrEmpty() || "unknown".equals(ipAddress, ignoreCase = true)) {
            ipAddress = httpServletRequest?.getHeader("WL-Proxy-Client-IP") ?: httpServletRequest?.getHeader("wl-proxy-client-ip")
        }
        if (ipAddress.isNullOrEmpty() || "unknown".equals(ipAddress, ignoreCase = true)) {
            ipAddress = httpServletRequest?.getHeader("HTTP_CLIENT_IP") ?: httpServletRequest?.getHeader("http_client_ip")
        }
        if (ipAddress.isNullOrEmpty() || "unknown".equals(ipAddress, ignoreCase = true)) {
            ipAddress = httpServletRequest?.remoteAddr
        }
        ipAddress?.split(",")?.get(0)
    } ?: ""
}

/**
 * 获取用户代理
 */
fun reqGetUserAgent():String{
    return currentRequest()?.getHeader("User-Agent") ?: ""
}

/**
 * 获取 referer
 */
fun reqGetReferer():String{
    return currentRequest()?.getHeader("Referer") ?: ""
}

/**
 * 获取所有请求头信息
 */
fun reqGetHeaders(): Map<String, String> {
    return currentRequest()?.headerNames?.toList()?.let { headerNameList->
        headerNameList.associateWith { currentRequest()!!.getHeader(it) }
    } ?: mapOf()
}


/**
 * 获取cookie
 */
fun reqGetCookies():Map<String, String>{
    return currentRequest().let { httpServletRequest ->
        val cookies = httpServletRequest?.cookies
        val cookieMap = mutableMapOf<String, String>()
        cookies?.forEach { cookie ->
            cookieMap[cookie.name] = cookie.value
        }
        cookieMap
    } ?: emptyMap()
}

/**
 * 获取cookie
 */
fun reqGetCookie(name:String):String?{
    return currentRequest().let { httpServletRequest ->
        val cookie = httpServletRequest?.cookies?.find { it.name == name }
        cookie?.value
    }
}