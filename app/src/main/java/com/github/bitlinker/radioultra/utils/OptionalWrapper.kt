package com.github.bitlinker.radioultra.utils

data class OptionalWrapper<T>(val value: T?) {
    companion object {
        fun <T> of(value: T) = OptionalWrapper(value)
        fun <T> empty() = OptionalWrapper(null)
    }

    fun isEmpty() = value == null
}