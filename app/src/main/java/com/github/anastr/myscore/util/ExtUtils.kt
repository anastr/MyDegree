package com.github.anastr.myscore.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

fun Float.formattedScore(): String = DecimalFormat(
    "#.##",
    DecimalFormatSymbols(Locale.ENGLISH)
).format(this)

fun <T> MutableList<T>.swap(i1: Int, i2: Int) {
    this[i1] = this[i2].also { this[i2] = this[i1] }
}
