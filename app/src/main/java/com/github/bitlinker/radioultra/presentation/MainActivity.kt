package com.github.bitlinker.radioultra.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigator
import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.presentation.common.ErrorDisplayer
import com.github.bitlinker.radioultra.presentation.common.ErrorDisplayerMgr
import com.github.bitlinker.radioultra.presentation.navigation.MainNavigator
import com.github.bitlinker.radioultra.presentation.navigation.MainNavigatorMgr
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.androidx.scope.currentScope
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    val navMgr: MainNavigatorMgr by currentScope.inject()
    val errorMgr: ErrorDisplayerMgr by currentScope.inject()
    val vm: MainActivityViewModel by currentScope.viewModel(this)

    private val mainNavigator = object : MainNavigator {
        override fun navigateTo(id: Int) {
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
    }

    private val errorDisplayer = object : ErrorDisplayer {
        override fun showError(error: Throwable?) {
            // TODO: use with viewmodel, or sometimes error state may be lost
            // val error = MutableLiveData<Throwable>()
            Timber.w(error, "Error shown on UI: ")
            Snackbar.make(base_container, mapError(error), Snackbar.LENGTH_SHORT).show()
        }

        private fun mapError(error: Throwable?): String {
            // TODO: map
            return getString(R.string.error_generic)
        }
    }

    private val navController: NavController by lazy { Navigation.findNavController(this, R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate");
        setContentView(R.layout.activity_main)

        vm.keepScreenOnFlag.observe(this, Observer {
            with(window) {
                if (it)
                    addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                else
                    clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        })
    }

    private val currentFragment: BackListener?
        get() = supportFragmentManager.findFragmentById(R.id.container) as? BackListener

    override fun onResumeFragments() {
        super.onResumeFragments()
        navMgr.navigator = mainNavigator
        errorMgr.errorDisplayer = errorDisplayer
        vm.onResume()
    }

    override fun onPause() {
        vm.onPause()
        errorMgr.errorDisplayer = null
        navMgr.navigator = null
        super.onPause()
    }

    override fun onDestroy() {
        Timber.d("onDestroy");
        super.onDestroy()
    }

    override fun onBackPressed() {
        currentFragment?.onBackPressed() ?: super.onBackPressed()
    }
}