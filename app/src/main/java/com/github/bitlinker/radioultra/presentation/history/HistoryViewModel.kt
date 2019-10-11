package com.github.bitlinker.radioultra.presentation.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.bitlinker.radioultra.business.ui.HistoryViewInteractor
import com.github.bitlinker.radioultra.data.schedulers.SchedulerProvider
import com.github.bitlinker.radioultra.domain.HistoryItem
import com.github.bitlinker.radioultra.presentation.common.BaseViewModel
import com.github.bitlinker.radioultra.presentation.common.ErrorDisplayerMgr

// TODO: offline history & delete all features
class HistoryViewModel(private val navigator: HistoryNavigator,
                       private val interactor: HistoryViewInteractor,
                       private val schedulerProvider: SchedulerProvider,
                       private val errorDisplayerMgr: ErrorDisplayerMgr) : BaseViewModel() {
    private val _refreshing = MutableLiveData<Boolean>()
    private val _items = MutableLiveData<List<HistoryItem>>()

    val refreshing: LiveData<Boolean>
        get() = _refreshing

    val items: LiveData<List<HistoryItem>>
        get() = _items

    init {
        doRefresh()
    }

    fun onBackPressed() {
        navigator.navigateBack()
    }

    fun doRefresh() {
        interactor.getHistory()
                .observeOn(schedulerProvider.ui())
                .toList()
                .doOnSubscribe { _refreshing.value = true }
                .doFinally { _refreshing.value = false }
                .subscribe(
                        { _items.value = it },
                        { throwable -> errorDisplayerMgr.showError(throwable) }
                )
                .connect()
    }

    fun onRefresh() = doRefresh()

    fun onItemClicked(item: HistoryItem) {
        navigator.navigateToItem(item.metadata)
    }
}