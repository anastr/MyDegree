package com.github.anastr.myscore.util.pref

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.github.anastr.myscore.R


class NumberPickerPreference(context: Context?, attrs: AttributeSet?): DialogPreference(
    context,
    attrs
) {

    var value: Int = 60
        set(value) {
            field = value
            persistInt(value)
            summary = "$value"
        }

    override fun getDialogLayoutResource(): Int = R.layout.pref_number

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, 60)
    }

    override fun onSetInitialValue(
        restorePersistedValue: Boolean,
        defaultValue: Any?
    ) {
        value = if (restorePersistedValue) getPersistedInt(value) else defaultValue as Int
    }
}