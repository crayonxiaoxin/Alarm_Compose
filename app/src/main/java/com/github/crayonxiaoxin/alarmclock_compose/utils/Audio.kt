package com.github.crayonxiaoxin.alarmclock_compose.utils

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object AudioManager {
    private val mediaPlayer by lazy { MediaPlayer() }

    private var job: Job? = null

    /**
     * 从 uri 播放音乐
     */
    fun playMp3FromUri(context: Context, uri: Uri, isLoop: Boolean = false) {
        job?.cancel()
        context.grantUriPermission(context.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        job = CoroutineScope(Dispatchers.IO).launch {
            mediaPlayer.apply {
                reset()
                setAudioAttributes(
                    AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(context, uri)
                prepareAsync()
                isLooping = isLoop
                setOnPreparedListener {
                    it.start()
                }
            }
        }
    }

    /**
     * 停止播放
     */
    fun stopMp3() {
        job?.cancel()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        stopMp3()
        mediaPlayer?.release()
    }
}


