package com.github.anastr.myscore.util

import android.content.SharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


@ExperimentalCoroutinesApi
private fun <T> SharedPreferences.internalFlow(
    key: String,
    getObj: SharedPreferences.() -> T
): Flow<T> = callbackFlow {
    // Send the current value.
    offer(getObj(this@internalFlow))
    val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
        if (changedKey == key) {
            offer(getObj(this@internalFlow))
        }
    }
    // Send new value when it changes.
    registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    awaitClose {
        unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
}

@ExperimentalCoroutinesApi
fun SharedPreferences.intFlow(key: String, defValue: Int): Flow<Int> = internalFlow(key) { getInt(key, defValue) }
