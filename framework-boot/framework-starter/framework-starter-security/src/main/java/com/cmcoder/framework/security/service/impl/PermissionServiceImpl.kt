package com.lumispring.framework.security.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.lumispring.framework.base.model.ErrorCode
import com.lumispring.framework.security.mapper.PermissionMapper
import com.lumispring.framework.security.mapper.RolePermissionMapper
import com.lumispring.framework.security.mapper.UserPermissionMapper
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
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 权限服务实现类
 */
@Service
@Transactional(transactionManager = "securityTransactionManager")
class PermissionServiceImpl(
    private val permissionMapper: PermissionMapper,
    private val rolePermissionMapper: RolePermissionMapper,
    private val userPermissionMapper: UserPermissionMapper,
    private val operationLogService: OperationLogService
) : ServiceImpl<PermissionMapper, SysPermission>(), PermissionService {

    override fun createPermission(permissionDTO: PermissionDTO): PermissionVO {
        // 检查权限编码是否已存在
        if (checkPermissionCodeExists(permissionDTO.code)) {
            throw ErrorCode.SERVICE_PARAM_ERROR.exception("权限编码已存在")
        }

        // 校验权限类型
        validatePermissionType(permissionDTO.type)

        // 如果指定了父级权限，校验父级是否存在
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
        return convertToVO(sysPermission)
    }

    override fun updatePermission(id: Long, permissionDTO: PermissionUpdateDTO): PermissionVO {
        // 查询原权限
        val existingPermission = permissionMapper.selectById(id)
            ?: throw ErrorCode.RESOURCE_NOT_FIND.exception("权限不存在")

        // 如果修改了权限编码，检查是否与其他权限冲突
        if (permissionDTO.code != null && permissionDTO.code != existingPermission.code) {
            if (checkPermissionCodeExists(permissionDTO.code)) {
                throw ErrorCode.SERVICE_PARAM_ERROR.exception("权限编码已存在")
            }
        }

        // 校验权限类型
        permissionDTO.type?.let { validatePermissionType(it) }

        // 如果修改了父级权限，校验父级是否存在且不能将自己设为自己的子级
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

        return getPermissionById(id)
            ?: throw ErrorCode.RESOURCE_NOT_FIND.exception("权限不存在")
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun deletePermission(id: Long) {
        val permission = permissionMapper.selectById(id)
            ?: throw ErrorCode.RESOURCE_NOT_FIND.exception("权限不存在")

        // 检查是否有子权限
        val childCount = permissionMapper.selectCount(
            KtQueryWrapper(SysPermission::class.java)
                .eq(SysPermission::parentId, id)
        )
        if (childCount > 0) {
            throw ErrorCode.SERVICE_PARAM_ERROR.exception("存在子权限，不能删除")
        }

        // 删除角色权限关联
        rolePermissionMapper.deleteByPermissionId(id)

        // 删除用户权限关联
        userPermissionMapper.deleteByPermissionId(id)

        // 记录操作日志（保存删除前的数据快照）
        operationLogService.log(
            module = SysOperationLog.MODULE_PERMISSION,
            action = SysOperationLog.ACTION_DELETE,
            targetId = permission.id,
            targetName = permission.name,
            detail = permission
        )

        // 物理删除权限
        permissionMapper.deleteById(id)
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
            // 名称模糊查询
            queryDTO.name?.takeIf { it.isNotBlank() }?.let {
                like(SysPermission::name, it)
            }

            // 编码模糊查询
            queryDTO.code?.takeIf { it.isNotBlank() }?.let {
                like(SysPermission::code, it)
            }

            // 类型精确查询
            queryDTO.type?.takeIf { it.isNotBlank() }?.let {
                eq(SysPermission::type, it)
            }

            // 父级ID精确查询
            queryDTO.parentId?.let {
                eq(SysPermission::parentId, it)
            }

            // 状态精确查询
            queryDTO.status?.let {
                eq(SysPermission::status, it)
            }

            // 按创建时间升序排列
            orderByAsc(SysPermission::createTime)
        }

        val permissions = permissionMapper.selectList(queryWrapper)
        return permissions.map { convertToVO(it) }
    }

    override fun getPermissionTree(): List<PermissionVO> {
        // 查询所有启用的权限
        val allPermissions = permissionMapper.selectList(
            KtQueryWrapper(SysPermission::class.java)
                .eq(SysPermission::status, SysPermission.STATUS_ENABLED)
                .orderByAsc(SysPermission::createTime)
        )

        // 构建树形结构
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
        // 通过角色获取的权限
        val rolePermissions = permissionMapper.selectPermissionsByUserId(userId)

        // 直接分配的权限
        val directPermissions = permissionMapper.selectDirectPermissionsByUserId(userId)

        // 合并去重
        val allPermissions = (rolePermissions + directPermissions)
            .distinctBy { it.id }
            .sortedBy { it.createTime }

        return allPermissions.map { convertToVO(it) }
    }

    override fun getPermissionCodesByUserId(userId: Long): List<String> {
        return getPermissionsByUserId(userId).mapNotNull { it.code }.distinct()
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun assignPermissionsToRole(roleId: Long, permissionIds: List<Long>) {
        // 删除原有权限关联
        rolePermissionMapper.deleteByRoleId(roleId)

        // 添加新的权限关联
        if (permissionIds.isNotEmpty()) {
            val rolePermissions = permissionIds.map { permissionId ->
                SysRolePermission(
                    roleId = roleId,
                    permissionId = permissionId
                )
            }
            rolePermissionMapper.insert(rolePermissions)
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun assignPermissionsToUser(userId: Long, permissionIds: List<Long>) {
        // 删除原有权限关联
        userPermissionMapper.deleteByUserId(userId)

        // 添加新的权限关联
        if (permissionIds.isNotEmpty()) {
            val userPermissions = permissionIds.map { permissionId ->
                SysUserPermission(
                    userId = userId,
                    permissionId = permissionId
                )
            }
            userPermissionMapper.insert(userPermissions)
        }
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

    /**
     * 校验权限类型是否合法
     */
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

    /**
     * 构建权限树
     */
    private fun buildTree(permissions: List<PermissionVO>, parentId: Long? = null): List<PermissionVO> {
        return permissions.filter { it.parentId == parentId }
            .map { permission ->
                permission.copy(
                    children = buildTree(permissions, permission.id)
                )
            }
    }

    /**
     * 转换为视图对象
     */
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
