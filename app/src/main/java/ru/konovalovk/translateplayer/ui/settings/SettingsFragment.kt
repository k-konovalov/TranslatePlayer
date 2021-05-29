package ru.konovalovk.translateplayer.ui.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import ru.konovalovk.translateplayer.R
import ru.konovalovk.translateplayer.base.BasePreferenceFragment

class SettingsFragment: BasePreferenceFragment() {
    override val prefXml = R.xml.preference_settings
    override val preferenceKeys: Array<PrefKeys> = arrayOf(
        PrefKeys(R.string.settings_subtitles_transparency_key, R.string.settings_subtitles_transparency_default_value, R.string.settings_subtitles_transparency_title,R.string.settings_subtitles_transparency_summary),
        PrefKeys(R.string.settings_subtitles_bottom_margin_key, R.string.settings_subtitles_bottom_margin_default_value,R.string.settings_subtitles_bottom_margin_title,R.string.settings_subtitles_bottom_margin_summary),
        PrefKeys(R.string.settings_media_pause_key, R.string.settings_media_pause_default_value,R.string.settings_media_pause_title,R.string.settings_media_pause_summary),
        PrefKeys(R.string.settings_media_audio_shift_key, R.string.settings_media_audio_shift_default_value,R.string.settings_media_audio_shift_title,R.string.settings_media_audio_shift_summary)
    )
}