package com.github.anastr.myscore.viewmodel

sealed class State<out T> {
    object Loading : State<Nothing>()
    class Error(val error: Exception) : State<Nothing>()
    class Success<out T>(val data: T) : State<T>()
}
