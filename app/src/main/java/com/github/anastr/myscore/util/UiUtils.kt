package com.github.anastr.myscore.util

import android.content.Context
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

fun FloatingActionButton.hideFab() {
    hide()
//    animate()
////        .alpha(0f)
//        .scaleX(0f)
//        .scaleY(0f)
//        .setDuration(150)
//        .withStartAction { isClickable = false }
//        .withEndAction {
//            visibility = View.GONE
//        }
}

fun FloatingActionButton.showFab() {
    show()
//    animate()
////        .alpha(1f)
//        .scaleX(1f)
//        .scaleY(1f)
//        .setDuration(150)
//        .withStartAction { isClickable = true }
//        .withEndAction {
//            visibility = View.VISIBLE
//        }
}