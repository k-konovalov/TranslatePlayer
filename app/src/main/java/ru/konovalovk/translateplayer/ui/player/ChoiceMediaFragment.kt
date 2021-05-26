package ru.konovalovk.translateplayer.ui.player

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import ru.konovalovk.translateplayer.R

const val EXTRA_VIDEO_URI = "EXTRA_VIDEO_URI"
const val EXTRA_SUBTITLES_URI = "EXTRA_SUBTITLES_URI"

class ChoiceMediaFragment: Fragment(R.layout.fragment_choice_media) {
    val mbPlusVideo by lazy { requireView().findViewById<MaterialButton>(R.id.mbPlusVideo) }
    val tieVideo by lazy { requireView().findViewById<TextInputEditText>(R.id.tieVideo) }
    val mbPlusAudio by lazy { requireView().findViewById<MaterialButton>(R.id.mbPlusAudio) }
    val tieAudio by lazy { requireView().findViewById<TextInputEditText>(R.id.tieAudio) }
    val mbPlusSubtitles by lazy { requireView().findViewById<MaterialButton>(R.id.mbPlusSubtitles) }
    val tieSubtitles by lazy { requireView().findViewById<TextInputEditText>(R.id.tieSubtitles) }
    val mbNext by lazy { requireView().findViewById<MaterialButton>(R.id.mbNext) }

    val bundle = Bundle()
    var currentUriType = ContentType.Video
    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Handle the returned Uri
        val fileName = uri.toString().split("%2F").last()
        when(currentUriType){
            ContentType.Video -> {
                bundle.putParcelable(EXTRA_VIDEO_URI, uri)
                tieVideo.setText(fileName)
            }
            ContentType.Audio -> {
                TODO()
                tieAudio.setText(fileName)
            }
            ContentType.Subtitles -> {
                bundle.putParcelable(EXTRA_SUBTITLES_URI, uri)
                tieSubtitles.setText(fileName)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initOnClickListeners()
    }

    fun initOnClickListeners(){
        mbPlusVideo.setOnClickListener {
            currentUriType = ContentType.Video
            getContent.launch("video/*")
        }
        mbPlusAudio.setOnClickListener {
            currentUriType = ContentType.Audio
            getContent.launch("audio/*")
        }
        mbPlusSubtitles.setOnClickListener {
            currentUriType = ContentType.Subtitles
            getContent.launch("*/*")
        }
        mbNext.setOnClickListener {
            findNavController().navigate(R.id.videoPlayerFragment, bundle)
        }
    }
 enum class ContentType{
     Video, Audio, Subtitles
 }
}
