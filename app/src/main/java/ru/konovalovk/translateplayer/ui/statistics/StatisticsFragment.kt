package ru.konovalovk.translateplayer.ui.statistics

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ru.konovalovk.translateplayer.R
import ru.konovalovk.translateplayer.base.BasePreferenceFragment
import ru.konovalovk.translateplayer.logic.convertSecondsToHMmSs

class StatisticsFragment: BasePreferenceFragment() {
    override val preferenceKeys = arrayOf(
        PrefKeys(R.string.statistics_words_total_key, R.string.statistics_words_total_default_value, R.string.statistics_words_total_title,R.string.statistics_words_total_summary),
        PrefKeys(R.string.statistics_words_exported_key, R.string.statistics_words_exported_default_value,R.string.statistics_words_exported_title,R.string.statistics_words_exported_summary),
        PrefKeys(R.string.statistics_media_total_key, R.string.statistics_media_total_default_value,R.string.statistics_media_total_title,R.string.statistics_media_total_summary),
        PrefKeys(R.string.statistics_media_time_key, R.string.statistics_media_time_default_value,R.string.statistics_media_time_title,R.string.statistics_media_time_summary)
    )
    override val prefXml = R.xml.preference_statictics
}