package ru.konovalovk.translateplayer.ui.player

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import ru.konovalovk.subtitle_parser.habib.SubtitleParser
import ru.konovalovk.translateplayer.R
import java.io.IOException

class VideoPlayerFragment : Fragment(R.layout.video_player_fragment) {
    private val viewModel: VideoPlayerViewModel by viewModels()
    private val vlcPlayer by lazy { requireView().findViewById<VLCVideoLayout>(R.id.vlcPlayer).apply {
        setOnClickListener {
            if(isPaused) mMediaPlayer.play()
            else mMediaPlayer.pause()
            isPaused = !isPaused
        }
    } }
    val mLibVLC by lazy { LibVLC(requireContext(), ArrayList<String>().apply { add("-vvv") }) }
    val mMediaPlayer by lazy { MediaPlayer(mLibVLC) }

    var isPaused = false
    val tvSubtitle by lazy { requireView().findViewById<TextView>(R.id.tvSubtitles).apply {
        setOnClickListener {
            if(isPaused) mMediaPlayer.play()
            else mMediaPlayer.pause()
            isPaused = !isPaused
        }
    } }

    val tvTime by lazy { requireView().findViewById<TextView>(R.id.tvTime)}
    val sbTime by lazy { requireView().findViewById<SeekBar>(R.id.sbTime).apply {
        setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(isPaused) tvTime.text = viewModel.convertSecondsToHMmSs(progress.toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val currTime = seekBar?.progress?.toLong() ?: return
                mMediaPlayer.time = currTime
                tvTime.text = viewModel.convertSecondsToHMmSs(currTime)
            }
        })
    }}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val newFile = viewModel.fileFromAssets(requireContext(), "", "naruto.srt") ?: return

        SubtitleParser.getInstance().run {
            setSubtitleParserListener(viewModel.iSubtitleParserListener)
            parseSubtitle(newFile.absolutePath, "ru", "")
        }

        viewModel.liveCurSubtitleContent.observe(viewLifecycleOwner, {
            tvSubtitle.text = it
        })
        viewModel.currPlaybackTime.observe(viewLifecycleOwner,{
            sbTime.progress = it
            if (sbTime.max == 0) sbTime.max = mMediaPlayer.length.toInt()
            tvTime.text = viewModel.convertSecondsToHMmSs(it.toLong())
        })
        viewModel.state.observe(viewLifecycleOwner, {
            if(viewModel.state.value == it) return@observe
            when(it){
                VideoPlayerViewModel.State.Ready -> {

                }
                VideoPlayerViewModel.State.LaunchVideo -> vlcPlayer.doOnLayout {
                    launchVideo(viewModel.savedState.get<Int>(viewModel.EXTRA_CURR_TIME))
                }
                VideoPlayerViewModel.State.PausedVideo -> {
                    mMediaPlayer.stop()
                    mMediaPlayer.detachViews()
                    viewModel.savedState.set(viewModel.EXTRA_CURR_TIME, viewModel.currPlaybackTime.value)
                }
            }
        })
    }

    override fun onStop() {
        super.onStop()
        viewModel.state.postValue(VideoPlayerViewModel.State.PausedVideo)
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer.release()
        mLibVLC.release()
    }

    private fun launchVideo(int: Int? = 0) {
        mMediaPlayer.run {
            attachViews(vlcPlayer, null, false, false)
            setEventListener(viewModel.eventListener)

            try {
                media = Media(mLibVLC, requireContext().assets.openFd("naruto.mkv"))
                media?.release()

            } catch (e: IOException) {
                throw RuntimeException("Invalid asset folder")
            }
            play()
            //Todo: Think!!!
            viewModel.executor.execute {
                Thread.sleep(100)
                time = int?.toLong() ?: 0L
            }
        }
    }
}