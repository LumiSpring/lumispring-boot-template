package com.lumispring.framework.security.service

import com.baomidou.mybatisplus.extension.service.IService
import com.lumispring.framework.security.model.entity.SysOperationLog

/**
 * 操作日志服务接口
 */
interface OperationLogService : IService<SysOperationLog> {

    /**
     * 记录操作日志
     *
     * @param module 模块
     * @param action 操作类型
     * @param targetId 目标数据ID
     * @param targetName 目标数据名称
     * @param detail 数据快照
     */
    fun log(module: String, action: String, targetId: Long?, targetName: String?, detail: Any?)
}
