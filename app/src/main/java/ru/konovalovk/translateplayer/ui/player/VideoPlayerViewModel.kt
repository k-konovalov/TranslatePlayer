package ru.konovalovk.translateplayer.ui.player

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.videolan.libvlc.MediaPlayer
import ru.konovalovk.interactor.TranslatorInteractor
import ru.konovalovk.repository.network.NetworkModule
import ru.konovalovk.subtitle_parser.habib.SubtitleParser
import ru.konovalovk.subtitle_parser.subs.TimedTextObject
import ru.konovalovk.translateplayer.Subtitle
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class VideoPlayerViewModel(val savedState: SavedStateHandle) : ViewModel() {
    val TAG = this::class.java.simpleName

    val translatorInteractor = TranslatorInteractor()
    val executor = Executors.newSingleThreadExecutor()

    private var subtitles: Array<Subtitle>? = null
    val liveCurSubtitleContent = MutableLiveData<String>()
    val EXTRA_CURR_TIME = "EXTRA_CURR_TIME"
    val currPlaybackTime = MutableLiveData<Int>()
    private var currSubtitle = Subtitle(0,1,"")

    var lastState = State.Ready
    val state = MutableLiveData<State>()

    val eventListener = object : MediaPlayer.EventListener {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun onEvent(event: MediaPlayer.Event?) {
            executor.execute {
                val startTime = System.currentTimeMillis()
                val currTime = event?.timeChanged ?: 0L
                if (event?.timeChanged != 0L) {
                    currPlaybackTime.postValue(currTime.toInt())
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
    val iSubtitleParserListener = SubtitleParser.ISubtitleParserListener { isSuccessful, subtitleFile, subtitlePath ->
            if (isSuccessful) subtitleFile?.run {
                val list = mutableListOf<Subtitle>()
                captions.values.forEach {
                    val improvedContent = it.content.replace("<br />","\n")
                    list.add(Subtitle(it.start.milliseconds, it.end.milliseconds, improvedContent))
                }
                subtitles = list.toTypedArray()
                state.postValue(State.Starting)
                //Log.e("MainActivity", str)
            } else Log.e(TAG, "Not success")
        }

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

    fun convertSecondsToHMmSs(millis: Long): String {
        return String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
            TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    enum class State{
        Ready, Starting, Playing, Pausing, Stopping
    }
}