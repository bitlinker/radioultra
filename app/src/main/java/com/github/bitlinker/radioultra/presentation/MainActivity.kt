package com.github.bitlinker.radioultra.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigator
import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.business.common.StartupInteractor
import com.github.bitlinker.radioultra.business.notification.NotificationServiceInteractor
import com.github.bitlinker.radioultra.data.radiostreams.RadioMetadataRepository
import com.github.bitlinker.radioultra.presentation.navigation.MainNavigator
import com.github.bitlinker.radioultra.presentation.navigation.MainNavigatorMgr
import org.koin.android.ext.android.inject
import timber.log.Timber

// TODO: pass navigation command to the fragment itself and navigate there with extras?
class MainActivity : AppCompatActivity(), MainNavigator {
    val navMgr: MainNavigatorMgr by inject()

    private val navController: NavController by lazy { Navigation.findNavController(this, R.id.nav_host_fragment) }

    // DBG
    val notificationServiceInteractor: NotificationServiceInteractor by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate");
        setContentView(R.layout.activity_main)
        navMgr.navigator = this


        // TODO: use settings flag
        //window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) //, not here

        notificationServiceInteractor.start()
    }

    private val currentFragment: BackListener?
        get() = supportFragmentManager.findFragmentById(R.id.container) as? BackListener

    override fun onDestroy() {
        Timber.d("onDestroy");
        navMgr.navigator = null
        super.onDestroy()
    }

    override fun navigateTo(@IdRes id: Int) {
        navController.navigate(id)
    }

    override fun navigateToExternalUri(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    override fun navigateTo(directions: NavDirections) {
        navController.navigate(directions)
    }

    override fun navigateTo(directions: NavDirections, extras: FragmentNavigator.Extras?) {
        navController.navigate(directions.getActionId(), directions.getArguments(), null, extras)
    }

    override fun navigateBack() = onBackPressed()

    override fun onBackPressed() {
        currentFragment?.onBackPressed() ?: super.onBackPressed()
    }
}