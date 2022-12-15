package com.hxl.android.xiaoan.word.utils

import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.QUEUE_FLUSH
import android.util.Log
import com.hxl.android.xiaoan.word.WordApplication
import com.hxl.android.xiaoan.word.net.Net
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.MessageFormat
import java.util.*
import kotlin.concurrent.thread
import kotlin.io.path.exists

enum class PlayLan(val typeValue: String) {
    EN("en"), UK("uk")
}

private fun interface TTSPlayer {
    fun player(text: String, type: PlayLan): Boolean
}

private class Player {
    companion object {
        fun player(mp3File: File) {
            try {
                createMediaPlayer().apply {
                    this.setDataSource(FileInputStream(mp3File).fd)
                    this.prepare()
                    this.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun createMediaPlayer(): MediaPlayer = MediaPlayer()
    }

}


private abstract class BasePlayer {

    fun generatorCacheKey(playLan: PlayLan, text: String): String {
        return "${playLan.typeValue}${text.trim()}.mp3"
    }

    fun downloadAndPlay(url: String, type: PlayLan, text: String): Boolean {
        try {
            val context = WordApplication.context
            val mp3File =
                Paths.get(context.cacheDir.toString(), generatorCacheKey(type, text))
            val mp3Bytes = Net.download(url)
            if (mp3Bytes != null) {
                Files.write(mp3File, mp3Bytes)
                Player.player(mp3File.toFile())
                return true
            }
        }catch (e:Exception){}
        return false
    }
}

/**
 * 先从缓存播放
 */
private class CachePlayer : BasePlayer(), TTSPlayer {
    override fun player(text: String, type: PlayLan): Boolean {
        val mp3File =
            Paths.get(WordApplication.context.cacheDir.toString(), generatorCacheKey(type, text))
        if (!mp3File.exists()) return false
        Player.player(mp3File.toFile())
        return true
    }
}

private class NetworkPlayer() {
    companion object {
        private const val BAIDU_TTS_EN = "https://fanyi.baidu.com/gettts?lan=en&text={0}&spd=3&source=web"
        private const val BAIDU_TTS_UK = "https://fanyi.baidu.com/gettts?lan=uk&text={0}&spd=3&source=web"

        private const val YOUDAO_TTS_UK = "https://dict.youdao.com/dictvoice?audio={0}&type=1"
        private const val YOUDAO_TTS_EN = "http://dict.youdao.com/dictvoice?type=0&audio={0}"

        fun format(url: String, arg: String): String {
            return MessageFormat.format(url, arg)
        }
    }

    class BaiduPlayer : BasePlayer(), TTSPlayer {
        override fun player(text: String, type: PlayLan): Boolean {
            val ttsUrl = if (type == PlayLan.EN) BAIDU_TTS_EN else BAIDU_TTS_UK
            return downloadAndPlay(format(ttsUrl,text), type, text)
        }
    }
    class YouDaoPlayer : BasePlayer(), TTSPlayer {
        override fun player(text: String, type: PlayLan): Boolean {
            val ttsUrl = if (type == PlayLan.EN) YOUDAO_TTS_EN else YOUDAO_TTS_UK
            return downloadAndPlay(format(ttsUrl,text), type, text)
        }
    }
}

private class LocalTextToSpeech() : TTSPlayer, TextToSpeech.OnInitListener {
    private val textToSpeech = TextToSpeech(WordApplication.context, this)
    override fun player(text: String, type: PlayLan): Boolean {
        textToSpeech.language = if (type == PlayLan.EN) Locale.ENGLISH else Locale.UK
        textToSpeech.speak(text, QUEUE_FLUSH, null, text.trim())
        return true
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale.ENGLISH
        }

    }
}

private abstract class PlayerStep(val iPlayer: TTSPlayer?, val text: String, val type: PlayLan) {
    protected var nextPlayer: PlayerStep? = null
    fun next(iPlayer: TTSPlayer): PlayerStep {
        val newStep = object : PlayerStep(iPlayer, text, type) {}

        var node: PlayerStep? = this
        while (node?.nextPlayer != null) {
            node = node.nextPlayer
        }
        node!!.nextPlayer = newStep
        return this
    }

    fun start() {
        if (iPlayer?.player(text, type) != true) {
            nextPlayer?.start()
        }
    }

    companion object {
        fun player(text: String, type: PlayLan): PlayerStep {
            val first: (String, PlayLan) -> Boolean = { _: String, _: PlayLan -> false }
            return object : PlayerStep(first, text, type) {}
        }
    }
}

class TxtPlayer {
    private val localTextToSpeech = LocalTextToSpeech()
    private val baiduNetworkPlayer = NetworkPlayer.BaiduPlayer()
    private val youdaoNetworkPlayer = NetworkPlayer.YouDaoPlayer()
    fun player(text: String, type: PlayLan) {
        thread {
            val play = PlayerStep.player(text, type)
                .next(CachePlayer())
                .next(baiduNetworkPlayer)
                .next(youdaoNetworkPlayer)
                .next(localTextToSpeech)
            play.start()
        }
    }


}