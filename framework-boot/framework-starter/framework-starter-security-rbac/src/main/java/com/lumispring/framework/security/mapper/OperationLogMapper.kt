package com.lumispring.framework.security.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.lumispring.framework.security.model.entity.SysOperationLog
import org.apache.ibatis.annotations.Mapper

/**
 * 操作日志数据访问层
 */
@Mapper
interface OperationLogMapper : BaseMapper<SysOperationLog>
