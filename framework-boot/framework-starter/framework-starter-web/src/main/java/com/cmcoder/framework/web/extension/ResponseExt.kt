package com.lumispring.framework.web.extension

import jakarta.servlet.http.Cookie

/**
 * 响应添加cookie
 */
fun resAddCookie(name: String, value: String, maxAge: Int = -1) {
    currentResponse()?.addCookie(Cookie(name, value).apply {
        this.maxAge = maxAge
    })
}

/**
 * 响应输出
 * @param content 输出内容
 */
fun resWrite(content: String) {
    currentResponse()?.writer?.write(content)
}

/**
 * 响应重定向
 * @param url 重定向地址
 */
fun resRedirect(url: String) {
    currentResponse()?.sendRedirect(url)
}