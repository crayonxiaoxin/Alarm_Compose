package com.github.crayonxiaoxin.alarmclock_compose.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import com.github.crayonxiaoxin.alarmclock_compose.App
import com.github.crayonxiaoxin.alarmclock_compose.BuildConfig
import com.github.crayonxiaoxin.alarmclock_compose.model.Alarm
import com.github.crayonxiaoxin.alarmclock_compose.service.AlarmService
import com.github.crayonxiaoxin.alarmclock_compose.utils.AudioManager
import com.github.crayonxiaoxin.alarmclock_compose.utils.NotificationUtil
import java.util.Calendar

/**
 * 闹钟 - 广播接收器
 */
open class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"

        // 设备启动
        const val ACTION_BOOT = "android.intent.action.BOOT_COMPLETED"

        // 自定义
        const val ACTION_ALARM_CLOCK = BuildConfig.APPLICATION_ID + "." + "action_alarm_clock"

        const val EXTRA_ALARM_ID = "alarm_id"

        // 设置闹钟
        fun setAlarmClock(
            context: Context?,
            timestamp: Long = System.currentTimeMillis(),
            requestCode: Int = 0,
        ) {
            context?.let {
                val alarmManager = it.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                if (alarmManager != null) {
                    // 设置意图
                    val intent = Intent(it, AlarmReceiver::class.java).apply {
                        setPackage(it.packageName)
                        action = ACTION_ALARM_CLOCK
                        putExtra(EXTRA_ALARM_ID, Alarm.idFromRequestCode(requestCode))
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        it,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    // 如果意图已存在，先取消，防止重复设置
                    if (pendingIntent != null) {
                        alarmManager.cancel(pendingIntent)
                    }
                    // 根据实际情况设置 Alarm 类型
                    val calendar: Calendar = Calendar.getInstance().apply {
                        timeInMillis = timestamp
                    }

                    val now = System.currentTimeMillis()
                    val next = calendar.timeInMillis
                    var delay = next - now
                    delay = if (delay > 0) delay else 0
                    val triggerAtTime = SystemClock.elapsedRealtime() + delay
                    // 保证低电耗模式运行 [此处不能使用 setRepeating，高版本不会被执行]
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent
                    )

                    Log.e(TAG, "setAlarm: ")
                }
            }
        }

        // 取消闹钟
        fun unsetAlarmClock(
            requestCode: Int = 0,
        ) {
            App.appContext.let {
                val alarmManager = it.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                if (alarmManager != null) {
                    // 设置意图
                    val intent = Intent(it, AlarmReceiver::class.java).apply {
                        setPackage(it.packageName)
                        action = ACTION_ALARM_CLOCK
                        putExtra(EXTRA_ALARM_ID, Alarm.idFromRequestCode(requestCode))
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        it,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    // 如果意图已存在，先取消
                    if (pendingIntent != null) {
                        // 取消任务
                        alarmManager.cancel(pendingIntent)
                        // 取消通知和响铃
                        removeNotify(requestCode)
                    }
                }
            }
        }

        // 取消通知和响铃
        fun removeNotify(
            requestCode: Int = 0,
        ) {
            // 取消音乐
            AudioManager.stopMp3()
            // 取消通知
            NotificationUtil.hide(requestCode)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        when (intent.action) {
            ACTION_BOOT -> {
                // Set the alarm here when the device boot.
            }

            ACTION_ALARM_CLOCK -> {
                Log.e(TAG, "onReceive: 收到设置的闹钟 - 转发")
                // 转发给动态注册的 receiver
                context.sendBroadcast(Intent().apply {
                    action = ACTION_ALARM_CLOCK
                    intent.extras?.let { putExtras(it) }
                })
            }

            else -> {
                Log.e(TAG, "$intent")
            }
        }

    }

    private fun startService(context: Context, it: Bundle) {
        Log.e(TAG, "startService: 符合条件 - 触发闹钟")
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtras(it)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun stopService(context: Context) {
        val serviceIntent = Intent(context, AlarmService::class.java)
        context.stopService(serviceIntent)
    }
}