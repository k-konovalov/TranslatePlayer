package ru.konovalovk.translateplayer.ui.statistics

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ru.konovalovk.translateplayer.R
import ru.konovalovk.translateplayer.ui.convertSecondsToHMmSs

class StatisticsFragment: PreferenceFragmentCompat() {
    private val preferenceKeys = arrayOf(
        PrefKeys(R.string.statistics_words_total_key, R.string.statistics_words_total_default_value, R.string.statistics_words_total_title,R.string.statistics_words_total_summary),
        PrefKeys(R.string.statistics_words_exported_key, R.string.statistics_words_exported_default_value,R.string.statistics_words_exported_title,R.string.statistics_words_exported_summary),
        PrefKeys(R.string.statistics_media_total_key, R.string.statistics_media_total_default_value,R.string.statistics_media_total_title,R.string.statistics_media_total_summary),
        PrefKeys(R.string.statistics_media_time_key, R.string.statistics_media_time_default_value,R.string.statistics_media_time_title,R.string.statistics_media_time_summary)
    )
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_statictics,rootKey)
        setCurrentSummary()
    }

    private fun setCurrentSummary(){
        preferenceKeys.forEach { pref ->
            val strPref = pref.toPrefKeysAsString()
            (findPreference(strPref.key) as? Preference)?.run {
                val currentVal = sharedPreferences.getString(strPref.key, strPref.defaultVal)  ?: ""
                summary = formatSummary(strPref, currentVal)
            }
        }
    }

    private fun formatSummary(pref: PrefKeysAsString, settedVal: String): String{
        val isPrefContainsTime = pref.key == getString(R.string.statistics_media_time_key)
        return if (isPrefContainsTime) convertSecondsToHMmSs(settedVal.toLong())
        else String.format(pref.summaryTemplate, settedVal)
    }

    inner class PrefKeys(
        val key: Int,
        val defaultVal: Int,
        val title: Int,
        val summaryTemplate: Int
    ){
        fun toPrefKeysAsString(): PrefKeysAsString {
            return PrefKeysAsString(getString(key),getString(defaultVal),getString(title),getString(summaryTemplate))
        }
    }

    data class PrefKeysAsString(
        val key: String,
        val defaultVal: String,
        val title: String,
        val summaryTemplate: String
    )
}