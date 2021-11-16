package com.github.anastr.myscore.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun LifecycleOwner.launchAndRepeatOnLifecycle(
    state: Lifecycle.State,
    block: suspend CoroutineScope.() -> Unit,
) {
    lifecycleScope.launch { repeatOnLifecycle(state, block) }
}
