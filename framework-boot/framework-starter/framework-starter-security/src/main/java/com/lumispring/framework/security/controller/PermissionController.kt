package com.lumispring.framework.security.controller

import com.lumispring.framework.base.model.ErrorCode
import com.lumispring.framework.base.model.Response
import com.lumispring.framework.security.config.annotation.RequireAdmin
import com.lumispring.framework.security.model.dto.PermissionDTO
import com.lumispring.framework.security.model.dto.PermissionQueryDTO
import com.lumispring.framework.security.model.dto.PermissionUpdateDTO
import com.lumispring.framework.security.model.vo.PermissionVO
import com.lumispring.framework.security.service.PermissionService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

/**
 * 权限管理控制器
 * 提供权限的增删改查接口
 */
@RestController
@RequestMapping("/admin/api/permission")
@RequireAdmin
class PermissionController(
    private val permissionService: PermissionService
) {

    /**
     * 创建权限
     *
     * @param permissionDTO 权限信息
     * @return 创建后的权限
     */
    @PostMapping
    fun createPermission(@Valid @RequestBody permissionDTO: PermissionDTO): Response<PermissionVO> {
        val permissionVO = permissionService.createPermission(permissionDTO)
        return Response.success(permissionVO)
    }

    /**
     * 更新权限
     *
     * @param id 权限ID
     * @param permissionDTO 权限信息
     * @return 更新后的权限
     */
    @PutMapping("/{id}")
    fun updatePermission(
        @PathVariable id: Long,
        @Valid @RequestBody permissionDTO: PermissionUpdateDTO
    ): Response<PermissionVO> {
        val permissionVO = permissionService.updatePermission(id, permissionDTO)
        return Response.success(permissionVO)
    }

    /**
     * 删除权限
     *
     * @param id 权限ID
     */
    @DeleteMapping("/{id}")
    fun deletePermission(@PathVariable id: Long): Response<Boolean> {
        permissionService.deletePermission(id)
        return Response.success(true)
    }

    /**
     * 根据ID查询权限
     *
     * @param id 权限ID
     * @return 权限信息
     */
    @GetMapping("/{id}")
    fun getPermissionById(@PathVariable id: Long): Response<PermissionVO> {
        val permissionVO = permissionService.getPermissionById(id)
            ?: throw ErrorCode.SERVICE_ERROR.exception("权限不存在")
        return Response.success(permissionVO)
    }

    /**
     * 条件查询权限列表
     *
     * @param queryDTO 查询条件
     * @return 权限列表
     */
    @GetMapping("/list")
    fun listPermissions(queryDTO: PermissionQueryDTO): Response<List<PermissionVO>> {
        val permissions = permissionService.listPermissions(queryDTO)
        return Response.success(permissions)
    }

    /**
     * 查询权限树形结构
     *
     * @return 权限树
     */
    @GetMapping("/tree")
    fun getPermissionTree(): Response<List<PermissionVO>> {
        val tree = permissionService.getPermissionTree()
        return Response.success(tree)
    }

    /**
     * 查询所有启用的权限
     *
     * @return 权限列表
     */
    @GetMapping("/enabled")
    fun listAllEnabledPermissions(): Response<List<PermissionVO>> {
        val permissions = permissionService.listAllEnabledPermissions()
        return Response.success(permissions)
    }

    /**
     * 根据角色ID查询权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @GetMapping("/role/{roleId}")
    fun getPermissionsByRoleId(@PathVariable roleId: Long): Response<List<PermissionVO>> {
        val permissions = permissionService.getPermissionsByRoleId(roleId)
        return Response.success(permissions)
    }

    /**
     * 根据用户ID查询权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @GetMapping("/user/{userId}")
    fun getPermissionsByUserId(@PathVariable userId: Long): Response<List<PermissionVO>> {
        val permissions = permissionService.getPermissionsByUserId(userId)
        return Response.success(permissions)
    }

    /**
     * 根据用户ID查询权限编码列表
     *
     * @param userId 用户ID
     * @return 权限编码列表
     */
    @GetMapping("/user/{userId}/codes")
    fun getPermissionCodesByUserId(@PathVariable userId: Long): Response<List<String>> {
        val codes = permissionService.getPermissionCodesByUserId(userId)
        return Response.success(codes)
    }

    /**
     * 为角色分配权限
     *
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    @PostMapping("/assign/role/{roleId}")
    fun assignPermissionsToRole(
        @PathVariable roleId: Long,
        @RequestBody permissionIds: List<Long>
    ): Response<Boolean> {
        permissionService.assignPermissionsToRole(roleId, permissionIds)
        return Response.success(true)
    }

    /**
     * 为用户直接分配权限
     *
     * @param userId 用户ID
     * @param permissionIds 权限ID列表
     */
    @PostMapping("/assign/user/{userId}")
    fun assignPermissionsToUser(
        @PathVariable userId: Long,
        @RequestBody permissionIds: List<Long>
    ): Response<Boolean> {
        permissionService.assignPermissionsToUser(userId, permissionIds)
        return Response.success(true)
    }

    /**
     * 检查权限编码是否存在
     *
     * @param code 权限编码
     * @return true-存在，false-不存在
     */
    @GetMapping("/check/code")
    fun checkPermissionCodeExists(@RequestParam code: String): Response<Boolean> {
        val exists = permissionService.checkPermissionCodeExists(code)
        return Response.success(exists)
    }

    /**
     * 检查用户是否拥有指定权限
     *
     * @param userId 用户ID
     * @param code 权限编码
     * @return true-拥有，false-没有
     */
    @GetMapping("/check/user")
    fun checkUserHasPermission(
        @RequestParam userId: Long,
        @RequestParam code: String
    ): Response<Boolean> {
        val hasPermission = permissionService.hasPermission(userId, code)
        return Response.success(hasPermission)
    }
}
