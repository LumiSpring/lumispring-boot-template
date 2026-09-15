package com.lumispring.framework.security.controller

import com.lumispring.framework.base.model.Response
import com.lumispring.framework.security.config.annotation.RequireLogin
import com.lumispring.framework.security.config.annotation.UnAuth
import com.lumispring.framework.security.extension.currentUserId
import com.lumispring.framework.security.model.dto.LoginDTO
import com.lumispring.framework.security.model.dto.PasswordUpdateDTO
import com.lumispring.framework.security.model.dto.RegisterDTO
import com.lumispring.framework.security.model.dto.UserLoginRecordsDTO
import com.lumispring.framework.security.model.dto.UserUpdateDTO
import com.lumispring.framework.security.model.vo.TokenVO
import com.lumispring.framework.security.model.vo.UserVO
import com.lumispring.framework.security.service.UserService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

/**
 * 认证控制器
 * 提供登录、注册、登出等接口
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService
) {

    /**
     * 用户登录
     */
    @UnAuth
    @PostMapping("/login")
    fun login(@Valid @RequestBody loginDTO: LoginDTO): Response<TokenVO> {
        val tokenVO = userService.login(loginDTO)
        return Response.success(tokenVO)
    }

    /**
     * 用户注册
     */
    @UnAuth
    @PostMapping("/register")
    fun register(@Valid @RequestBody registerDTO: RegisterDTO): Response<TokenVO> {
        val tokenVO = userService.register(registerDTO)
        return Response.success(tokenVO)
    }

    /**
     * 用户登出
     */
    @RequireLogin
    @PostMapping("/logout")
    fun logout(): Response<Boolean> {
        userService.logout()
        return Response.success(true)
    }

    /**
     * 登出全部会话
     */
    @RequireLogin
    @PostMapping("/logout-all")
    fun logoutAll(): Response<Boolean> {
        userService.logoutAll()
        return Response.success(true)
    }

    /**
     * 刷新当前 Token 过期时间
     */
    @RequireLogin
    @PostMapping("/refresh")
    fun refreshToken(): Response<TokenVO> {
        return Response.success(userService.refreshToken())
    }

    /**
     * 获取当前登录用户信息
     */
    @RequireLogin
    @GetMapping("/user")
    fun getCurrentUser(): Response<UserVO?> {
        val userVO = userService.getCurrentUser()
        return Response.success(userVO)
    }

    /**
     * 更新当前用户信息
     */
    @RequireLogin
    @PutMapping("/user")
    fun updateCurrentUser(@Valid @RequestBody updateDTO: UserUpdateDTO): Response<UserVO> {
        val userVO = userService.updateUser(updateDTO.apply {
            userId = currentUserId()
        })
        return Response.success(userVO)
    }

    /**
     * 修改当前用户密码。修改成功后需重新登录。
     */
    @RequireLogin
    @PutMapping("/password")
    fun updatePassword(@Valid @RequestBody passwordDTO: PasswordUpdateDTO): Response<Boolean> {
        userService.updatePassword(passwordDTO.apply {
            userId = currentUserId()
        })
        return Response.success(true)
    }

    /**
     * 当前用户的登录会话
     */
    @RequireLogin
    @GetMapping("/sessions")
    fun getLoginRecords(): Response<UserLoginRecordsDTO> {
        return Response.success(userService.getLoginRecords())
    }

    /**
     * 检查用户名是否已存在
     */
    @UnAuth
    @GetMapping("/check/username")
    fun checkUsernameExists(@RequestParam username: String): Response<Boolean> {
        val exists = userService.checkUsernameExists(username)
        return Response.success(exists)
    }

    /**
     * 检查邮箱是否已存在
     */
    @UnAuth
    @GetMapping("/check/email")
    fun checkEmailExists(@RequestParam email: String): Response<Boolean> {
        val exists = userService.checkEmailExists(email)
        return Response.success(exists)
    }

    /**
     * 检查手机号是否已存在
     */
    @UnAuth
    @GetMapping("/check/phone")
    fun checkPhoneExists(@RequestParam phone: String): Response<Boolean> {
        val exists = userService.checkPhoneExists(phone)
        return Response.success(exists)
    }
}
