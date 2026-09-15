package com.lumispring.framework.security.controller

import com.lumispring.framework.base.model.ErrorCode
import com.lumispring.framework.base.model.Response
import com.lumispring.framework.security.config.PermissionCodes
import com.lumispring.framework.security.config.annotation.RequirePermission
import com.lumispring.framework.security.model.dto.RoleDTO
import com.lumispring.framework.security.model.dto.RoleUpdateDTO
import com.lumispring.framework.security.model.vo.RoleVO
import com.lumispring.framework.security.service.RoleService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/admin/api/role")
class RoleController(
    private val roleService: RoleService
) {

    @RequirePermission(PermissionCodes.ROLE_CREATE)
    @PostMapping
    fun createRole(@Valid @RequestBody roleDTO: RoleDTO): Response<RoleVO> {
        val roleVO = roleService.createRole(roleDTO)
        return Response.success(roleVO)
    }

    @RequirePermission(PermissionCodes.ROLE_UPDATE)
    @PutMapping("/{id}")
    fun updateRole(
        @PathVariable id: Long,
        @Valid @RequestBody roleDTO: RoleUpdateDTO
    ): Response<RoleVO> {
        val roleVO = roleService.updateRole(id, roleDTO)
        return Response.success(roleVO)
    }

    @RequirePermission(PermissionCodes.ROLE_DELETE)
    @DeleteMapping("/{id}")
    fun deleteRole(@PathVariable id: Long): Response<Boolean> {
        roleService.deleteRole(id)
        return Response.success(true)
    }

    @RequirePermission(PermissionCodes.ROLE_GET)
    @GetMapping("/{id}")
    fun getRoleById(@PathVariable id: Long): Response<RoleVO> {
        val roleVO = roleService.getRoleById(id)
            ?: throw ErrorCode.RESOURCE_NOT_FIND.exception("角色不存在")
        return Response.success(roleVO)
    }

    @RequirePermission(PermissionCodes.ROLE_LIST)
    @GetMapping("/list")
    fun listAllRoles(): Response<List<RoleVO>> {
        val roles = roleService.listAllRoles()
        return Response.success(roles)
    }

    @RequirePermission(PermissionCodes.ROLE_LIST)
    @GetMapping("/enabled")
    fun listAllEnabledRoles(): Response<List<RoleVO>> {
        val roles = roleService.listAllEnabledRoles()
        return Response.success(roles)
    }

    @RequirePermission(PermissionCodes.ROLE_ASSIGN)
    @PostMapping("/assign/{userId}")
    fun assignRolesToUser(
        @PathVariable userId: Long,
        @RequestBody roleIds: List<Long>
    ): Response<Boolean> {
        roleService.assignRolesToUser(userId, roleIds)
        return Response.success(true)
    }

    @RequirePermission(PermissionCodes.ROLE_GET)
    @GetMapping("/user/{userId}")
    fun getRolesByUserId(@PathVariable userId: Long): Response<List<RoleVO>> {
        val roles = roleService.getRolesByUserId(userId)
        return Response.success(roles)
    }

    @RequirePermission(PermissionCodes.ROLE_LIST)
    @GetMapping("/check/code")
    fun checkRoleCodeExists(@RequestParam roleCode: String): Response<Boolean> {
        val exists = roleService.checkRoleCodeExists(roleCode)
        return Response.success(exists)
    }
}
