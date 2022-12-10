package com.hxl.android.xiaoan.word.utils

import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.QUEUE_FLUSH
import android.util.Log
import android.widget.Toast
import com.hxl.android.xiaoan.word.WordApplication
import com.hxl.android.xiaoan.word.net.Net
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.text.MessageFormat
import java.util.*
import kotlin.io.path.exists

private fun interface IPlayer {
    fun player(text: String): Boolean
}

private class NetworkPlayer() : IPlayer {
    companion object {
        private const val BAIDU_TTS =
            "https://fanyi.baidu.com/gettts?lan=en&text={0}&spd=3&source=web"

        private fun format(arg: String): String {
            return MessageFormat.format(BAIDU_TTS, arg)
        }
    }

    private fun createMediaPlayer(): MediaPlayer = MediaPlayer()
    override fun player(text: String): Boolean {
        val context = WordApplication.context
        val mp3File = Paths.get(context.cacheDir.toString(), "${text.trim()}.mp3")

        //如果缓存不存在
        if (!mp3File.exists()) {
            //下载tts失败
            try {
                val mp3Bytes = Net.download(format(text)) ?: return false
                if (mp3Bytes.isEmpty()) return false
                Files.write(mp3File, mp3Bytes)
            }catch (e:Exception){
                return false
            }
        }
        try {
            createMediaPlayer().apply {
                this.setDataSource(FileInputStream(mp3File.toFile()).fd)
                this.prepare()
                this.start()
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}

private class LocalTextToSpeech() : IPlayer, TextToSpeech.OnInitListener {
    private val textToSpeech = TextToSpeech(WordApplication.context, this)
    override fun player(text: String): Boolean {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.trim())
        return true
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale.ENGLISH
        }

    }
}

private abstract class PlayerStep(val iPlayer: IPlayer?, val text: String) {
    protected var nextPlayer: PlayerStep? = null

    fun next(iPlayer: IPlayer): PlayerStep {
        val newStep = object : PlayerStep(iPlayer, text) {}

        var node: PlayerStep? = this
        while (node?.nextPlayer != null) {
            node = this.nextPlayer
        }
        node!!.nextPlayer = newStep
        return this
    }

    fun start() {
        if (iPlayer?.player(text) != true) {
            nextPlayer?.start()
        }
    }

    companion object {
        fun player(text: String): PlayerStep {
            val a: (String) -> Boolean = {
                false
            }
            return object : PlayerStep(a, text) {}
        }
    }
}

class TxtPlayer {
    private val localTextToSpeech = LocalTextToSpeech()
    private val networkPlayer = NetworkPlayer()
    fun player(text: String) {
        val play = PlayerStep.player(text)
            .next(networkPlayer)
            .next(localTextToSpeech)
        play.start()
    }


}