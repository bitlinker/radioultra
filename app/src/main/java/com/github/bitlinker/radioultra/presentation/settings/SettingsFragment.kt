package com.github.bitlinker.radioultra.presentation.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceFragmentCompat
import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.presentation.BackListener
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsFragment : PreferenceFragmentCompat(), BackListener {
    companion object {
        fun newInstance() = SettingsFragment()
    }

    val vm : SettingsViewModel by viewModel()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // This uses custom preferences layout from theme
        val toolbar = view.findViewById(R.id.toolbar) as Toolbar

        toolbar.title = getString(R.string.title_settings)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        toolbar.setNavigationOnClickListener {
            vm.onBackPressed();
        }
    }

    override fun onBackPressed() {
        vm.onBackPressed()
    }

    // TODO: listen to changes?

}