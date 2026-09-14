package com.lumispring.framework.security.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.lumispring.framework.security.model.entity.SysOperationLog
import org.apache.ibatis.annotations.Mapper
import org.springframework.transaction.annotation.Transactional

/**
 * 操作日志数据访问层
 */
@Mapper
@Transactional(transactionManager = "securityTransactionManager")
interface OperationLogMapper : BaseMapper<SysOperationLog>
