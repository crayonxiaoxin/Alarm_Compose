package com.github.crayonxiaoxin.alarmclock_compose.model

import android.content.Context
import android.net.Uri
import androidx.annotation.IntRange
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.crayonxiaoxin.alarmclock_compose.App
import com.github.crayonxiaoxin.alarmclock_compose.R
import com.github.crayonxiaoxin.alarmclock_compose.utils.getFileNameFromUri
import java.io.Serializable

/**
 * 闹钟对象
 */
@Entity
data class Alarm(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @IntRange(from = 0, to = 23) var hour: Int = 0, // 小时
    @IntRange(from = 0, to = 59) var minute: Int = 0, // 分钟
    var repeatType: RepeatType = RepeatType.list.first(), // 重复间隔
    var uri: String?, // 音乐地址
    var remark: String = "", // 备注
    @IntRange(from = 0, to = 1) var enable: Int,// 是否启用
) : Serializable {
    fun time(): String {
        return String.format("%02d", hour) + ":" + String.format("%02d", minute)
    }

    fun toUri(): Uri? {
        return if (uri.isNullOrEmpty()) {
            null
        } else {
            Uri.parse(uri)
        }
    }

    fun music(context: Context): String = toUri()?.let { context.getFileNameFromUri(it) } ?: ""
    fun isEnable(): Boolean = enable == 1
    fun requestCode(): Int = id + requestCodeDiff()
    fun content(): String = App.appContext.getString(R.string.alarm_time_arrived, time())

    fun repeatTypeName(): String {
        return repeatType.nameFmt()
    }

    companion object {
        private fun requestCodeDiff(): Int = 1000

        fun idFromRequestCode(requestCode: Int): Int {
            val id = requestCode - requestCodeDiff()
            if (id > 0) return id
            return 0
        }

        fun repeatTypeList(): List<RepeatType> {
            return RepeatType.list
        }
    }
}
