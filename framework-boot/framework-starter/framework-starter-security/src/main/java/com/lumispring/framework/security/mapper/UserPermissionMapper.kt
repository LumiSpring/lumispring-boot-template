package com.lumispring.framework.security.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.lumispring.framework.security.model.entity.SysUserPermission
import org.apache.ibatis.annotations.Delete
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.springframework.transaction.annotation.Transactional

/**
 * 用户权限关联数据访问层
 */
@Mapper
@Transactional(transactionManager = "securityTransactionManager")
interface UserPermissionMapper : BaseMapper<SysUserPermission> {

    /**
     * 根据用户ID删除所有权限关联
     *
     * @param userId 用户ID
     * @return 删除数量
     */
    @Delete("DELETE FROM sys_user_permission WHERE user_id = #{userId}")
    fun deleteByUserId(@Param("userId") userId: Long): Long

    /**
     * 根据权限ID删除所有用户关联
     *
     * @param permissionId 权限ID
     * @return 删除数量
     */
    @Delete("DELETE FROM sys_user_permission WHERE permission_id = #{permissionId}")
    fun deleteByPermissionId(@Param("permissionId") permissionId: Long): Long

    /**
     * 根据用户ID查询权限ID列表
     *
     * @param userId 用户ID
     * @return 权限ID列表
     */
    @Select("SELECT permission_id FROM sys_user_permission WHERE user_id = #{userId}")
    fun selectPermissionIdsByUserId(@Param("userId") userId: Long): List<Long>
}
