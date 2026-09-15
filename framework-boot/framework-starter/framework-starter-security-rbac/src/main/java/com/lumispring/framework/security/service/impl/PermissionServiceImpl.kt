package com.lumispring.framework.security.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl
import com.lumispring.framework.base.model.ErrorCode
import com.lumispring.framework.security.mapper.PermissionMapper
import com.lumispring.framework.security.mapper.RolePermissionMapper
import com.lumispring.framework.security.mapper.UserPermissionMapper
import com.lumispring.framework.security.mapper.UserRoleMapper
import com.lumispring.framework.security.model.dto.PermissionDTO
import com.lumispring.framework.security.model.dto.PermissionQueryDTO
import com.lumispring.framework.security.model.dto.PermissionUpdateDTO
import com.lumispring.framework.security.model.entity.SysPermission
import com.lumispring.framework.security.model.entity.SysOperationLog
import com.lumispring.framework.security.model.entity.SysRolePermission
import com.lumispring.framework.security.model.entity.SysUserPermission
import com.lumispring.framework.security.model.vo.PermissionVO
import com.lumispring.framework.security.service.PermissionService
import com.lumispring.framework.security.service.OperationLogService
import com.lumispring.framework.security.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 权限服务实现类
 */
@Service
class PermissionServiceImpl(
    private val permissionMapper: PermissionMapper,
    private val rolePermissionMapper: RolePermissionMapper,
    private val userPermissionMapper: UserPermissionMapper,
    private val userRoleMapper: UserRoleMapper,
    private val operationLogService: OperationLogService,
    private val userService: UserService,
) : ServiceImpl<PermissionMapper, SysPermission>(), PermissionService {

    @Transactional(rollbackFor = [Exception::class])
    override fun createPermission(permissionDTO: PermissionDTO): PermissionVO {
        if (checkPermissionCodeExists(permissionDTO.code)) {
            throw ErrorCode.SERVICE_PARAM_ERROR.exception("权限编码已存在")
        }

        validatePermissionType(permissionDTO.type)

        permissionDTO.parentId?.let { parentId ->
            permissionMapper.selectById(parentId)
                ?: throw ErrorCode.RESOURCE_NOT_FIND.exception("父级权限不存在")
        }

        val sysPermission = SysPermission(
            name = permissionDTO.name,
            code = permissionDTO.code,
            path = permissionDTO.path,
            parentId = permissionDTO.parentId,
            type = permissionDTO.type,
            component = permissionDTO.component,
            description = permissionDTO.description,
            status = permissionDTO.status ?: SysPermission.STATUS_ENABLED
        )

        permissionMapper.insert(sysPermission)
        operationLogService.log(
            module = SysOperationLog.MODULE_PERMISSION,
            action = SysOperationLog.ACTION_CREATE,
            targetId = sysPermission.id,
            targetName = sysPermission.name,
            detail = sysPermission
        )
        return convertToVO(sysPermission)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun updatePermission(id: Long, permissionDTO: PermissionUpdateDTO): PermissionVO {
        val existingPermission = permissionMapper.selectById(id)
            ?: throw ErrorCode.RESOURCE_NOT_FIND.exception("权限不存在")

        if (permissionDTO.code != null && permissionDTO.code != existingPermission.code) {
            if (checkPermissionCodeExists(permissionDTO.code)) {
                throw ErrorCode.SERVICE_PARAM_ERROR.exception("权限编码已存在")
            }
        }

        permissionDTO.type?.let { validatePermissionType(it) }

        permissionDTO.parentId?.let { parentId ->
            if (parentId == id) {
                throw ErrorCode.SERVICE_PARAM_ERROR.exception("不能将自己设为父级权限")
            }
            permissionMapper.selectById(parentId)
                ?: throw ErrorCode.RESOURCE_NOT_FIND.exception("父级权限不存在")
        }

        val sysPermission = SysPermission(
            id = id,
            name = permissionDTO.name,
            code = permissionDTO.code,
            path = permissionDTO.path,
            parentId = permissionDTO.parentId,
            type = permissionDTO.type,
            component = permissionDTO.component,
            description = permissionDTO.description,
            status = permissionDTO.status
        )

        permissionMapper.updateById(sysPermission)

        val authChanged = (permissionDTO.code != null && permissionDTO.code != existingPermission.code)
            || (permissionDTO.status != null && permissionDTO.status != existingPermission.status)
        if (authChanged) {
            refreshUsersByPermissionId(id)
        }

        return getPermissionById(id)
            ?: throw ErrorCode.RESOURCE_NOT_FIND.exception("权限不存在")
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun deletePermission(id: Long) {
        val permission = permissionMapper.selectById(id)
            ?: throw ErrorCode.RESOURCE_NOT_FIND.exception("权限不存在")

        val childCount = permissionMapper.selectCount(
            KtQueryWrapper(SysPermission::class.java)
                .eq(SysPermission::parentId, id)
        )
        if (childCount > 0) {
            throw ErrorCode.SERVICE_PARAM_ERROR.exception("存在子权限，不能删除")
        }

        val affectedUserIds = collectUserIdsByPermissionId(id)
        rolePermissionMapper.deleteByPermissionId(id)
        userPermissionMapper.deleteByPermissionId(id)

        operationLogService.log(
            module = SysOperationLog.MODULE_PERMISSION,
            action = SysOperationLog.ACTION_DELETE,
            targetId = permission.id,
            targetName = permission.name,
            detail = permission
        )

        permissionMapper.deleteById(id)
        affectedUserIds.forEach { userService.refreshUserCache(it) }
    }

    override fun getPermissionById(id: Long): PermissionVO? {
        val permission = permissionMapper.selectById(id)
        return permission?.let { convertToVO(it) }
    }

    override fun getPermissionByCode(code: String): SysPermission? {
        return permissionMapper.selectByCode(code)
    }

    override fun listPermissions(queryDTO: PermissionQueryDTO): List<PermissionVO> {
        val queryWrapper = KtQueryWrapper(SysPermission::class.java).apply {
            queryDTO.name?.takeIf { it.isNotBlank() }?.let {
                like(SysPermission::name, it)
            }
            queryDTO.code?.takeIf { it.isNotBlank() }?.let {
                like(SysPermission::code, it)
            }
            queryDTO.type?.takeIf { it.isNotBlank() }?.let {
                eq(SysPermission::type, it)
            }
            queryDTO.parentId?.let {
                eq(SysPermission::parentId, it)
            }
            queryDTO.status?.let {
                eq(SysPermission::status, it)
            }
            orderByAsc(SysPermission::createTime)
        }

        val permissions = permissionMapper.selectList(queryWrapper)
        return permissions.map { convertToVO(it) }
    }

    override fun getPermissionTree(): List<PermissionVO> {
        val allPermissions = permissionMapper.selectList(
            KtQueryWrapper(SysPermission::class.java)
                .eq(SysPermission::status, SysPermission.STATUS_ENABLED)
                .orderByAsc(SysPermission::createTime)
        )
        return buildTree(allPermissions.map { convertToVO(it) })
    }

    override fun listAllEnabledPermissions(): List<PermissionVO> {
        val permissions = permissionMapper.selectList(
            KtQueryWrapper(SysPermission::class.java)
                .eq(SysPermission::status, SysPermission.STATUS_ENABLED)
                .orderByAsc(SysPermission::createTime)
        )
        return permissions.map { convertToVO(it) }
    }

    override fun getPermissionsByRoleId(roleId: Long): List<PermissionVO> {
        val permissions = permissionMapper.selectPermissionsByRoleId(roleId)
        return permissions.map { convertToVO(it) }
    }

    override fun getPermissionsByUserId(userId: Long): List<PermissionVO> {
        val rolePermissions = permissionMapper.selectPermissionsByUserId(userId)
        val directPermissions = permissionMapper.selectDirectPermissionsByUserId(userId)
        val allPermissions = (rolePermissions + directPermissions)
            .distinctBy { it.id }
            .sortedBy { it.createTime }
        return allPermissions.map { convertToVO(it) }
    }

    override fun getPermissionCodesByUserId(userId: Long): List<String> {
        return permissionMapper.selectPermissionCodesByUserId(userId)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun assignPermissionsToRole(roleId: Long, permissionIds: List<Long>) {
        rolePermissionMapper.deleteByRoleId(roleId)

        if (permissionIds.isNotEmpty()) {
            permissionIds.distinct().forEach { permissionId ->
                rolePermissionMapper.insert(
                    SysRolePermission(
                        roleId = roleId,
                        permissionId = permissionId
                    )
                )
            }
        }

        operationLogService.log(
            module = SysOperationLog.MODULE_PERMISSION,
            action = SysOperationLog.ACTION_ASSIGN,
            targetId = roleId,
            targetName = roleId.toString(),
            detail = mapOf("roleId" to roleId, "permissionIds" to permissionIds.distinct())
        )
        refreshUsersByRoleId(roleId)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun assignPermissionsToUser(userId: Long, permissionIds: List<Long>) {
        userPermissionMapper.deleteByUserId(userId)

        if (permissionIds.isNotEmpty()) {
            permissionIds.distinct().forEach { permissionId ->
                userPermissionMapper.insert(
                    SysUserPermission(
                        userId = userId,
                        permissionId = permissionId
                    )
                )
            }
        }

        operationLogService.log(
            module = SysOperationLog.MODULE_PERMISSION,
            action = SysOperationLog.ACTION_ASSIGN,
            targetId = userId,
            targetName = userId.toString(),
            detail = mapOf("userId" to userId, "permissionIds" to permissionIds.distinct())
        )
        userService.refreshUserCache(userId)
    }

    override fun checkPermissionCodeExists(code: String): Boolean {
        return permissionMapper.selectCount(
            KtQueryWrapper(SysPermission::class.java)
                .eq(SysPermission::code, code)
        ) > 0
    }

    override fun hasPermission(userId: Long, code: String): Boolean {
        return getPermissionCodesByUserId(userId).contains(code)
    }

    private fun validatePermissionType(type: String) {
        val validTypes = listOf(
            SysPermission.TYPE_API,
            SysPermission.TYPE_MENU,
            SysPermission.TYPE_BUTTON
        )
        if (type !in validTypes) {
            throw ErrorCode.SERVICE_PARAM_ERROR.exception("权限类型不合法，可选值：api、menu、button")
        }
    }

    private fun collectUserIdsByPermissionId(permissionId: Long): Set<Long> {
        val userIds = mutableSetOf<Long>()
        userIds += userPermissionMapper.selectUserIdsByPermissionId(permissionId)
        rolePermissionMapper.selectRoleIdsByPermissionId(permissionId).forEach { roleId ->
            userIds += userRoleMapper.selectUserIdsByRoleId(roleId)
        }
        return userIds
    }

    private fun refreshUsersByPermissionId(permissionId: Long) {
        collectUserIdsByPermissionId(permissionId).forEach { userService.refreshUserCache(it) }
    }

    private fun refreshUsersByRoleId(roleId: Long) {
        userRoleMapper.selectUserIdsByRoleId(roleId).distinct().forEach { userId ->
            userService.refreshUserCache(userId)
        }
    }

    private fun buildTree(permissions: List<PermissionVO>, parentId: Long? = null): List<PermissionVO> {
        return permissions.filter { it.parentId == parentId }
            .map { permission ->
                permission.copy(
                    children = buildTree(permissions, permission.id)
                )
            }
    }

    private fun convertToVO(sysPermission: SysPermission): PermissionVO {
        return PermissionVO(
            id = sysPermission.id,
            name = sysPermission.name,
            code = sysPermission.code,
            path = sysPermission.path,
            parentId = sysPermission.parentId,
            type = sysPermission.type,
            component = sysPermission.component,
            description = sysPermission.description,
            status = sysPermission.status,
            createTime = sysPermission.createTime,
            updateTime = sysPermission.updateTime
        )
    }
}
