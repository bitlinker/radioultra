package com.github.bitlinker.radioultra.presentation.common

class ErrorDisplayerMgr : ErrorDisplayer {
    var errorDisplayer: ErrorDisplayer? = null

    override fun showError(error: Throwable?) {
        errorDisplayer?.showError(error)
    }
}

interface ErrorDisplayer {
    fun showError(error: Throwable?)
}