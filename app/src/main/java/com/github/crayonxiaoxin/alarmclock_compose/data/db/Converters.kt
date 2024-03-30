package com.github.crayonxiaoxin.alarmclock_compose.data.db

import androidx.room.TypeConverter
import com.github.crayonxiaoxin.alarmclock_compose.model.RepeatType
import com.google.gson.Gson

/**
 * Room 数据库 - 特殊字段转换器
 */
class Converters {

    @TypeConverter
    fun fromRepeatType2String(value: RepeatType?): String {
        if (value == null) return ""
        return Gson().toJson(value)
    }

    @TypeConverter
    fun string2RepeatType(value: String?): RepeatType {
        if (value.isNullOrEmpty()) return RepeatType.list.first()
        return try {
            Gson().fromJson(value, RepeatType::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            RepeatType.list.first()
        }
    }
}