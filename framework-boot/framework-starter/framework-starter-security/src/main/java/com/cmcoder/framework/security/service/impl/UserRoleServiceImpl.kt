package com.lumispring.framework.security.service.impl

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.lumispring.framework.security.mapper.UserRoleMapper
import com.lumispring.framework.security.model.entity.SysUserRole
import com.lumispring.framework.security.service.UserRoleService
import com.lumispring.framework.security.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(transactionManager = "securityTransactionManager")
class UserRoleServiceImpl : ServiceImpl<UserRoleMapper, SysUserRole>(), UserRoleService {
    @Autowired
    private lateinit var userService: UserService

    override fun addRoleToUser(roleId: Long, userId: Long): Boolean {
        return saveOrUpdate(SysUserRole(userId = userId, roleId = roleId)).also {
            userService.refreshUserCache(userId)
        }
    }

    override fun removeRoleFromUser(roleId: Long, userId: Long) : Boolean{
        return (baseMapper.deleteById(roleId) > 0).also {
            userService.refreshUserCache(userId)
        }
    }

    override fun resetRolesToUser(roleIds: List<Long>, userId: Long): Boolean {
        if (baseMapper.deleteByUserId(userId) <= 0) return false
        return saveOrUpdateBatch(
            roleIds.map { SysUserRole(userId = userId, roleId = it) },
        ).also {
            userService.refreshUserCache(userId)
        }
    }
}