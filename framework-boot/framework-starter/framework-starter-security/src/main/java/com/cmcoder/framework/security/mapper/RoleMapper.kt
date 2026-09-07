package com.lumispring.framework.security.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.lumispring.framework.security.model.entity.SysRole
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.springframework.transaction.annotation.Transactional

/**
 * 角色数据访问层
 */
@Mapper
@Transactional(transactionManager = "securityTransactionManager")
interface RoleMapper : BaseMapper<SysRole> {

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @Select("""
        SELECT r.* FROM sys_role r
        INNER JOIN sys_user_role ur ON r.id = ur.role_id
        WHERE ur.user_id = #{userId} AND r.status = 1
    """)
    fun selectRolesByUserId(@Param("userId") userId: Long): List<SysRole>

    /**
     * 根据角色编码查询角色
     *
     * @param roleCode 角色编码
     * @return 角色
     */
    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode}")
    fun selectByRoleCode(@Param("roleCode") roleCode: String): SysRole?
}
