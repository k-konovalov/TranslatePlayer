package ru.konovalovk.translateplayer.ui.player

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.view.doOnLayout
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
    private val vlcPlayer by lazy { requireView().findViewById<VLCVideoLayout>(R.id.vlcPlayer) }
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
        viewModel.state.observe(viewLifecycleOwner, {
            when(it){
                VideoPlayerViewModel.State.Ready -> {

                }
                VideoPlayerViewModel.State.LaunchVideo -> vlcPlayer.doOnLayout {
                    launchVideo()
                }
            }
        })
    }

    private fun launchVideo() {
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
        }
    }
}