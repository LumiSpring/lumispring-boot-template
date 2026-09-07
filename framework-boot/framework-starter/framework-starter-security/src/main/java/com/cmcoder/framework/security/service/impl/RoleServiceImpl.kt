package com.lumispring.framework.security.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.lumispring.framework.base.model.ErrorCode
import com.lumispring.framework.security.mapper.RoleMapper
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 角色服务实现类
 */
@Service
@Transactional(transactionManager = "securityTransactionManager")
class RoleServiceImpl(
    private val roleMapper: RoleMapper,
    private val userRoleMapper: UserRoleMapper,
    private val operationLogService: OperationLogService,
    @Qualifier("securityRedisTemplate")
    private val redisTemplate: RedisTemplate<String, String>,
) : ServiceImpl<RoleMapper, SysRole>(), RoleService {

    @Autowired
    private lateinit var userService: UserService

    override fun createRole(roleDTO: RoleDTO): RoleVO {
        // 检查角色编码是否已存在
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
        return convertToVO(sysRole)
    }

    override fun updateRole(id: Long, roleDTO: RoleUpdateDTO): RoleVO {
        // 查询原角色
        val existingRole = roleMapper.selectById(id)
            ?: throw ErrorCode.RESOURCE_NOT_FIND.exception("角色不存在")

        // 如果修改了角色编码，检查是否与其他角色冲突
        if (roleDTO.roleCode != null && roleDTO.roleCode != existingRole.roleCode) {
            if (checkRoleCodeExists(roleDTO.roleCode)) {
                throw ErrorCode.SERVICE_PARAM_ERROR.exception("角色编码已存在")
            }
        }

        // 不能修改内置角色编码
        if (existingRole.roleCode in listOf(SysRole.ROLE_ADMIN, SysRole.ROLE_USER)) {
            if (roleDTO.roleCode != existingRole.roleCode) {
                throw ErrorCode.SERVICE_PARAM_ERROR.exception("不能修改内置角色的编码")
            }
        }

        val sysRole = SysRole(
            id = id,
            roleName = roleDTO.roleName,
            roleCode = roleDTO.roleCode,
            description = roleDTO.description,
            status = roleDTO.status
        )

        roleMapper.updateById(sysRole)

        return getRoleById(id)
            ?: throw ErrorCode.RESOURCE_NOT_FIND.exception("角色不存在")
    }

    override fun deleteRole(id: Long) {
        val role = roleMapper.selectById(id)
            ?: throw ErrorCode.RESOURCE_NOT_FIND.exception("角色不存在")

        // 不能删除内置角色
        if (role.roleCode in listOf(SysRole.ROLE_ADMIN, SysRole.ROLE_USER)) {
            throw ErrorCode.SERVICE_PARAM_ERROR.exception("不能删除内置角色")
        }

        // 删除角色用户关联
        userRoleMapper.deleteByRoleId(id)

        // 记录操作日志（保存删除前的数据快照）
        operationLogService.log(
            module = SysOperationLog.MODULE_ROLE,
            action = SysOperationLog.ACTION_DELETE,
            targetId = role.id,
            targetName = role.roleName,
            detail = role
        )

        // 物理删除角色
        roleMapper.deleteById(id)
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

    override fun getRolesByUserId(userId: Long): List<RoleVO> {
        val roles = roleMapper.selectRolesByUserId(userId)
        return roles.map { convertToVO(it) }
    }

    override fun getRoleCodesByUserId(userId: Long): List<String> {
        return userRoleMapper.selectRoleCodesByUserId(userId)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun assignRolesToUser(userId: Long, roleIds: List<Long>) {
        // 删除原有角色关联
        userRoleMapper.deleteByUserId(userId)

        // 添加新的角色关联
        if (roleIds.isNotEmpty()) {
            val userRoles = roleIds.map { roleId ->
                SysUserRole(
                    userId = userId,
                    roleId = roleId,
                    createTime = LocalDateTime.now()
                )
            }
            userRoles.forEach { userRoleMapper.insert(it) }
        }

        // 更改缓存中的用户信息
        userService.refreshUserCache(userId)
    }

    override fun checkRoleCodeExists(roleCode: String): Boolean {
        return roleMapper.selectCount(
            KtQueryWrapper(SysRole::class.java)
                .eq(SysRole::roleCode, roleCode)
        ) > 0
    }

    /**
     * 转换为视图对象
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
