package ru.konovalovk.translateplayer

import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.lifecycle.MutableLiveData
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import ru.konovalovk.subtitle_parser.habib.SubtitleParser
import ru.konovalovk.subtitle_parser.subs.TimedTextObject
import ru.konovalovk.translateplayer.ui.player.VideoPlayerFragment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        supportFragmentManager.beginTransaction()
            .replace(R.id.flMain, VideoPlayerFragment())
            .commit()
    }
}