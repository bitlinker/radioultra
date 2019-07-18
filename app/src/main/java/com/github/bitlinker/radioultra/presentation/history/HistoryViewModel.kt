package com.github.bitlinker.radioultra.presentation.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.bitlinker.radioultra.business.history.HistoryFragmentInteractor
import com.github.bitlinker.radioultra.data.schedulers.SchedulerProvider
import com.github.bitlinker.radioultra.domain.HistoryItem
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

// TODO: offline history & delete all feature?
class HistoryViewModel(private val navigator: HistoryNavigator,
                       private val historyFragmentInteractor: HistoryFragmentInteractor,
                       private val schedulerProvider: SchedulerProvider) : ViewModel() {
    val disposable = CompositeDisposable()

    // TODO: expose simple livedata?
    val refreshing = MutableLiveData<Boolean>()
    val items = MutableLiveData<List<HistoryItem>>()
    val error = MutableLiveData<Throwable>()
    // TODO: how to show error?

    init {
        doRefresh()
    }

    fun onBackPressed() {
        navigator.navigateBack()
    }

    fun doRefresh() {
        disposable.add(historyFragmentInteractor.getHistory()
                .observeOn(schedulerProvider.ui())
                .toList()
                .doOnSubscribe { refreshing.value = true }
                .doFinally { refreshing.value = false }
                .subscribe(
                        { items.value = it },
                        { throwable ->
                            Timber.e(throwable)
                            error.value = throwable
                        }
                )
        )
    }

    fun onRefresh() = doRefresh()

    fun onItemClicked(item: HistoryItem) {
        navigator.navigateToItem(item.metadata)
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }
}