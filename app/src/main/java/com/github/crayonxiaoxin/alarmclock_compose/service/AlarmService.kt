package com.github.crayonxiaoxin.alarmclock_compose.service

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.github.crayonxiaoxin.alarmclock_compose.BuildConfig
import com.github.crayonxiaoxin.alarmclock_compose.MainActivity
import com.github.crayonxiaoxin.alarmclock_compose.R
import com.github.crayonxiaoxin.alarmclock_compose.data.Repository
import com.github.crayonxiaoxin.alarmclock_compose.model.Alarm
import com.github.crayonxiaoxin.alarmclock_compose.receiver.AlarmReceiver
import com.github.crayonxiaoxin.alarmclock_compose.utils.AudioManager
import com.github.crayonxiaoxin.alarmclock_compose.utils.NotificationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

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
        stopForeground(STOP_FOREGROUND_REMOVE)
        unregisterReceiver(alarmReceiver)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        configForeground()  // 前台服务
        configReceiver()    // 广播接收
        return START_STICKY
    }

    private fun configForeground() {
        val pendingIntent = NotificationUtil.pendingIntent(
            intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        )
        val notification = NotificationUtil.show(
            title = "前台服务运行中",
            autoCancel = false,
            contentIntent = pendingIntent,
            show = false
        )
        startForeground(BuildConfig.VERSION_CODE, notification)
    }

    private val alarmReceiver = object : AlarmReceiver() {
        private val TAG = "AlarmReceiver"

        override fun onReceive(context: Context?, intent: Intent?) {
            Log.e(TAG, "onReceive: $intent")
            if (context == null || intent == null) return
            when (intent.action) {
                ACTION_ALARM_CLOCK -> {
                    Log.e(TAG, "onReceive: 收到设置的闹钟")
                    val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, 0)
                    alarmServiceScope.launch {
                        val alarm = Repository.alarmDao.get(alarmId) ?: return@launch

                        val calendar = Calendar.getInstance()
                        // 今天是星期几
                        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 从 1:星期天 开始
                        if (alarm.isEnable()) {
                            if (alarm.repeatType.isWeekDay()) { // 周一至周五
                                val range = 2..6
                                if (!range.contains(dayOfWeek)) { // 今天不在周一至周五范围
                                    return@launch
                                }
                            } else if (alarm.repeatType.isCustom()) { // 自定义星期几
                                val range = alarm.repeatType.customWeekdayList
                                if (!range.contains(dayOfWeek)) { // 今天不在自定义星期范围
                                    return@launch
                                }
                            }
                            // 符合条件，发出通知并播放音乐
                            // 发出通知
                            val pendingIntent = NotificationUtil.pendingIntent(Intent(
                                context, MainActivity::class.java
                            ).apply {
                                this.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                this.action = ACTION_ALARM_CLOCK + "_receive"
                            })
                            val title = alarm.remark.ifEmpty { getString(R.string.app_name) }
                            NotificationUtil.show(
                                title,
                                alarm.content(),
                                alarm.requestCode(),
                                contentIntent = pendingIntent
                            )
                            // 获取音乐 uri
                            val musicUri = alarm.toUri()
                            Log.e(TAG, "onReceive: uri => $musicUri")
                            // 播放音乐
                            musicUri?.let {
                                AudioManager.playMp3FromUri(this@AlarmService, musicUri, true)
                            }
                            // 做 callback 提醒 ui
                            onAlarmTriggered(alarm)
                            // 为重复闹钟继续发送广播
                            if (!alarm.repeatType.isOnce()) {
                                // 稍微做个延迟，防止马上触发，导致死循环
                                delay(3000L)
                                Repository.setAlarmCycleTask(alarm)
                            }
                        }
                    }
                }

                else -> {
                    Log.e(TAG, "$intent")
                }
            }

        }
    }


    private fun configReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(AlarmReceiver.ACTION_ALARM_CLOCK)
            addCategory("android.intent.category.DEFAULT")
            priority = Int.MAX_VALUE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(alarmReceiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(alarmReceiver, intentFilter)
        }
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
                                alarmServiceScope.launch {
                                    if (alarm.repeatType.isOnce()) { // 不重复闹钟才关闭，重复闹钟依然保持开启
                                        Repository.updateAlarm(
                                            alarm.copy(enable = 0)
                                        )
                                    } else { // 只需要移除 notification 和 停止音乐
                                        Repository.removeNotificationAndMusicOnly(
                                            alarm
                                        )
                                    }
                                }
                                windowManager.removeView(this@apply)
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
        val title = alarm.remark.ifEmpty { getString(R.string.app_name) }
        Column(
            modifier = Modifier
                .padding(20.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
                .background(color = MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Text(
                text = title,
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