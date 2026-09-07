package com.lumispring.framework.security.controller

import com.lumispring.framework.base.model.ErrorCode
import com.lumispring.framework.base.model.Response
import com.lumispring.framework.security.config.annotation.RequireAdmin
import com.lumispring.framework.security.model.dto.RoleDTO
import com.lumispring.framework.security.model.dto.RoleUpdateDTO
import com.lumispring.framework.security.model.vo.RoleVO
import com.lumispring.framework.security.service.RoleService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

/**
 * 角色管理控制器
 * 提供角色的增删改查接口
 * 路径前缀由 SecurityApiPathConfig 统一配置为 /api
 */
@RestController
@RequestMapping("/admin/api/role")
@RequireAdmin
class RoleController(
    private val roleService: RoleService
) {

    /**
     * 创建角色
     *
     * @param roleDTO 角色信息
     * @return 创建后的角色
     */
    @PostMapping
    fun createRole(@Valid @RequestBody roleDTO: RoleDTO): Response<RoleVO> {
        val roleVO = roleService.createRole(roleDTO)
        return Response.success(roleVO)
    }

    /**
     * 更新角色
     *
     * @param id 角色ID
     * @param roleDTO 角色信息
     * @return 更新后的角色
     */
    @PutMapping("/{id}")
    fun updateRole(
        @PathVariable id: Long,
        @Valid @RequestBody roleDTO: RoleUpdateDTO
    ): Response<RoleVO> {
        val roleVO = roleService.updateRole(id, roleDTO)
        return Response.success(roleVO)
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    @DeleteMapping("/{id}")
    @RequireAdmin
    fun deleteRole(@PathVariable id: Long): Response<Boolean> {
        roleService.deleteRole(id)
        return Response.success(true)
    }

    /**
     * 根据ID查询角色
     *
     * @param id 角色ID
     * @return 角色信息
     */
    @GetMapping("/{id}")
    fun getRoleById(@PathVariable id: Long): Response<RoleVO> {
        val roleVO = roleService.getRoleById(id)
            ?: throw ErrorCode.SERVICE_ERROR.exception("角色不存在")
        return Response.success(roleVO)
    }

    /**
     * 查询所有启用的角色
     *
     * @return 角色列表
     */
    @GetMapping("/list")
    fun listAllEnabledRoles(): Response<List<RoleVO>> {
        val roles = roleService.listAllEnabledRoles()
        return Response.success(roles)
    }

    /**
     * 为用户分配角色
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    @PostMapping("/assign/{userId}")
    fun assignRolesToUser(
        @PathVariable userId: Long,
        @RequestBody roleIds: List<Long>
    ): Response<Boolean> {
        roleService.assignRolesToUser(userId, roleIds)
        return Response.success(true)
    }

    /**
     * 获取用户的角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @GetMapping("/user/{userId}")
    fun getRolesByUserId(@PathVariable userId: Long): Response<List<RoleVO>> {
        val roles = roleService.getRolesByUserId(userId)
        return Response.success(roles)
    }

    /**
     * 检查角色编码是否存在
     *
     * @param roleCode 角色编码
     * @return true-存在，false-不存在
     */
    @GetMapping("/check/code")
    fun checkRoleCodeExists(@RequestParam roleCode: String): Response<Boolean> {
        val exists = roleService.checkRoleCodeExists(roleCode)
        return Response.success(exists)
    }
}
