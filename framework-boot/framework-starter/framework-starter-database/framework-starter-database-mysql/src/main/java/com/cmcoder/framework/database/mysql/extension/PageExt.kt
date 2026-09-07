package com.lumispring.framework.database.mysql.extension

import com.baomidou.mybatisplus.core.metadata.IPage
import com.lumispring.framework.base.model.Response

/**
 * 分页转换
 */
fun <T> IPage<T>.toResponse():Response<List<T>>{
    return Response.success(
        data = this.records,
        total = this.total,
        pageNum = this.current,
        pageSize = this.size,
        pages = this.pages
    )
}