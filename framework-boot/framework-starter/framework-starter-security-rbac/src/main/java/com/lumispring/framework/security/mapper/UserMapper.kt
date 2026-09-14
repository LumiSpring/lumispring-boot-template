package com.lumispring.framework.security.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.lumispring.framework.security.model.entity.SysUser
import org.apache.ibatis.annotations.Mapper

/**
 * 用户数据访问层
 */
@Mapper
interface UserMapper : BaseMapper<SysUser>
