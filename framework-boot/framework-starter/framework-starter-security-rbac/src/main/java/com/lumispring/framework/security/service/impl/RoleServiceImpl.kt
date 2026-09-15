package com.lumispring.framework.security.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl
import com.lumispring.framework.base.model.ErrorCode
import com.lumispring.framework.security.mapper.RoleMapper
import com.lumispring.framework.security.mapper.RolePermissionMapper
import com.lumispring.framework.security.mapper.UserRoleMapper
import com.lumispring.framework.security.model.dto.RoleDTO
import com.lumispring.framework.security.model.dto.RoleUpdateDTO
import com.lumispring.framework.security.model.entity.SysRole
import com.lumispring.framework.security.model.entity.SysOperationLog
import com.lumispring.framework.security.model.entity.SysUserRole
import com.lumispring.framework.security.model.vo.RoleVO
import com.lumispring.framework.security.service.RoleService
import com.lumispring.framework.security.service.OperationLogService
import com.lumispring.framework.security.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 角色服务实现类
 */
@Service
class RoleServiceImpl(
    private val roleMapper: RoleMapper,
    private val userRoleMapper: UserRoleMapper,
    private val rolePermissionMapper: RolePermissionMapper,
    private val operationLogService: OperationLogService,
    private val userService: UserService,
) : ServiceImpl<RoleMapper, SysRole>(), RoleService {

    @Transactional(rollbackFor = [Exception::class])
    override fun createRole(roleDTO: RoleDTO): RoleVO {
        if (checkRoleCodeExists(roleDTO.roleCode)) {
            throw ErrorCode.SERVICE_PARAM_ERROR.exception("角色编码已存在")
        }

        val sysRole = SysRole(
            roleName = roleDTO.roleName,
            roleCode = roleDTO.roleCode,
            description = roleDTO.description,
            status = roleDTO.status ?: SysRole.STATUS_ENABLED
        )

        roleMapper.insert(sysRole)
        operationLogService.log(
            module = SysOperationLog.MODULE_ROLE,
            action = SysOperationLog.ACTION_CREATE,
            targetId = sysRole.id,
            targetName = sysRole.roleName,
            detail = sysRole
        )
        return convertToVO(sysRole)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun updateRole(id: Long, roleDTO: RoleUpdateDTO): RoleVO {
        val existingRole = roleMapper.selectById(id)
            ?: throw ErrorCode.RESOURCE_NOT_FIND.exception("角色不存在")

        if (roleDTO.roleCode != null && roleDTO.roleCode != existingRole.roleCode) {
            if (checkRoleCodeExists(roleDTO.roleCode)) {
                throw ErrorCode.SERVICE_PARAM_ERROR.exception("角色编码已存在")
            }
        }

        if (existingRole.roleCode in listOf(SysRole.ROLE_ADMIN, SysRole.ROLE_USER)) {
            if (roleDTO.roleCode != null && roleDTO.roleCode != existingRole.roleCode) {
                throw ErrorCode.SERVICE_PARAM_ERROR.exception("不能修改内置角色的编码")
            }
        }

        if (existingRole.roleCode == SysRole.ROLE_ADMIN && roleDTO.status == SysRole.STATUS_DISABLED) {
            throw ErrorCode.SERVICE_PARAM_ERROR.exception("不能禁用管理员角色")
        }

        val sysRole = SysRole(
            id = id,
            roleName = roleDTO.roleName,
            roleCode = roleDTO.roleCode,
            description = roleDTO.description,
            status = roleDTO.status
        )

        roleMapper.updateById(sysRole)

        val statusChanged = roleDTO.status != null && roleDTO.status != existingRole.status
        val codeChanged = roleDTO.roleCode != null && roleDTO.roleCode != existingRole.roleCode
        if (statusChanged || codeChanged) {
            refreshUsersByRoleId(id)
        }

        return getRoleById(id)
            ?: throw ErrorCode.RESOURCE_NOT_FIND.exception("角色不存在")
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun deleteRole(id: Long) {
        val role = roleMapper.selectById(id)
            ?: throw ErrorCode.RESOURCE_NOT_FIND.exception("角色不存在")

        if (role.roleCode in listOf(SysRole.ROLE_ADMIN, SysRole.ROLE_USER)) {
            throw ErrorCode.SERVICE_PARAM_ERROR.exception("不能删除内置角色")
        }

        val userIds = userRoleMapper.selectUserIdsByRoleId(id)
        rolePermissionMapper.deleteByRoleId(id)
        userRoleMapper.deleteByRoleId(id)

        operationLogService.log(
            module = SysOperationLog.MODULE_ROLE,
            action = SysOperationLog.ACTION_DELETE,
            targetId = role.id,
            targetName = role.roleName,
            detail = role
        )

        roleMapper.deleteById(id)
        userIds.distinct().forEach { userService.refreshUserCache(it) }
    }

    override fun getRoleById(id: Long): RoleVO? {
        val role = roleMapper.selectById(id)
        return role?.let { convertToVO(it) }
    }

    override fun getRoleByCode(roleCode: String): SysRole? {
        return roleMapper.selectByRoleCode(roleCode)
    }

    override fun listAllEnabledRoles(): List<RoleVO> {
        val roles = roleMapper.selectList(
            KtQueryWrapper(SysRole::class.java)
                .eq(SysRole::status, SysRole.STATUS_ENABLED)
                .orderByAsc(SysRole::createTime)
        )
        return roles.map { convertToVO(it) }
    }

    override fun listAllRoles(): List<RoleVO> {
        val roles = roleMapper.selectList(
            KtQueryWrapper(SysRole::class.java)
                .orderByAsc(SysRole::createTime)
        )
        return roles.map { convertToVO(it) }
    }

    override fun getRolesByUserId(userId: Long): List<RoleVO> {
        val roles = roleMapper.selectRolesByUserId(userId)
        return roles.map { convertToVO(it) }
    }

    override fun getRoleCodesByUserId(userId: Long): List<String> {
        return userRoleMapper.selectRoleCodesByUserId(userId)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun assignRolesToUser(userId: Long, roleIds: List<Long>) {
        ensureNotRemovingLastAdmin(userId, roleIds)
        userRoleMapper.deleteByUserId(userId)

        if (roleIds.isNotEmpty()) {
            val userRoles = roleIds.distinct().map { roleId ->
                SysUserRole(
                    userId = userId,
                    roleId = roleId,
                    createTime = LocalDateTime.now()
                )
            }
            userRoles.forEach { userRoleMapper.insert(it) }
        }

        operationLogService.log(
            module = SysOperationLog.MODULE_ROLE,
            action = SysOperationLog.ACTION_ASSIGN,
            targetId = userId,
            targetName = userId.toString(),
            detail = mapOf("roleIds" to roleIds.distinct())
        )
        userService.refreshUserCache(userId)
    }

    override fun checkRoleCodeExists(roleCode: String): Boolean {
        return roleMapper.selectCount(
            KtQueryWrapper(SysRole::class.java)
                .eq(SysRole::roleCode, roleCode)
        ) > 0
    }

    /**
     * 确保不会移除最后一个管理员角色
     */
    private fun ensureNotRemovingLastAdmin(userId: Long, newRoleIds: List<Long>) {
        val adminRole = roleMapper.selectByRoleCode(SysRole.ROLE_ADMIN) ?: return
        val adminRoleId = adminRole.id ?: return
        if (adminRoleId in newRoleIds) return

        val adminUserIds = userRoleMapper.selectUserIdsByRoleId(adminRoleId)
        if (userId in adminUserIds && adminUserIds.none { it != userId }) {
            throw ErrorCode.AUTH_ERROR.exception("不能移除最后一个管理员")
        }
    }

    /**
     * 根据角色ID刷新相关用户缓存
     */
    private fun refreshUsersByRoleId(roleId: Long) {
        userRoleMapper.selectUserIdsByRoleId(roleId).distinct().forEach { userId ->
            userService.refreshUserCache(userId)
        }
    }

    /**
     * 将SysRole转换为RoleVO
     */
    private fun convertToVO(sysRole: SysRole): RoleVO {
        return RoleVO(
            id = sysRole.id,
            roleName = sysRole.roleName,
            roleCode = sysRole.roleCode,
            description = sysRole.description,
            status = sysRole.status,
            createTime = sysRole.createTime,
            updateTime = sysRole.updateTime
        )
    }
}
