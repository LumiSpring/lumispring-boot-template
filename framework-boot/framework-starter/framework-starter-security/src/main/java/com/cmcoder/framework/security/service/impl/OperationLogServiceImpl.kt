package com.lumispring.framework.security.service.impl

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.lumispring.framework.base.extension.toJsonString
import com.lumispring.framework.security.extension.currentUserId
import com.lumispring.framework.security.extension.currentUsername
import com.lumispring.framework.security.mapper.OperationLogMapper
import com.lumispring.framework.security.model.entity.SysOperationLog
import com.lumispring.framework.security.service.OperationLogService
import com.lumispring.framework.web.extension.reqGetIp
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 操作日志服务实现类
 */
@Service
@Transactional(transactionManager = "securityTransactionManager")
class OperationLogServiceImpl(
    private val operationLogMapper: OperationLogMapper
) : ServiceImpl<OperationLogMapper, SysOperationLog>(), OperationLogService {

    private val logger = LoggerFactory.getLogger(OperationLogServiceImpl::class.java)

    override fun log(module: String, action: String, targetId: Long?, targetName: String?, detail: Any?) {
        try {
            val operationLog = SysOperationLog(
                operatorId = currentUserId(),
                operatorName = currentUsername(),
                module = module,
                action = action,
                targetId = targetId,
                targetName = targetName,
                detail = detail?.toJsonString(),
                ip = reqGetIp()
            )
            operationLogMapper.insert(operationLog)
        } catch (e: Exception) {
            // 日志记录失败不应影响主业务流程
            logger.error("记录操作日志失败: module=$module, action=$action, targetId=$targetId", e)
        }
    }
}
