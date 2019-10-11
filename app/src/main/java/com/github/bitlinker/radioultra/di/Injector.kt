package com.github.bitlinker.radioultra.di

import android.content.Context
import com.github.bitlinker.radioultra.business.playerservice.MetadataInteractor
import com.github.bitlinker.radioultra.business.common.PlayerInteractor
import com.github.bitlinker.radioultra.business.common.StreamSelectionInteractor
import com.github.bitlinker.radioultra.business.playerservice.PlayerServiceInteractor
import com.github.bitlinker.radioultra.business.ui.*
import com.github.bitlinker.radioultra.data.PackageUtils
import com.github.bitlinker.radioultra.data.player.ExoPlayerWrapper
import com.github.bitlinker.radioultra.data.playerservice.PlayerService
import com.github.bitlinker.radioultra.data.playerservice.PlayerServiceConnector
import com.github.bitlinker.radioultra.data.radiostreams.PredefinedRadioStreamsRepository
import com.github.bitlinker.radioultra.data.radiostreams.RadioMetadataRepository
import com.github.bitlinker.radioultra.data.schedulers.AndroidSchedulerProvider
import com.github.bitlinker.radioultra.data.schedulers.SchedulerProvider
import com.github.bitlinker.radioultra.data.settings.SettingsRepository
import com.github.bitlinker.radioultra.data.storage.CurrentRadioStreamRepository
import com.github.bitlinker.radioultra.data.wrappers.AudioFocusWrapper
import com.github.bitlinker.radioultra.data.wrappers.MediaSessionWrapper
import com.github.bitlinker.radioultra.data.wrappers.NoisyBroadcastWrapper
import com.github.bitlinker.radioultra.data.wrappers.WakelockWrapper
import com.github.bitlinker.radioultra.domain.TrackMetadata
import com.github.bitlinker.radioultra.presentation.MainActivity
import com.github.bitlinker.radioultra.presentation.MainActivityViewModel
import com.github.bitlinker.radioultra.presentation.common.ErrorDisplayerMgr
import com.github.bitlinker.radioultra.presentation.history.HistoryFragment
import com.github.bitlinker.radioultra.presentation.history.HistoryNavigator
import com.github.bitlinker.radioultra.presentation.history.HistoryViewModel
import com.github.bitlinker.radioultra.presentation.navigation.MainNavigator
import com.github.bitlinker.radioultra.presentation.navigation.MainNavigatorMgr
import com.github.bitlinker.radioultra.presentation.notification.NotificationPresenter
import com.github.bitlinker.radioultra.presentation.player.PlayerFragment
import com.github.bitlinker.radioultra.presentation.player.PlayerViewModel
import com.github.bitlinker.radioultra.presentation.player.PlayerViewNavigator
import com.github.bitlinker.radioultra.presentation.settings.SettingsFragment
import com.github.bitlinker.radioultra.presentation.settings.SettingsNavigator
import com.github.bitlinker.radioultra.presentation.settings.SettingsViewModel
import com.github.bitlinker.radioultra.presentation.streamselection.StreamSelectionArgs
import com.github.bitlinker.radioultra.presentation.streamselection.StreamSelectionDialogFragment
import com.github.bitlinker.radioultra.presentation.streamselection.StreamSelectionNavigator
import com.github.bitlinker.radioultra.presentation.streamselection.StreamSelectionViewModel
import com.github.bitlinker.radioultra.presentation.trackview.TrackViewFragment
import com.github.bitlinker.radioultra.presentation.trackview.TrackViewModel
import com.github.bitlinker.radioultra.presentation.trackview.TrackViewNavigator
import com.squareup.moshi.Moshi
import com.squareup.picasso.Picasso
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

class Injector private constructor() {
    private object Holder {
        val INSTANCE = Injector()
    }

    companion object {
        val instance: Injector by lazy { Holder.INSTANCE } // TODO: simplify?
    }

    private val coreModule = module {
        single { PackageUtils(get()) }
        single<SchedulerProvider> { AndroidSchedulerProvider() }
        single<Moshi> { Moshi.Builder().build() }
    }

    private val appModule = module {
        // Data:
        single { RadioMetadataRepository(get()) }
        single { PredefinedRadioStreamsRepository() }
        single { CurrentRadioStreamRepository(get(), get(), get()) }
        single { SettingsRepository(get(), get()) }
        single { PlayerServiceConnector(get()) }

        // Business:
        single { PlayerInteractor(get()) }
        single { StreamSelectionInteractor(get(), get(), get()) }
    }

    private val uiModule = module {
        // Core UI
        single { MainNavigatorMgr() }
        single<MainNavigator> { get<MainNavigatorMgr>() }
        single { ErrorDisplayerMgr() }
        single<Picasso> { Picasso.get() }

        single { UiStartupInteractor(get(), get()) }

        // Activity:
        scope(named<MainActivity>()) {
            viewModel { MainActivityViewModel(get()) }

            // Player:
            scope(named<PlayerFragment>()) {
                scoped { PlayerViewNavigator(get()) }
                scoped { PlayerViewInteractor(get(), get()) }
                viewModel { PlayerViewModel(get(), get(), get(), get()) }
            }

            // Stream selection
            scope(named<StreamSelectionDialogFragment>()) {
                scoped { StreamSelectionNavigator(get()) }
                scoped { StreamSelectionViewInteractor(get()) }
                viewModel { (args: StreamSelectionArgs) -> StreamSelectionViewModel(get(), get(), args) }
            }

            // Track view
            scope(named<TrackViewFragment>()) {
                scoped { TrackViewNavigator(get()) }
                viewModel { (item: TrackMetadata) -> TrackViewModel(get(), item) }
            }

            // History view
            scope(named<HistoryFragment>()) {
                scoped { HistoryNavigator(get()) }
                scoped { HistoryViewInteractor(get()) }
                viewModel { HistoryViewModel(get(), get(), get(), get()) }
            }

            // Settings
            scope(named<SettingsFragment>()) {
                scoped { SettingsViewInteractor(get()) }
                scoped { SettingsNavigator(get()) }
                viewModel { SettingsViewModel(get(), get()) }
            }
        }
    }

    private val playerModule = module {
        scope(named<PlayerService>()) {
            scoped { WakelockWrapper(get(), get()) }
            scoped { ExoPlayerWrapper(get(), get()) }
            scoped { MediaSessionWrapper(get(), get()) }
            scoped { NotificationPresenter(get(), get()) }
            scoped { MetadataInteractor(get(), get(), get()) }
            scoped { AudioFocusWrapper(get()) }
            scoped { NoisyBroadcastWrapper(get(), get()) }
            scoped {
                PlayerServiceInteractor(
                        get(),
                        get(),
                        get(),
                        get(),
                        get(),
                        get(),
                        get(),
                        get(),
                        get(),
                        get()
                )
            }
        }
    }

    fun init(appContext: Context) {
        startKoin {
            androidContext(appContext)
            androidLogger()
            androidFileProperties() // TODO: what properties can be used?
            modules(listOf(coreModule, appModule, playerModule, uiModule))
        }
    }
}