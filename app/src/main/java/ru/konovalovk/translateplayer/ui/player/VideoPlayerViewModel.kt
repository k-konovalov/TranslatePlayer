package ru.konovalovk.translateplayer.ui.player

import android.content.Context
import android.content.SharedPreferences
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
import ru.konovalovk.domain.models.Subtitle
import ru.konovalovk.interactor.TranslatorInteractor
import ru.konovalovk.repository.db.entity.Media
import ru.konovalovk.subtitle_parser.habib.SubtitleParser
import ru.konovalovk.translateplayer.R
import ru.konovalovk.translateplayer.logic.PlayTimer
import ru.konovalovk.translateplayer.ui.MyApp
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.Executors


class VideoPlayerViewModel(val savedState: SavedStateHandle) : ViewModel() {
    val TAG = this::class.java.simpleName

    var filename = ""

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

    val playTimer = PlayTimer()

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

    fun prepareMediaDb(filename: String?){
        viewModelScope.launch {
            filename?.run {
                val mediaInDb = MyApp.db.mediaDAO.getMediaByName(this)?.name ?: ""
                if (mediaInDb.isEmpty()) MyApp.db.mediaDAO.insert(Media(id = 0, name = this, duration = 0))
            }
        }
    }

    fun fillMediaChrono(time: Long) {
        viewModelScope.launch {
            val oldMedia = MyApp.db.mediaDAO.getMediaByName(filename) ?: return@launch
            MyApp.db.mediaDAO.update(Media(oldMedia.id, name = oldMedia.name, duration = oldMedia.duration?.plus(time.toInt())))
        }
    }

    fun saveGlobalChrono(sharedPreferences: SharedPreferences, getString: (Int)-> String){
        viewModelScope.launch {
            val curMedia = MyApp.db.mediaDAO.getMediaByName(filename) ?: return
            val strKey = getString(R.string.statistics_media_time_key)
            val strDefault = getString(R.string.statistics_media_time_default_value)
            val currValue = sharedPreferences.getString(strKey, strDefault)?.toLong() ?: 0L
            val newValue = currValue + (curMedia.duration ?: 0)
            sharedPreferences.edit().putString(strKey, newValue.toString()).apply()
        }
    }

    enum class State{
        Ready, Starting, Playing, Pausing, Stopping
    }
}