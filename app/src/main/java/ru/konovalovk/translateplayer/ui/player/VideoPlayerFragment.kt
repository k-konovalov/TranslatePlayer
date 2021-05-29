package ru.konovalovk.translateplayer.ui.player

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.doOnLayout
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.MediaPlayer.Position.Bottom
import org.videolan.libvlc.MediaPlayer.Position.Top
import org.videolan.libvlc.util.VLCVideoLayout
import ru.konovalovk.interactor.TranslatorInteractor
import ru.konovalovk.subtitle_parser.habib.SubtitleParser
import ru.konovalovk.translateplayer.R
import ru.konovalovk.translateplayer.logic.convertSecondsToHMmSs
import ru.konovalovk.translateplayer.logic.transparentMap
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.Exception

class VideoPlayerFragment : Fragment(R.layout.fragment_video_player) {
    private val viewModel: VideoPlayerViewModel by viewModels()
    private val sharedPreferences by lazy{ PreferenceManager.getDefaultSharedPreferences(requireActivity()) }

    private val clPlayer by lazy { requireView().findViewById<ConstraintLayout>(R.id.clPlayer) }

    private val vlcPlayer by lazy { requireView().findViewById<VLCVideoLayout>(R.id.vlcPlayer).apply {
        setOnClickListener {
            if (viewModel.state.value == VideoPlayerViewModel.State.Pausing) viewModel.state.postValue(VideoPlayerViewModel.State.Playing)
            else viewModel.state.postValue(VideoPlayerViewModel.State.Pausing)
        }
    } }
    val mLibVLC by lazy { LibVLC(requireContext(), ArrayList<String>().apply { add("-vvv") }) }
    val mMediaPlayer by lazy { MediaPlayer(mLibVLC) }

    val tvSubtitle by lazy { requireView().findViewById<TextView>(R.id.tvSubtitles).apply {
        setOnClickListener {
            viewModel.translatorInteractor.translatePhrase(text.toString(), TranslatorInteractor.Translator.Google)
            if (viewModel.state.value != VideoPlayerViewModel.State.Pausing) viewModel.state.postValue(VideoPlayerViewModel.State.Pausing)
        }
        val transparencyValue = sharedPreferences.getInt(getString(R.string.settings_subtitles_transparency_key), getString(R.string.settings_subtitles_transparency_default_value).toInt())
        val alpha = transparentMap[transparencyValue]
        setBackgroundColor(Color.parseColor("#${alpha}000000"))
        val constraintSet = ConstraintSet().apply{ clone(clPlayer) }
        val bottom =
            sharedPreferences.getString(
                getString(R.string.settings_subtitles_bottom_margin_key),
                getString(R.string.settings_subtitles_bottom_margin_default_value)
            )?.toInt() ?: 0
        constraintSet.connect(R.id.tvSubtitles, ConstraintSet.BOTTOM, R.id.sbTime, ConstraintSet.TOP, bottom)
        Log.e("TAG", bottom.toString())
        constraintSet.applyTo(clPlayer)
    } }
    val tvTime by lazy { requireView().findViewById<TextView>(R.id.tvTime)}
    val sbTime by lazy { requireView().findViewById<SeekBar>(R.id.sbTime).apply {
        setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (viewModel.state.value == VideoPlayerViewModel.State.Pausing) tvTime.text = convertSecondsToHMmSs(progress.toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val currTime = seekBar?.progress?.toLong() ?: return
                mMediaPlayer.time = currTime
                tvTime.text = convertSecondsToHMmSs(currTime)
            }
        })
    }}
    var snackBar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<MaterialToolbar>(R.id.mtbMain).visibility = View.GONE

        parseSubtitle()

        viewModel.liveCurSubtitleContent.observe(viewLifecycleOwner, {
            tvSubtitle.visibility = if (it.isEmpty()) View.INVISIBLE else View.VISIBLE
            tvSubtitle.text = it
        })
        viewModel.currPlaybackTime.observe(viewLifecycleOwner,{
            if (sbTime.max == 0) {
                sbTime.max = mMediaPlayer.length.toInt()
                sbTime.visibility = View.VISIBLE
                //Todo: Animation here
            }
            sbTime.progress = it
            tvTime.text = convertSecondsToHMmSs(it.toLong())
        })
        viewModel.translatorInteractor.translatedWord.observe(viewLifecycleOwner, {
            val originalWords = tvSubtitle.text
                .replace("[.?!)(,:]".toRegex(),"") //delete non words symbols
                .split(" ")
            val translatedWords = it
                .replace("[.?!)(,:]".toRegex(),"") //delete non words symbols
                .split(" ")
            val strKey = getString(R.string.statistics_words_total_key)
            val strDefault = getString(R.string.statistics_words_total_default_value)
            val currValue = sharedPreferences.getString(strKey, strDefault)?.toInt() ?: 0
            val newValue = currValue + originalWords.size

            sharedPreferences.edit().putString(strKey, newValue.toString()).apply()

            snackBar = Snackbar.make(requireView(), it ?: return@observe, Snackbar.LENGTH_LONG)
            snackBar?.show()
        })
        viewModel.state.observe(viewLifecycleOwner, {
            if(viewModel.lastState == it) return@observe
            viewModel.lastState = it
            when(it){
                VideoPlayerViewModel.State.Ready -> {

                }
                VideoPlayerViewModel.State.Starting -> vlcPlayer.doOnLayout {
                    launchVideo(viewModel.savedState.get<Int>(viewModel.EXTRA_CURR_TIME))
                }
                VideoPlayerViewModel.State.Playing -> {
                    snackBar?.dismiss()
                    mMediaPlayer.play()
                    viewModel.playTimer.start()
                }
                VideoPlayerViewModel.State.Pausing -> {
                    mMediaPlayer.pause()
                    viewModel.playTimer.pause()
                }
                VideoPlayerViewModel.State.Stopping -> {
                    mMediaPlayer.stop()
                    mMediaPlayer.detachViews()
                    viewModel.savedState.set(viewModel.EXTRA_CURR_TIME, viewModel.currPlaybackTime.value)
                }
            }
        })
        viewModel.playTimer.time.observe(viewLifecycleOwner,{
            val strKey = getString(R.string.statistics_media_time_key)
            val strDefault = getString(R.string.statistics_media_time_default_value)
            val currValue = sharedPreferences.getString(strKey, strDefault)?.toLong() ?: 0L
            val newValue = currValue + it.value
            sharedPreferences.edit().putString(strKey, newValue.toString()).apply()
        })
    }

    override fun onResume() {
        if(viewModel.state.value == VideoPlayerViewModel.State.Stopping){
            viewModel.state.postValue(VideoPlayerViewModel.State.Starting)
        }
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.state.postValue(VideoPlayerViewModel.State.Stopping)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<MaterialToolbar>(R.id.mtbMain).visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer.release()
        mLibVLC.release()
        updateMediaStats()
        viewModel.playTimer.stop()
    }

    private fun launchVideo(int: Int? = 0) {
        mMediaPlayer.run {
            attachViews(vlcPlayer, null, false, false)
            setEventListener(viewModel.eventListener)

            try {
                val uri = arguments?.getParcelable<Uri>(EXTRA_VIDEO_URI) ?: return
                media = Media(mLibVLC, requireContext().contentResolver.openFileDescriptor(uri, "r")?.fileDescriptor ?: return)
                media?.release()

            } catch (e: IOException) {
                throw RuntimeException("Invalid asset folder")
            }
            viewModel.state.postValue(VideoPlayerViewModel.State.Playing)
            //Todo: Think!!!
            viewModel.executor.execute {
                Thread.sleep(100)
                time = int?.toLong() ?: 0L
            }
        }
    }

    fun parseSubtitle(){
        val subsUri = arguments?.getParcelable<Uri>(EXTRA_SUBTITLES_URI) ?: return
        val fd = requireContext().contentResolver.openFileDescriptor(subsUri, "r", null)?.fileDescriptor

       val brandNewFile =  File(requireContext().cacheDir.absolutePath + "/subsCache", "custom.sub").apply {
            mkdirs()
            if (exists()) delete()
            createNewFile()
            outputStream().use { os ->
                FileInputStream(fd).use {
                    fis -> fis.copyTo(os)
                }
            }
        }

        SubtitleParser.getInstance().run {
            setSubtitleParserListener(viewModel.iSubtitleParserListener)
            parseSubtitle(brandNewFile.absolutePath, "ru", "")
        }
    }

    private fun updateMediaStats(){
        val strKey = getString(R.string.statistics_media_total_key)
        val strDefault = getString(R.string.statistics_media_total_default_value)
        val currValue = sharedPreferences.getString(strKey, strDefault)?.toInt() ?: 0
        val newValue = currValue + 1

        sharedPreferences.edit().putString(strKey, newValue.toString()).apply()
    }
}