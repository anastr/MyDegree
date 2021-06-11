package com.github.anastr.myscore.util

import androidx.annotation.WorkerThread
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
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

@WorkerThread
fun <TResult> Task<TResult>.await(): TResult = Tasks.await(this)

fun HashMap<String, Any>.getString(key: String) = get(key) as String
fun HashMap<String, Any>.getBoolean(key: String) = get(key) as Boolean
fun HashMap<String, Any>.getInt(key: String) = (get(key) as Number).toInt()
fun HashMap<String, Any>.getLong(key: String) = (get(key) as Number).toLong()

fun Exception.isError403() =
    message == "UNAUTHENTICATED" ||
        message?.contains("Error 403 (Forbidden)") == true
