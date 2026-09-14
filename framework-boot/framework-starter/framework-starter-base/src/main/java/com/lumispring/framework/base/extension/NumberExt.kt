package com.lumispring.framework.base.extension

import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal.toPercent(scale: Int = 2): String {
    return "${(this * BigDecimal(100)).setScale(scale, RoundingMode.HALF_UP)}%"
}