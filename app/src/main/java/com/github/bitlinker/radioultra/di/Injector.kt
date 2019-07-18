package com.github.bitlinker.radioultra.di

import android.content.Context
import com.github.bitlinker.radioultra.business.common.MetadataInteractor
import com.github.bitlinker.radioultra.business.common.StartupInteractor
import com.github.bitlinker.radioultra.business.history.HistoryFragmentInteractor
import com.github.bitlinker.radioultra.business.notification.PlayerServiceController
import com.github.bitlinker.radioultra.business.common.PlayerInteractor
import com.github.bitlinker.radioultra.business.player.StreamSelectionInteractor
import com.github.bitlinker.radioultra.data.wrappers.MediaSessionWrapper
import com.github.bitlinker.radioultra.data.player.PlayerWrapper
import com.github.bitlinker.radioultra.data.radiostreams.PredefinedRadioStreamsRepository
import com.github.bitlinker.radioultra.data.radiostreams.RadioMetadataRepository
import com.github.bitlinker.radioultra.data.schedulers.AndroidSchedulerProvider
import com.github.bitlinker.radioultra.data.schedulers.SchedulerProvider
import com.github.bitlinker.radioultra.data.settings.SettingsRepository
import com.github.bitlinker.radioultra.data.storage.PersistentStorage
import com.github.bitlinker.radioultra.data.wrappers.WakelockWrapper
import com.github.bitlinker.radioultra.di.Injector.Companion.SCHEDULER_UI
import com.github.bitlinker.radioultra.domain.TrackMetadata
import com.github.bitlinker.radioultra.presentation.MainActivity
import com.github.bitlinker.radioultra.presentation.history.HistoryNavigator
import com.github.bitlinker.radioultra.presentation.history.HistoryViewModel
import com.github.bitlinker.radioultra.presentation.navigation.MainNavigator
import com.github.bitlinker.radioultra.presentation.navigation.MainNavigatorMgr
import com.github.bitlinker.radioultra.presentation.notification.NotificationPresenter
import com.github.bitlinker.radioultra.presentation.player.PlayerNavigator
import com.github.bitlinker.radioultra.presentation.player.PlayerViewModel
import com.github.bitlinker.radioultra.presentation.streamselection.StreamSelectionViewModel
import com.github.bitlinker.radioultra.presentation.settings.SettingsNavigator
import com.github.bitlinker.radioultra.presentation.settings.SettingsViewModel
import com.github.bitlinker.radioultra.presentation.streamselection.StreamSelectionArgs
import com.github.bitlinker.radioultra.presentation.trackview.TrackViewNavigator
import com.github.bitlinker.radioultra.presentation.trackview.TrackViewModel
import com.squareup.moshi.Moshi
import com.squareup.picasso.Picasso
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module

private const val PLAYER_SCOPE = "PLAYER_SCOPE"

class Injector private constructor() {
    private object Holder {
        val INSTANCE = Injector()
    }

    companion object {
        const val SCHEDULER_UI = "SCHEDULER_UI"
        val instance: Injector by lazy { Holder.INSTANCE }
    }

    val appModule = module {
        // Core:
        single<SchedulerProvider> { AndroidSchedulerProvider() }

        // TODO: rm provider
        single(named(SCHEDULER_UI)) { get<SchedulerProvider>().ui() }

        // TODO: properly config moshi; use in lib?
        single<Moshi> { Moshi.Builder().build() }
        single<Picasso> { Picasso.get() }

        single { MainNavigatorMgr() }
        single<MainNavigator> { get<MainNavigatorMgr>() }

        // Data:
        single { RadioMetadataRepository(get()) }
        single { PredefinedRadioStreamsRepository() }
        single { PersistentStorage(get(), get(), get()) }
        single { WakelockWrapper(get(), get(named(SCHEDULER_UI))) }
        single { SettingsRepository(get(), get<SchedulerProvider>().io()) }
        single { PlayerWrapper(get(), get()) }
        single { MediaSessionWrapper(get()) }

        // Business:
        single { MetadataInteractor(get(), get()) }
        single { StartupInteractor(get(), get()) }
        // TODO: replace
        single { PlayerInteractor(get(), get(), get(), get(), get()) }
        single { StreamSelectionInteractor(get(), get(), get()) }

        // Notification UI
        single { NotificationPresenter(get(), get()) }
    }

    val playerModule = module {
        scope(named(PLAYER_SCOPE)) {
            scoped {
                PlayerServiceController(
                        get(),
                        get(),
                        get(),
                        get(),
                        get(),
                        get(),
                        get(),
                        get()
                )
                // TODO: confiure scopes?
                //scoped { PlayerWrapper(get(), get()) } // TODO: rm from base
            }
        }
    }

    val uiModule = module {
        // This is the special scope to provide android binding for each activity instance
        scope(named<MainActivity>()) {
            // TODO: separate interactors? Feature modules with scopes?

            // TODO: this will be in separate scope for each UI instance
            // Player UI
            scoped { PlayerNavigator(get()) }
            // TODO: return
            //scoped { PlayerInteractor(get(), get(), get(), get(), get()) }
            factory { PlayerViewModel(get(), get(), get(), get(named(SCHEDULER_UI))) }

            // Stream selection dialog
            // TODO: return
            //scoped { StreamSelectionInteractor(get(), get(), get()) }
            factory { (args: StreamSelectionArgs) -> StreamSelectionViewModel(get(), get(), args) }

            // Settings UI
            scoped { SettingsNavigator(get()) }
            factory { SettingsViewModel(get()) }

            // History UI
            scoped { HistoryFragmentInteractor(get()) }
            scoped { HistoryNavigator(get()) }
            factory { HistoryViewModel(get(), get(), get()) }

            // Track item UI
            scoped { TrackViewNavigator(get()) }
            factory { (item: TrackMetadata) -> TrackViewModel(get(), item) }
        }
    }

    fun init(appContext: Context) {
        startKoin {
            androidContext(appContext)
            androidLogger()
            androidFileProperties() // TODO: what properties can be used?
            modules(listOf(appModule, playerModule, uiModule))
        }
    }

    fun openPlayerScope(id: String): Scope {
        // TODO: address to scope id
        return GlobalContext.get().koin.getOrCreateScope(id, named(PLAYER_SCOPE))
    }
}