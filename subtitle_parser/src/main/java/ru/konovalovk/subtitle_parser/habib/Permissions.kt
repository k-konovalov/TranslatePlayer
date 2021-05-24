package ru.konovalovk.subtitle_parser.habib

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.ichi2.anki.FlashCardsContract.READ_WRITE_PERMISSION;

object Permissions {
    fun shouldRequestAnkiPermission(context: Context?): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            false
        } else ContextCompat.checkSelfPermission(
            context!!,
            READ_WRITE_PERMISSION
        ) !== PackageManager.PERMISSION_GRANTED
    }

    fun requestAnkiPermission(callbackActivity: Activity, callbackCode: Int) {
        ActivityCompat.requestPermissions(
            callbackActivity!!,
            arrayOf<String>(READ_WRITE_PERMISSION),
            callbackCode
        )
    }
}