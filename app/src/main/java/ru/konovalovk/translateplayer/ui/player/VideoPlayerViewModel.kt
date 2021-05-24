package ru.konovalovk.translateplayer.ui.player

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import ru.konovalovk.subtitle_parser.habib.SubtitleParser
import ru.konovalovk.subtitle_parser.subs.TimedTextObject
import ru.konovalovk.translateplayer.R
import ru.konovalovk.translateplayer.Subtitle
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.Executors

class VideoPlayerViewModel : ViewModel() {
    val TAG = this::class.java.simpleName

    val executor = Executors.newSingleThreadExecutor()

    private var subtitles: Array<Subtitle>? = null
    val liveCurSubtitleContent = MutableLiveData<String>()
    private var currSubtitle = Subtitle(0,1,"")

    val eventListener = object : MediaPlayer.EventListener {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun onEvent(event: MediaPlayer.Event?) {
            executor.execute {
                val startTime = System.currentTimeMillis()
                val currTime = event?.timeChanged ?: 0L
                if (event?.timeChanged != 0L) {
                    val isSubtitleStillShowing = currTime in currSubtitle.startTime..currSubtitle.endTime
                    if (!isSubtitleStillShowing) {
                        liveCurSubtitleContent.postValue("")
                        subtitles?.forEach {
                            if (currTime in it.startTime..it.endTime) {
                                currSubtitle = it
                                liveCurSubtitleContent.postValue(it.content)
                                Log.e(TAG, "Search time: ${System.currentTimeMillis() - startTime}ms")
                                return@execute
                            }
                        }
                    }
                    // Log.e(TAG, "Cur time: ${event?.timeChanged}ms")
                }
            }
        }
    }
    val iSubtitleParserListener = object : SubtitleParser.ISubtitleParserListener {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun onSubtitleParseCompleted(
            isSuccessful: Boolean,
            subtitleFile: TimedTextObject?,
            subtitlePath: String?
        ) {
            if (isSuccessful) subtitleFile?.run {
                val list = mutableListOf<Subtitle>()
                captions.values.forEach {
                    list.add(Subtitle(it.start.milliseconds, it.end.milliseconds, it.content))
                }
                subtitles = list.toTypedArray()
                state.postValue(State.LaunchVideo)
                //Log.e("MainActivity", str)
            } else Log.e(TAG, "Not success")
        }
    }

    val state = MutableLiveData<State>()

    fun fileFromAssets(context: Context, filepath: String, filename: String): File? {
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return null
        val out = File(directory.absolutePath, filename).apply {
            mkdirs()
            if (exists()) delete()
            createNewFile()
        }
        context.assets.open(filepath + filename).use { input ->
            FileOutputStream(out.absolutePath).use { output ->
                input.copyTo(output)
                output.flush()
            }
        }
        return out
    }
    enum class State{
        Ready, LaunchVideo
    }
}