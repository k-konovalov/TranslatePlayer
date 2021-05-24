package ru.konovalovk.translateplayer

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import ru.konovalovk.subtitle_parser.habib.SubtitleParser
import ru.konovalovk.subtitle_parser.subs.TimedTextObject
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    val listener = object: SubtitleParser.ISubtitleParserListener{

        @RequiresApi(Build.VERSION_CODES.N)
        override fun onSubtitleParseCompleted(
            isSuccessful: Boolean,
            subtitleFile: TimedTextObject?,
            subtitlePath: String?
        ) {
            if(isSuccessful) subtitleFile?.run {
                var str = "Parsed $subtitlePath:\n"
                captions.forEach { t, u ->
                    str += "Time: ${u.start.milliseconds}-${u.end.milliseconds}\n${u.content}\n"
                }
                findViewById<TextView>(R.id.tvText).text = str
                //Log.e("MainActivity", str)
            } else  Log.e("MainActivity", "Not success")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val newFile = fileFromAssets("","naruto.srt") ?: return
        SubtitleParser.getInstance().run {
            setSubtitleParserListener(listener)
            parseSubtitle(newFile.absolutePath,"ru","")
        }
    }

    fun fileFromAssets(filepath: String, filename: String): File? {
        val directory = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return null
        val out = File(directory.absolutePath, filename).apply {
            mkdirs()
            if (exists()) delete()
            createNewFile()
        }
        assets.open(filepath + filename).use { input ->
            FileOutputStream(out.absolutePath).use { output ->
                input.copyTo(output)
                output.flush()
            }
        }
        return out
    }
}