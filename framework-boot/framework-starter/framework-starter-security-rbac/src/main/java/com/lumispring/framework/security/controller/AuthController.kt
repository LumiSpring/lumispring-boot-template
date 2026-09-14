package com.lumispring.framework.security.controller

import com.lumispring.framework.base.model.Response
import com.lumispring.framework.security.extension.currentUserId
import com.lumispring.framework.security.model.dto.LoginDTO
import com.lumispring.framework.security.model.dto.PasswordUpdateDTO
import com.lumispring.framework.security.model.dto.RegisterDTO
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
//@RestController
//@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService
) {

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return Token信息
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody loginDTO: LoginDTO): Response<TokenVO> {
        val tokenVO = userService.login(loginDTO)
        return Response.success(tokenVO)
    }

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return Token信息
     */
    @PostMapping("/register")
    fun register(@Valid @RequestBody registerDTO: RegisterDTO): Response<TokenVO> {
        val tokenVO = userService.register(registerDTO)
        return Response.success(tokenVO)
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    fun logout(): Response<Boolean> {
        userService.logout()
        return Response.success(true)
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/user")
    fun getCurrentUser(): Response<UserVO?> {
        val userVO = userService.getCurrentUser()
        return Response.success(userVO)
    }

    /**
     * 更新当前用户信息
     *
     * @param updateDTO 更新信息
     * @return 更新后的用户信息
     */
    @PutMapping("/user")
    fun updateCurrentUser(@Valid @RequestBody updateDTO: UserUpdateDTO): Response<UserVO> {
        val userVO = userService.updateUser(updateDTO.apply {
            userId = currentUserId()
        })
        return Response.success(userVO)
    }

    /**
     * 修改当前用户密码
     *
     * @param passwordDTO 密码信息
     */
    @PutMapping("/password")
    fun updatePassword(@Valid @RequestBody passwordDTO: PasswordUpdateDTO): Response<Boolean> {
        userService.updatePassword(passwordDTO.apply {
            userId = currentUserId()
        })
        return Response.success(true)
    }

    /**
     * 检查用户名是否已存在
     *
     * @param username 用户名
     * @return true-存在，false-不存在
     */
    @GetMapping("/check/username")
    fun checkUsernameExists(@RequestParam username: String): Response<Boolean> {
        val exists = userService.checkUsernameExists(username)
        return Response.success(exists)
    }

    /**
     * 检查邮箱是否已存在
     *
     * @param email 邮箱
     * @return true-存在，false-不存在
     */
    @GetMapping("/check/email")
    fun checkEmailExists(@RequestParam email: String): Response<Boolean> {
        val exists = userService.checkEmailExists(email)
        return Response.success(exists)
    }

    /**
     * 检查手机号是否已存在
     *
     * @param phone 手机号
     * @return true-存在，false-不存在
     */
    @GetMapping("/check/phone")
    fun checkPhoneExists(@RequestParam phone: String): Response<Boolean> {
        val exists = userService.checkPhoneExists(phone)
        return Response.success(exists)
    }
}
