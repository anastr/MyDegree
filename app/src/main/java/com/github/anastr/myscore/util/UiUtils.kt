package com.github.anastr.myscore.util

import android.content.Context
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import io.reactivex.rxjava3.processors.PublishProcessor
import java.util.concurrent.TimeUnit

typealias ClickListener = () -> Unit

private val clickPublisher: PublishProcessor<ClickListener> by lazy {
    return@lazy PublishProcessor.create<ClickListener>().apply {
        throttleFirst(300, TimeUnit.MILLISECONDS)
            .onBackpressureDrop()
            .subscribe({
                try {
                    it.invoke()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, {
                it.printStackTrace()
            })
    }
}

fun View.rapidClickListener(clickListener: ClickListener) {
    setOnClickListener {
        clickPublisher.onNext(clickListener)
    }
}

@ColorInt
fun Context.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}
