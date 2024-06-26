package com.github.crayonxiaoxin.alarmclock_compose.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.IntRange
import com.github.crayonxiaoxin.alarmclock_compose.App
import com.github.crayonxiaoxin.alarmclock_compose.R
import com.github.crayonxiaoxin.alarmclock_compose.model.Alarm
import com.github.crayonxiaoxin.alarmclock_compose.model.RepeatType
import com.github.crayonxiaoxin.alarmclock_compose.receiver.AlarmReceiver
import com.github.crayonxiaoxin.alarmclock_compose.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * 数据仓库
 */
object Repository {
    private val db = App.db
    val alarmDao = db.alarmDao()

    /**
     * 设置闹钟
     */
    suspend fun setAlarm(
        alarm: Alarm,
        callback: () -> Unit = {}
    ) {
        val context = App.appContext
        // 记录在数据库中
        val insertIds = alarmDao.insert(alarm)
        Log.e("TAG", "setAlarm: ")
        if (insertIds.isNotEmpty()) {
            val alarm = alarmDao.get(insertIds[0].toInt())
            if (alarm != null) {
                // 运行第一个周期
                setAlarmCycleTask(alarm)

                // 返回
                withContext(Dispatchers.Main) {
                    callback.invoke()
                }
            }
        } else {
            context.toast("未知错误")
        }
    }

    fun setAlarmCycleTask(alarm: Alarm) {
        val context = App.appContext
        val requestCode = alarm.requestCode()
        val now = System.currentTimeMillis()
        val hour = alarm.hour
        val minute = alarm.minute
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        // 闹钟时间
        val alarmTime = calendar.timeInMillis

        val newTimestamp = if (now <= alarmTime) { // 未到时间，正常执行
            Log.e("TAG", "setAlarm: 未到时间，正常执行")
            alarmTime
        } else { // 已过时间，执行下一个周期
            Log.e("TAG", "setAlarm: 已过时间，执行下一个周期")
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.timeInMillis
        }

        // 设置闹钟
        AlarmReceiver.setAlarmClock(
            context = context,
            timestamp = newTimestamp,
            requestCode = requestCode,
        )
    }

    // 更新闹钟
    suspend fun updateAlarm(alarm: Alarm) {
        // 更新 Alarm
        alarmDao.update(alarm)
        if (alarm.isEnable()) {
            setAlarmCycleTask(alarm)
        } else {
            cancelAlarm(alarm)
        }
    }

    // 取消闹钟并停止音乐
    private fun cancelAlarm(alarm: Alarm) {
        AlarmReceiver.unsetAlarmClock(
            requestCode = alarm.requestCode()
        )
    }

    // 仅取消通知和音乐
    fun removeNotificationAndMusicOnly(alarm: Alarm) {
        AlarmReceiver.removeNotify(
            requestCode = alarm.requestCode()
        )
    }

    // 删除闹钟
    suspend fun unsetAlarm(alarm: Alarm) {
        val context = App.appContext
        cancelAlarm(alarm)
        // 删除记录
        alarmDao.delete(alarm)
        context.toast(context.getString(R.string.alarm_canceled))
    }

    suspend fun unsetAlarms(list: List<Alarm>) {
        list.forEach {
            cancelAlarm(it)
        }
        // 删除记录
        alarmDao.delete(*list.toTypedArray())
    }
}