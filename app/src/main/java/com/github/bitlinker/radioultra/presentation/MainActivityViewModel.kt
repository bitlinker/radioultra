package com.github.bitlinker.radioultra.presentation

import androidx.lifecycle.MutableLiveData
import com.github.bitlinker.radioultra.business.ui.UiStartupInteractor
import com.github.bitlinker.radioultra.presentation.common.BaseViewModel
import timber.log.Timber

class MainActivityViewModel(
        uiStartupInteractor: UiStartupInteractor
) : BaseViewModel() {
    init {
        uiStartupInteractor
                .bindToUi()
                // WTF???
                .doOnSubscribe { Timber.d("UI binding") }
                .doFinally { Timber.d("UI unbinding") }
                .subscribe()
                .connect()
    }

    // TODO: show error snackbar with livedata too

    // TODO: set it up!
    val keepScreenOnFlag = MutableLiveData<Boolean>()

    fun onResume() {

    }

    fun onPause() {

    }
}