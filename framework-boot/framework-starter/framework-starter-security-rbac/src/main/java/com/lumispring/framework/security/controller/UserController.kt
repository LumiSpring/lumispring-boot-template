package com.lumispring.framework.security.controller

import com.lumispring.framework.base.model.Response
import com.lumispring.framework.security.config.PermissionCodes
import com.lumispring.framework.security.config.annotation.RequirePermission
import com.lumispring.framework.security.model.dto.PasswordResetDTO
import com.lumispring.framework.security.model.dto.UserCreateDTO
import com.lumispring.framework.security.model.dto.UserLoginRecordsDTO
import com.lumispring.framework.security.model.dto.UserQueryDto
import com.lumispring.framework.security.model.dto.UserUpdateDTO
import com.lumispring.framework.security.model.entity.SysUser
import com.lumispring.framework.security.model.vo.UserVO
import com.lumispring.framework.security.service.UserService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/admin/api/user")
class UserController(
    private val userService: UserService
) {

    @RequirePermission(PermissionCodes.USER_CREATE)
    @PostMapping
    fun createUser(@Valid @RequestBody userDTO: UserCreateDTO): Response<UserVO> {
        val userVO = userService.create(userDTO)
        return Response.success(userVO)
    }

    @RequirePermission(PermissionCodes.USER_GET)
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): Response<UserVO?> {
        val userVO = userService.getUserById(id)
        return Response.success(userVO)
    }

    @RequirePermission(PermissionCodes.USER_LIST)
    @GetMapping("/list")
    fun listUsers(queryDto: UserQueryDto): Response<List<UserVO>> {
        return Response.success(userService.listUsers(queryDto))
    }

    @RequirePermission(PermissionCodes.USER_DELETE)
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): Response<Boolean> {
        userService.deleteUser(id)
        return Response.success(true)
    }

    @RequirePermission(PermissionCodes.USER_UPDATE)
    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @Valid @RequestBody updateDTO: UserUpdateDTO): Response<UserVO> {
        val userVO = userService.updateUser(updateDTO.apply { userId = id })
        return Response.success(userVO)
    }

    @RequirePermission(PermissionCodes.USER_STATUS)
    @PutMapping("/{id}/status")
    fun updateUserStatus(
        @PathVariable id: Long,
        @RequestParam status: Int
    ): Response<Boolean> {
        userService.updateUserStatus(id, status)
        return Response.success(true)
    }

    @RequirePermission(PermissionCodes.USER_STATUS)
    @PutMapping("/{id}/enable")
    fun enableUser(@PathVariable id: Long): Response<Boolean> {
        userService.updateUserStatus(id, SysUser.STATUS_ENABLED)
        return Response.success(true)
    }

    @RequirePermission(PermissionCodes.USER_STATUS)
    @PutMapping("/{id}/disable")
    fun disableUser(@PathVariable id: Long): Response<Boolean> {
        userService.updateUserStatus(id, SysUser.STATUS_DISABLED)
        return Response.success(true)
    }

    @RequirePermission(PermissionCodes.USER_UPDATE)
    @PutMapping("/{id}/password")
    fun resetPassword(
        @PathVariable id: Long,
        @Valid @RequestBody passwordDTO: PasswordResetDTO
    ): Response<Boolean> {
        userService.resetPassword(id, passwordDTO)
        return Response.success(true)
    }

    @RequirePermission(PermissionCodes.USER_GET)
    @GetMapping("/{id}/sessions")
    fun getLoginRecords(@PathVariable id: Long): Response<UserLoginRecordsDTO> {
        return Response.success(userService.getLoginRecords(id))
    }
}
