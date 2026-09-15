package com.lumispring.framework.security.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl
import com.lumispring.framework.security.mapper.UserRoleMapper
import com.lumispring.framework.security.model.entity.SysUserRole
import com.lumispring.framework.security.service.UserRoleService
import com.lumispring.framework.security.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserRoleServiceImpl(
    private val userService: UserService,
) : ServiceImpl<UserRoleMapper, SysUserRole>(), UserRoleService {

    @Transactional(rollbackFor = [Exception::class])
    override fun addRoleToUser(roleId: Long, userId: Long): Boolean {
        return saveOrUpdate(SysUserRole(userId = userId, roleId = roleId)).also {
            userService.refreshUserCache(userId)
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun removeRoleFromUser(roleId: Long, userId: Long): Boolean {
        val deleted = baseMapper.delete(
            KtQueryWrapper(SysUserRole::class.java)
                .eq(SysUserRole::userId, userId)
                .eq(SysUserRole::roleId, roleId)
        ) > 0
        if (deleted) {
            userService.refreshUserCache(userId)
        }
        return deleted
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun resetRolesToUser(roleIds: List<Long>, userId: Long): Boolean {
        baseMapper.deleteByUserId(userId)
        if (roleIds.isEmpty()) {
            userService.refreshUserCache(userId)
            return true
        }
        return saveOrUpdateBatch(
            roleIds.distinct().map { SysUserRole(userId = userId, roleId = it) },
        ).also {
            userService.refreshUserCache(userId)
        }
    }
}
