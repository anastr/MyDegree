package com.github.anastr.myscore.util

import java.util.*

fun Float.formattedScore() = String.format(Locale.ENGLISH, "%.2f", this)

fun <T> MutableList<T>.swap(i1: Int, i2: Int) {
//    this[i1] = this[i2].also { this[i2] = this[i1] }
    val tmp = this[i1]
    this[i1] = this[i2]
    this[i2] = tmp
}

fun Exception.isError403() =
    message == "UNAUTHENTICATED" ||
        message?.contains("Error 403 (Forbidden)") == true
