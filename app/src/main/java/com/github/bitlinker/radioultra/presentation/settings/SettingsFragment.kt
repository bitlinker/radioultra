package com.github.bitlinker.radioultra.presentation.settings

import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.presentation.BackListener
import com.github.bitlinker.radioultra.presentation.common.applyMenuTint
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.androidx.scope.currentScope

class SettingsFragment : PreferenceFragmentCompat(), BackListener {
    val vm: SettingsViewModel by currentScope.viewModel(this)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        setupSwitchPreference(R.string.pref_key_download_album_covers)
        setupSwitchPreference(R.string.pref_key_play_on_startup)
        setupTextPreference(R.string.pref_key_useragent_string, InputType.TYPE_CLASS_TEXT)
        setupTextPreference(R.string.pref_key_stopped_metadata_update_interval, InputType.TYPE_CLASS_NUMBER)
        setupTextPreference(R.string.pref_key_buffering_time, InputType.TYPE_CLASS_NUMBER)
        with(getPreference<Preference>(R.string.pref_key_version)) {
            summaryProvider = Preference.SummaryProvider<Preference> {
                vm.getVersionString()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // This fragment uses custom preferences layout from theme
        val toolbar = view.findViewById(R.id.toolbar) as Toolbar

        toolbar.title = getString(R.string.title_settings)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        toolbar.setNavigationOnClickListener {
            vm.onBackPressed();
        }
        toolbar.applyMenuTint(true, true)
    }

    private fun setupSwitchPreference(@StringRes res: Int) {
        with(getPreference<SwitchPreferenceCompat>(res)) {
            summaryOn = getString(R.string.switch_on)
            summaryOff = getString(R.string.switch_off)
        }
    }

    private fun setupTextPreference(@StringRes res: Int, inputType: Int) {
        with(getPreference<EditTextPreference>(res)) {
            setOnBindEditTextListener {
                it.inputType = inputType
                it.setSingleLine()
            }
            summaryProvider = Preference.SummaryProvider<EditTextPreference> { preference ->
                return@SummaryProvider preference.text
            }
        }
    }

    private fun <T : Preference> getPreference(@StringRes res: Int): T {
        return findPreference<T>(getString(res))!!
    }

    override fun onBackPressed() {
        vm.onBackPressed()
    }
}