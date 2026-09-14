package com.lumispring.framework.base.extension

import java.math.BigDecimal

fun BigDecimal.toPercent(scale: Int = 2): String {
    return "${(this * BigDecimal(100)).setScale(scale,  BigDecimal.ROUND_HALF_UP)}%"
}