package com.lumispring.framework.security.controller

import com.lumispring.framework.base.model.Response
import com.lumispring.framework.security.config.annotation.RequireAdmin
import com.lumispring.framework.security.model.dto.RegisterDTO
import com.lumispring.framework.security.model.dto.UserCreateDTO
import com.lumispring.framework.security.model.dto.UserQueryDto
import com.lumispring.framework.security.model.dto.UserUpdateDTO
import com.lumispring.framework.security.model.vo.UserVO
import com.lumispring.framework.security.service.UserService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

/**
 * 用户管理控制器
 * 提供用户管理相关接口（需要管理员权限）
 */
@RestController
@RequestMapping("/admin/api/user")
@RequireAdmin
class UserController(
    private val userService: UserService
) {

    /**
     * 创建用户
     */
    @PostMapping
    fun createUser(@RequestBody userDTO: UserCreateDTO): Response<UserVO> {
        val userVO = userService.create(userDTO)
        return Response.success(userVO)
    }


    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): Response<UserVO?> {
        val userVO = userService.getUserById(id)
        return Response.success(userVO)
    }

    /**
     * 查询用户列表
     * @param queryDto 查询条件
     */
    @GetMapping("/list")
    fun listUsers(queryDto: UserQueryDto): Response<List<UserVO>> {
        return Response.success(userService.listUsers(queryDto))
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): Response<Boolean> {
        userService.deleteUser(id)
        return Response.success(true)
    }

    /**
     * 更新用户信息
     *
     * @param updateDTO 更新信息
     * @return 更新后的用户信息
     */
    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody updateDTO: UserUpdateDTO): Response<UserVO> {
        val userVO = userService.updateUser(updateDTO)
        return Response.success(userVO)
    }

    /**
     * 修改用户状态
     *
     * @param id 用户ID
     * @param status 状态：0-禁用，1-启用
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    fun updateUserStatus(
        @PathVariable id: Long,
        @RequestParam status: Int
    ): Response<Boolean> {
        userService.updateUserStatus(id, status)
        return Response.success(true)
    }

    /**
     * 启用用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @PutMapping("/{id}/enable")
    fun enableUser(@PathVariable id: Long): Response<Boolean> {
        userService.updateUserStatus(id, 1)
        return Response.success(true)
    }

    /**
     * 禁用用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @PutMapping("/{id}/disable")
    fun disableUser(@PathVariable id: Long): Response<Boolean> {
        userService.updateUserStatus(id, 0)
        return Response.success(true)
    }
}
