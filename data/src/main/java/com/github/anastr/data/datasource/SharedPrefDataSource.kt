package com.github.anastr.data.datasource

import android.content.SharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

@ExperimentalCoroutinesApi
class SharedPrefDataSource @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) {

    fun intFlow(key: String, defValue: Int): Flow<Int> =
        internalFlow(key) { sharedPreferences.getInt(key, defValue) }

    fun stringFlow(key: String, defValue: String): Flow<String> =
        internalFlow(key) { sharedPreferences.getString(key, defValue) ?: defValue }

    private fun <T> internalFlow(
        key: String,
        getObj: () -> T
    ): Flow<T> = callbackFlow {
        // Send the current value.
        trySend(getObj())
        val preferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
                if (changedKey == key) {
                    trySend(getObj())
                }
            }
        // Send new value when it changes.
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        }
    }
}
