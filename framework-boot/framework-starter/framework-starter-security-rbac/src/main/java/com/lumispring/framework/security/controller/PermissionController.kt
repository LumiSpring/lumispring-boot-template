package com.lumispring.framework.security.controller

import com.lumispring.framework.base.model.ErrorCode
import com.lumispring.framework.base.model.Response
import com.lumispring.framework.security.config.PermissionCodes
import com.lumispring.framework.security.config.annotation.RequirePermission
import com.lumispring.framework.security.model.dto.PermissionDTO
import com.lumispring.framework.security.model.dto.PermissionQueryDTO
import com.lumispring.framework.security.model.dto.PermissionUpdateDTO
import com.lumispring.framework.security.model.vo.PermissionVO
import com.lumispring.framework.security.service.PermissionService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

/**
 * 权限管理控制器
 */
@RestController
@RequestMapping("/admin/api/permission")
class PermissionController(
    private val permissionService: PermissionService
) {

    @RequirePermission(PermissionCodes.PERMISSION_CREATE)
    @PostMapping
    fun createPermission(@Valid @RequestBody permissionDTO: PermissionDTO): Response<PermissionVO> {
        val permissionVO = permissionService.createPermission(permissionDTO)
        return Response.success(permissionVO)
    }

    @RequirePermission(PermissionCodes.PERMISSION_UPDATE)
    @PutMapping("/{id}")
    fun updatePermission(
        @PathVariable id: Long,
        @Valid @RequestBody permissionDTO: PermissionUpdateDTO
    ): Response<PermissionVO> {
        val permissionVO = permissionService.updatePermission(id, permissionDTO)
        return Response.success(permissionVO)
    }

    @RequirePermission(PermissionCodes.PERMISSION_DELETE)
    @DeleteMapping("/{id}")
    fun deletePermission(@PathVariable id: Long): Response<Boolean> {
        permissionService.deletePermission(id)
        return Response.success(true)
    }

    @RequirePermission(PermissionCodes.PERMISSION_GET)
    @GetMapping("/{id}")
    fun getPermissionById(@PathVariable id: Long): Response<PermissionVO> {
        val permissionVO = permissionService.getPermissionById(id)
            ?: throw ErrorCode.RESOURCE_NOT_FIND.exception("权限不存在")
        return Response.success(permissionVO)
    }

    @RequirePermission(PermissionCodes.PERMISSION_LIST)
    @GetMapping("/list")
    fun listPermissions(queryDTO: PermissionQueryDTO): Response<List<PermissionVO>> {
        val permissions = permissionService.listPermissions(queryDTO)
        return Response.success(permissions)
    }

    @RequirePermission(PermissionCodes.PERMISSION_TREE)
    @GetMapping("/tree")
    fun getPermissionTree(): Response<List<PermissionVO>> {
        val tree = permissionService.getPermissionTree()
        return Response.success(tree)
    }

    @RequirePermission(PermissionCodes.PERMISSION_LIST)
    @GetMapping("/enabled")
    fun listAllEnabledPermissions(): Response<List<PermissionVO>> {
        val permissions = permissionService.listAllEnabledPermissions()
        return Response.success(permissions)
    }

    @RequirePermission(PermissionCodes.PERMISSION_GET)
    @GetMapping("/role/{roleId}")
    fun getPermissionsByRoleId(@PathVariable roleId: Long): Response<List<PermissionVO>> {
        val permissions = permissionService.getPermissionsByRoleId(roleId)
        return Response.success(permissions)
    }

    @RequirePermission(PermissionCodes.PERMISSION_GET)
    @GetMapping("/user/{userId}")
    fun getPermissionsByUserId(@PathVariable userId: Long): Response<List<PermissionVO>> {
        val permissions = permissionService.getPermissionsByUserId(userId)
        return Response.success(permissions)
    }

    @RequirePermission(PermissionCodes.PERMISSION_GET)
    @GetMapping("/user/{userId}/codes")
    fun getPermissionCodesByUserId(@PathVariable userId: Long): Response<List<String>> {
        val codes = permissionService.getPermissionCodesByUserId(userId)
        return Response.success(codes)
    }

    @RequirePermission(PermissionCodes.PERMISSION_ASSIGN_ROLE)
    @PostMapping("/assign/role/{roleId}")
    fun assignPermissionsToRole(
        @PathVariable roleId: Long,
        @RequestBody permissionIds: List<Long>
    ): Response<Boolean> {
        permissionService.assignPermissionsToRole(roleId, permissionIds)
        return Response.success(true)
    }

    @RequirePermission(PermissionCodes.PERMISSION_ASSIGN_USER)
    @PostMapping("/assign/user/{userId}")
    fun assignPermissionsToUser(
        @PathVariable userId: Long,
        @RequestBody permissionIds: List<Long>
    ): Response<Boolean> {
        permissionService.assignPermissionsToUser(userId, permissionIds)
        return Response.success(true)
    }

    @RequirePermission(PermissionCodes.PERMISSION_LIST)
    @GetMapping("/check/code")
    fun checkPermissionCodeExists(@RequestParam code: String): Response<Boolean> {
        val exists = permissionService.checkPermissionCodeExists(code)
        return Response.success(exists)
    }

    @RequirePermission(PermissionCodes.PERMISSION_GET)
    @GetMapping("/check/user")
    fun checkUserHasPermission(
        @RequestParam userId: Long,
        @RequestParam code: String
    ): Response<Boolean> {
        val hasPermission = permissionService.hasPermission(userId, code)
        return Response.success(hasPermission)
    }
}
