package ru.konovalovk.translateplayer.ui

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData

class PlayTimer {
    val TAG = this::class.java.simpleName

    val time = MutableLiveData<MilliSeconds>()
    private var currentMediaPlayTime = 0L
    private val second = 1000L
    private val timer = object: CountDownTimer(Int.MAX_VALUE.toLong(), second){
        override fun onTick(millisUntilFinished: Long) {
            currentMediaPlayTime += second
        }

        override fun onFinish() {}
    }

    fun start(){
        timer.start()
    }

    fun pause(){
        timer.cancel()
        time.postValue(MilliSeconds(currentMediaPlayTime))
    }

    fun stop(){
        currentMediaPlayTime = 0L
    }

    data class MilliSeconds(
        var value: Long
    )


}