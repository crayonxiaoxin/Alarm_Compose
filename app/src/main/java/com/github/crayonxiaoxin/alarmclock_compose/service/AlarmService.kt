package com.github.crayonxiaoxin.alarmclock_compose.service

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.github.crayonxiaoxin.alarmclock_compose.MainActivity
import com.github.crayonxiaoxin.alarmclock_compose.R
import com.github.crayonxiaoxin.alarmclock_compose.data.Repository
import com.github.crayonxiaoxin.alarmclock_compose.model.Alarm
import com.github.crayonxiaoxin.alarmclock_compose.receiver.AlarmReceiver
import com.github.crayonxiaoxin.alarmclock_compose.utils.AudioManager
import com.github.crayonxiaoxin.alarmclock_compose.utils.NotificationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 闹钟 - 前台服务，播放音乐
 */
class AlarmService : LifecycleService(), LifecycleOwner, SavedStateRegistryOwner {

    private val alarmServiceScope = CoroutineScope(Dispatchers.IO)
    private val TAG = this.javaClass.name
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val alarmId = intent.getIntExtra(AlarmReceiver.EXTRA_ALARM_ID, 0)
            alarmServiceScope.launch {
                // 获取闹钟对象
                val alarm = Repository.alarmDao.get(alarmId) ?: return@launch

                // 发出通知
                val pendingIntent = NotificationUtil.pendingIntent(Intent(
                    this@AlarmService, MainActivity::class.java
                ).apply {
                    this.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    this.action = AlarmReceiver.ACTION_ALARM_CLOCK + "_receive"
                })
                val notification = NotificationUtil.show(
                    title = getString(R.string.app_name),
                    content = alarm.content(),
                    notifyID = alarm.requestCode(),
                    contentIntent = pendingIntent,
                    autoCancel = false,
                    show = false
                )
                startForeground(alarm.requestCode(), notification)

                // 获取音乐 uri
                val musicUri = alarm.toUri()
                Log.e(TAG, "onReceive: uri => $musicUri")
                // 播放音乐
                musicUri?.let {
                    AudioManager.playMp3FromUri(this@AlarmService, musicUri, true)
                }

                // 做 callback 提醒 ui
                onAlarmTriggered(alarm)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun onAlarmTriggered(alarm: Alarm) {
        alarmServiceScope.launch(Dispatchers.Main) {
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            windowManager?.let {
                val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                }
                val layoutParams = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    type,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )

                val dialog = ComposeView(context = this@AlarmService).apply {
                    setViewTreeSavedStateRegistryOwner(this@AlarmService)
                    setViewTreeLifecycleOwner(this@AlarmService)
                    setContent {
                        AlarmDialog(
                            alarm = alarm,
                            onCancelClick = {
                                windowManager.removeView(this)
                                alarmServiceScope.launch {
                                    if (alarm.repeatType.isOnce()) { // 不重复闹钟才关闭，重复闹钟依然保持开启
                                        Repository.updateAlarm(
                                            this@AlarmService,
                                            alarm.copy(enable = 0)
                                        )
                                    } else { // 只需要移除 notification 和 停止音乐
                                        Repository.removeNotificationAndMusicOnly(
                                            this@AlarmService,
                                            alarm
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                windowManager.addView(dialog, layoutParams)
            }
        }
    }

    @Composable
    private fun AlarmDialog(
        alarm: Alarm,
        onCancelClick: () -> Unit = {}
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
                .background(color = MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Text(
                text = getString(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Text(
                text = alarm.content(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.2f
                        ),
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        disabledContainerColor = Color.LightGray,
                        disabledContentColor = Color.White
                    ),
                    onClick = onCancelClick
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        }
    }

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
}