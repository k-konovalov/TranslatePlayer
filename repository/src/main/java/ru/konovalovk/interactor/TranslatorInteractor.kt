package ru.konovalovk.interactor

import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import ru.konovalovk.repository.db.AppDatabase
import ru.konovalovk.repository.db.entity.Library
import ru.konovalovk.repository.network.NetworkModule

class TranslatorInteractor {
    private val TAG = this::class.java.simpleName
    private val ioScope = CoroutineScope(Dispatchers.IO)
    val translatedWord = MutableLiveData<String>()

    val db = AppDatabase.instance

    fun translateWord(text: String) {
        ioScope.launch {
            val result = NetworkModule.googleApi.getWordTranslation("en", "ru", text.lowercase())
            val str = result.string()
            Log.e(TAG, str)
        }
    }

    fun translatePhrase(text: String, translator: Translator) {
        ioScope.launch {
            val originalLanguage = "en"
            val translatedLanguage = "ru"
            val improvedText = text.lowercase().replace("\n", "")
            val result = when(translator){
                Translator.Google -> {
                    NetworkModule.googleApi
                        .getPhraseTranslation(originalLanguage, translatedLanguage, improvedText)
                        .parseGoogleResult()
                }
            }
            translatedWord.postValue(result)
        }
    }



    fun saveWordToDb(originalWord: String, translatedWord: String){

    }

    //ToDdo: to new class Translator /
    //Example - [[["интересно, где он сейчас ...","i wonder where he is right now...",null,null
    private fun ResponseBody.parseGoogleResult(): String {
        val regex = Regex("\".+\"")
        val founded = regex.find(string())
        val searchedValue = founded?.value ?: "empty"
        val splitted = searchedValue.split("\",\"")
        if (splitted.isNotEmpty()) {
            val res = splitted.first().let { it.substring(1, it.length) }
            Log.i(TAG, "Translated res: $res")
            return res//surroundings
        }
        return "Error: Empty translation result"
    }

    enum class Translator {
        Google
    }
}