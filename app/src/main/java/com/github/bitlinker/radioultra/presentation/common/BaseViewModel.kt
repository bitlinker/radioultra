package com.github.bitlinker.radioultra.presentation.common

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseViewModel : ViewModel() {
    private val disposable = CompositeDisposable()

    protected fun Disposable.connect() {
        disposable.add(this)
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }
}