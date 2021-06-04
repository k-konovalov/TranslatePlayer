package ru.konovalovk.translateplayer.ui.export

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.lifecycle.ViewModel
import ru.konovalovk.repository.db.entity.Library
import java.io.File
import java.io.FileInputStream

class ChoiceDestinationViewModel: ViewModel() {
    fun sendToAnki() {
        TODO("Not yet implemented")
    }

    fun sendToLinguaLeo() {
        TODO("Not yet implemented")
    }

    fun sendToTxt(context: Context, words: List<Library>): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val outFolder = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)?.absolutePath
                    ?: return ""
            ).apply { if (!exists()) mkdirs() }
            val outTxt = File(outFolder.absolutePath, "out_${System.currentTimeMillis()}.txt")
            outTxt.outputStream().bufferedWriter().use { bf ->
                words.forEach {
                    val outStr = "${it.originalWord};${it.translatedWord}\n"
                    bf.write(outStr)
                }
            }
            return outTxt.absolutePath
        }
        return ""
    }

    fun sendToPdf() {
        TODO("Not yet implemented")
    }
}