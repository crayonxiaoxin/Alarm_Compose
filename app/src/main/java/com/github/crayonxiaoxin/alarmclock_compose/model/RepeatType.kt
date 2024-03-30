package com.github.crayonxiaoxin.alarmclock_compose.model

import com.github.crayonxiaoxin.alarmclock_compose.App
import com.github.crayonxiaoxin.alarmclock_compose.R
import java.io.Serializable

/**
 * 闹钟 - 重复类型
 */
data class RepeatType(
    val key: String,
    val name: String,
    var customWeekdayList: List<Int> = arrayListOf() // 自定义才需要填写这个值
) : Serializable {

    fun isOnce(): Boolean = key == Once
    fun isEveryDay(): Boolean = key == EveryDay
    fun isWeekDay(): Boolean = key == WeekDay
    fun isCustom(): Boolean = key == Custom

    fun nameFmt(): String {
        if (isCustom()) {
            if (customWeekdayList.isNotEmpty()) {
                val alias = customWeekdayList.map { key ->
                    weekdayList.find { it.key == key }?.alia ?: ""
                }
                return alias.joinToString(separator = " ")
            }
        }
        return name
    }

    /**
     * 星期几
     */
    data class WeekDay(
        val key: Int,
        val name: String,
        val alia: String = ""
    )

    companion object {
        const val Once = "once"
        const val EveryDay = "everyday"
        const val WeekDay = "weekday"
        const val Custom = "custom"

        /**
         * 可用的重复类型列表
         */
        val list = arrayListOf(
            RepeatType(Once, App.appContext.getString(R.string.repeat_type_once)),
            RepeatType(EveryDay, App.appContext.getString(R.string.repeat_type_everyday)),
            RepeatType(WeekDay, App.appContext.getString(R.string.repeat_type_weekday)),
            RepeatType(Custom, App.appContext.getString(R.string.repeat_type_custom)),
        )

        /**
         * 可用的星期列表
         */
        val weekdayList = arrayListOf(
            WeekDay(
                key = 2,
                name = App.appContext.getString(R.string.monday),
                alia = App.appContext.getString(R.string.monday_alia)
            ),
            WeekDay(
                key = 3,
                name = App.appContext.getString(R.string.tuesday),
                alia = App.appContext.getString(R.string.tuesday_alia)
            ),
            WeekDay(
                key = 4,
                name = App.appContext.getString(R.string.wednesday),
                alia = App.appContext.getString(R.string.wednesday_alia)
            ),
            WeekDay(
                key = 5,
                name = App.appContext.getString(R.string.thursday),
                alia = App.appContext.getString(R.string.thursday_alia)
            ),
            WeekDay(
                key = 6,
                name = App.appContext.getString(R.string.friday),
                alia = App.appContext.getString(R.string.friday_alia)
            ),
            WeekDay(
                key = 7,
                name = App.appContext.getString(R.string.saturday),
                alia = App.appContext.getString(R.string.saturday_alia)
            ),
            WeekDay(
                key = 1,
                name = App.appContext.getString(R.string.sunday),
                alia = App.appContext.getString(R.string.sunday_alia)
            ),
        )
    }
}

