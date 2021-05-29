package ru.konovalovk.translateplayer.ui.statistics

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import ru.konovalovk.translateplayer.R

class StatisticsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_statictics,rootKey)
    }
}