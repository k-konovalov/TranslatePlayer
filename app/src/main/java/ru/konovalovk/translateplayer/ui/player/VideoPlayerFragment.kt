package ru.konovalovk.translateplayer.ui.player

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
            if (viewModel.state.value == VideoPlayerViewModel.State.Pausing) viewModel.state.postValue(VideoPlayerViewModel.State.Playing)
            else viewModel.state.postValue(VideoPlayerViewModel.State.Pausing)
        }
    } }
    val mLibVLC by lazy { LibVLC(requireContext(), ArrayList<String>().apply { add("-vvv") }) }
    val mMediaPlayer by lazy { MediaPlayer(mLibVLC) }

    val tvSubtitle by lazy { requireView().findViewById<TextView>(R.id.tvSubtitles).apply {
        setOnClickListener {
            if (viewModel.state.value == VideoPlayerViewModel.State.Pausing) viewModel.state.postValue(VideoPlayerViewModel.State.Playing)
            else viewModel.state.postValue(VideoPlayerViewModel.State.Pausing)
        }
    } }

    val tvTime by lazy { requireView().findViewById<TextView>(R.id.tvTime)}
    val sbTime by lazy { requireView().findViewById<SeekBar>(R.id.sbTime).apply {
        setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (viewModel.state.value == VideoPlayerViewModel.State.Pausing) tvTime.text = viewModel.convertSecondsToHMmSs(progress.toLong())
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
            if(viewModel.lastState == it) return@observe
            viewModel.lastState = it
            when(it){
                VideoPlayerViewModel.State.Ready -> {

                }
                VideoPlayerViewModel.State.Starting -> vlcPlayer.doOnLayout {
                    launchVideo(viewModel.savedState.get<Int>(viewModel.EXTRA_CURR_TIME))
                }
                VideoPlayerViewModel.State.Playing -> {
                    mMediaPlayer.play()
                }
                VideoPlayerViewModel.State.Pausing -> {
                    mMediaPlayer.pause()
                }
                VideoPlayerViewModel.State.Stopping -> {
                    (requireActivity() as AppCompatActivity).supportActionBar?.show()
                    mMediaPlayer.stop()
                    mMediaPlayer.detachViews()
                    viewModel.savedState.set(viewModel.EXTRA_CURR_TIME, viewModel.currPlaybackTime.value)
                }
            }
        })
    }

    override fun onStart() {
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        if(viewModel.state.value == VideoPlayerViewModel.State.Stopping){
            viewModel.state.postValue(VideoPlayerViewModel.State.Starting)
        }
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
        viewModel.state.postValue(VideoPlayerViewModel.State.Stopping)
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
            viewModel.state.postValue(VideoPlayerViewModel.State.Playing)
            //Todo: Think!!!
            viewModel.executor.execute {
                Thread.sleep(100)
                time = int?.toLong() ?: 0L
            }
        }
    }
}