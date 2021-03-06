package com.github.anastr.myscore.util.pref

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder

class DangerPreference(
    context: Context?,
    attrs: AttributeSet?,
): Preference(context, attrs) {

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        holder?.itemView?.findViewById<TextView>(android.R.id.title)?.setTextColor(Color.RED)
    }
}
