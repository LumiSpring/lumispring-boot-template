package com.lumispring.framework.security.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.lumispring.framework.security.model.entity.SysUser
import org.apache.ibatis.annotations.Mapper
import org.springframework.transaction.annotation.Transactional

/**
 * 用户数据访问层
 */
@Mapper
@Transactional(transactionManager = "securityTransactionManager")
interface UserMapper : BaseMapper<SysUser>
