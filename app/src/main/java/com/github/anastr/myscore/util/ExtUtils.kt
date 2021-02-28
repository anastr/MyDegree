package com.github.anastr.myscore.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*

fun Float.formattedScore() = String.format(Locale.ENGLISH, "%.2f", this)

fun <T> LiveData<T>.toFlowable(owner: LifecycleOwner): Flowable<T> {
    return Flowable.create({ emitter ->
        observe(owner) {
            if (!emitter.isCancelled)
                emitter.onNext(it)
        }
    }, BackpressureStrategy.LATEST)
}

fun Disposable.disposeOnDestroy(owner: LifecycleOwner) {
    owner.lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY && !isDisposed)
                dispose()
        }
    })
}

fun <T> MutableList<T>.swap(i1: Int, i2: Int) {
//    this[i1] = this[i2].also { this[i2] = this[i1] }
    val tmp = this[i1]
    this[i1] = this[i2]
    this[i2] = tmp
}

fun ioThread(action: () -> Unit) {
    Flowable.fromCallable { action.invoke() }
        .subscribeOn(Schedulers.io())
        .subscribe()
}

fun Exception.isError403() =
    message == "UNAUTHENTICATED" ||
        message?.contains("Error 403 (Forbidden)") == true
