package ru.konovalovk.translateplayer.base

import android.os.Bundle
import android.text.InputType
import androidx.preference.*
import ru.konovalovk.translateplayer.R
import ru.konovalovk.translateplayer.logic.convertSecondsToHMmSs
import kotlin.reflect.typeOf

abstract class BasePreferenceFragment: PreferenceFragmentCompat() {
   abstract val prefXml: Int
   abstract val preferenceKeys: Array<PrefKeys>

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(prefXml, rootKey)
        setCurrentSummary()
        setSummaryChangeListeners()
        setEditTextNumberInput()
    }

    private fun setCurrentSummary(){
        preferenceKeys.forEach { pref ->
            val strPref = pref.toPrefKeysAsString()
            (findPreference(strPref.key) as? Preference)?.run {
                val currentVal = when(this){
                    is SeekBarPreference -> sharedPreferences.getInt(strPref.key, strPref.defaultVal.toInt())
                    is SwitchPreferenceCompat -> sharedPreferences.getBoolean(strPref.key, strPref.defaultVal.toBoolean())
                    else -> sharedPreferences.getString(strPref.key, strPref.defaultVal)
                }.toString()
                summary = formatSummary(strPref, currentVal)
            }
        }
    }

    private fun formatSummary(pref: PrefKeysAsString, settedVal: String): String{
        val isPrefContainsTime = pref.key == getString(R.string.statistics_media_time_key)
        return if (isPrefContainsTime) convertSecondsToHMmSs(settedVal.toLong())
        else String.format(pref.summaryTemplate, settedVal)
    }

    private fun setSummaryChangeListeners(){
        preferenceKeys.forEach { pref ->
            val strPref = pref.toPrefKeysAsString()
            (findPreference(strPref.key) as? Preference)?.run {
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                    when(this){
                        is SeekBarPreference -> sharedPreferences.edit().putInt(preference.key, newValue.toString().toInt()).apply()
                        is SwitchPreferenceCompat -> sharedPreferences.edit().putBoolean(preference.key, newValue.toString().toBoolean()).apply()
                        else -> sharedPreferences.edit().putString(preference.key, newValue.toString()).apply()
                    }
                    summary = String.format(strPref.summaryTemplate, newValue.toString())
                    true
                }
            }
        }
    }

    private fun setEditTextNumberInput() {
        preferenceKeys.forEach { pref ->
            val strPref = pref.toPrefKeysAsString()
            (findPreference(strPref.key) as? EditTextPreference)?.setOnBindEditTextListener { editText ->
                    editText.inputType = InputType.TYPE_CLASS_NUMBER
                }
            }
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