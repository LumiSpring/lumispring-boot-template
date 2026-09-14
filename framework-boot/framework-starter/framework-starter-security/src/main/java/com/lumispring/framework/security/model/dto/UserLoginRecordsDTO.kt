package com.lumispring.framework.security.model.dto

import com.lumispring.framework.security.model.vo.UserVO

data class UserLoginRecordsDTO(
    var userId: Long? = null,
    var userInfo: UserVO? = null,
    var records: Map<String, LoginRecordDTO?>? = null,
)