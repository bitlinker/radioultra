package com.github.bitlinker.radioultra.di

import android.content.Context
import com.github.bitlinker.radioultra.business.common.MetadataInteractor
import com.github.bitlinker.radioultra.business.common.StartupInteractor
import com.github.bitlinker.radioultra.business.history.HistoryFragmentInteractor
import com.github.bitlinker.radioultra.business.notification.NotificationServiceInteractor
import com.github.bitlinker.radioultra.business.common.PlayerInteractor
import com.github.bitlinker.radioultra.business.player.StreamSelectionInteractor
import com.github.bitlinker.radioultra.data.mediasession.MediaSessionRepository
import com.github.bitlinker.radioultra.data.player.PlayerRepository
import com.github.bitlinker.radioultra.data.radiostreams.PredefinedRadioStreamsRepository
import com.github.bitlinker.radioultra.data.radiostreams.RadioMetadataRepository
import com.github.bitlinker.radioultra.data.schedulers.AndroidSchedulerProvider
import com.github.bitlinker.radioultra.data.schedulers.SchedulerProvider
import com.github.bitlinker.radioultra.data.settings.SettingsRepository
import com.github.bitlinker.radioultra.data.storage.PersistentStorage
import com.github.bitlinker.radioultra.data.wakelock.WakelockRepository
import com.github.bitlinker.radioultra.presentation.history.HistoryNavigator
import com.github.bitlinker.radioultra.presentation.history.HistoryViewModel
import com.github.bitlinker.radioultra.presentation.navigation.MainNavigator
import com.github.bitlinker.radioultra.presentation.navigation.MainNavigatorMgr
import com.github.bitlinker.radioultra.presentation.notification.NotificationPresenter
import com.github.bitlinker.radioultra.presentation.player.PlayerNavigator
import com.github.bitlinker.radioultra.presentation.player.PlayerViewModel
import com.github.bitlinker.radioultra.presentation.player.StreamSelectionViewModel
import com.github.bitlinker.radioultra.presentation.settings.SettingsNavigator
import com.github.bitlinker.radioultra.presentation.settings.SettingsViewModel
import com.squareup.moshi.Moshi
import com.squareup.picasso.Picasso
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module

class Injector private constructor() {
    private object Holder {
        val INSTANCE = Injector()
    }

    companion object {
        val instance: Injector by lazy { Holder.INSTANCE }
    }

    lateinit var koinApp: KoinApplication

    val appModule = module {
        // Core:
        single<SchedulerProvider> { AndroidSchedulerProvider() }
        // TODO: properly config moshi; use in lib?
        single<Moshi> { Moshi.Builder().build() }
        single<Picasso> { Picasso.get() }

        single { MainNavigatorMgr() }
        single<MainNavigator> { get<MainNavigatorMgr>() }

        // Data:
        single { RadioMetadataRepository(get()) }
        single { PredefinedRadioStreamsRepository() }
        single { PersistentStorage(get(), get(), get()) }
        single { WakelockRepository(get()) }
        single { SettingsRepository(get(), get<SchedulerProvider>().io()) }
        single { PlayerRepository(get(), get()) }
        single { MediaSessionRepository(get()) }

        // Business:
        single { MetadataInteractor(get(), get()) }
        single { StartupInteractor(get(), get()) }

        // Player UI
        single { PlayerNavigator(get()) }
        single { PlayerInteractor(get(), get(), get(), get(), get()) }
        factory { PlayerViewModel(get(), get(), get(), get<SchedulerProvider>().ui()) }
        single { StreamSelectionInteractor(get(), get(), get()) }
        factory { StreamSelectionViewModel(get(), get()) }

        // Settings UI
        single { SettingsNavigator(get()) }
        factory { SettingsViewModel(get()) }

        // History UI
        single { HistoryFragmentInteractor(get()) }
        single { HistoryNavigator(get()) }
        factory { HistoryViewModel(get(), get(), get()) }

        // Notification UI
        single { NotificationPresenter(get(), get()) }
        single { NotificationServiceInteractor(get(), get(), get(), get()) }

        // TODO: confiure scopes?


    }

    fun init(appContext: Context) {
        koinApp = startKoin {
            // TODO
            androidContext(appContext)
            androidLogger()
            modules(appModule)
        }
    }

    fun addModule() {
        //koinApp.koin.getScope("123")
        //koinApp.modules(appModule)
        //koinApp.unloadModules(appModule)
    }
}