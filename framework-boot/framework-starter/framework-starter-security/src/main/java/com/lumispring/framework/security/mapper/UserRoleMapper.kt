package com.lumispring.framework.security.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.lumispring.framework.security.model.entity.SysUserRole
import org.apache.ibatis.annotations.Delete
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.springframework.transaction.annotation.Transactional

/**
 * 用户角色关联数据访问层
 */
@Mapper
@Transactional(transactionManager = "securityTransactionManager")
interface UserRoleMapper : BaseMapper<SysUserRole> {

    /**
     * 根据用户ID查询角色编码列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    @Select("""
        SELECT r.role_code FROM sys_role r
        INNER JOIN sys_user_role ur ON r.id = ur.role_id
        WHERE ur.user_id = #{userId} AND r.status = 1
    """)
    fun selectRoleCodesByUserId(@Param("userId") userId: Long): List<String>

    /**
     * 根据角色编码查询用户ID列表
     *
     * @param roleCode 角色编码
     * @return 用户ID列表
     */
    @Select("""
        SELECT u.id FROM sys_user u
        INNER JOIN sys_user_role ur ON u.id = ur.user_id
        WHERE ur.role_id = (SELECT r.id FROM sys_role r WHERE r.role_code = #{roleCode} AND r.status = 1)
    """)
    fun selectUserIdsByRoleCode(@Param("roleCode") roleCode: String): List<Long>

    /**
     * 根据用户ID删除所有角色关联
     *
     * @param userId 用户ID
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    fun deleteByUserId(@Param("userId") userId: Long): Long

    /**
     * 根据角色ID删除所有用户关联
     *
     * @param roleId 角色ID
     */
    @Delete("DELETE FROM sys_user_role WHERE role_id = #{roleId}")
    fun deleteByRoleId(@Param("roleId") roleId: Long): Long
}
