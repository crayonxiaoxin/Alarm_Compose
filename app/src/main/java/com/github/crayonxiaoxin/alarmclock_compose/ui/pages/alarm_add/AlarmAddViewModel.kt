package com.github.crayonxiaoxin.alarmclock_compose.ui.pages.alarm_add

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.crayonxiaoxin.alarmclock_compose.App
import com.github.crayonxiaoxin.alarmclock_compose.R
import com.github.crayonxiaoxin.alarmclock_compose.data.Repository
import com.github.crayonxiaoxin.alarmclock_compose.model.Alarm
import com.github.crayonxiaoxin.alarmclock_compose.model.RepeatType
import com.github.crayonxiaoxin.alarmclock_compose.utils.copyFile
import com.github.crayonxiaoxin.alarmclock_compose.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmAddViewModel : ViewModel() {
    val selectedTime = mutableStateOf("")
    val uri = mutableStateOf<Uri?>(null)
    val repeatType = mutableStateOf(
        RepeatType.list.first()
    )
    val remark = mutableStateOf("")
    var alarm: Alarm? = null

    fun initAdd() {
        selectedTime.value = ""
        uri.value = null
        remark.value = ""
        repeatType.value = RepeatType.list.first()
        alarm = null
    }

    fun initEdit(alarm: Alarm?) {
        if (alarm == null) {
            initAdd()
        } else {
            selectedTime.value = alarm.time()
            uri.value = alarm.toUri()
            remark.value = alarm.remark
            repeatType.value = alarm.repeatType
            this.alarm = alarm
        }
    }

    fun submit(onBack: () -> Unit = {}) {
        val context = App.appContext
        if (uri.value == null) {
            context.toast(context.getString(R.string.pls_pick_music))
        } else {
            if (alarm == null) { // 新增
                // 保存文件到内部存储
                uri.value = context.copyFile(uri.value)
                // 设置闹钟并保存到数据库
                val time = selectedTime.value.split(":")
                viewModelScope.launch(Dispatchers.IO) {
                    Repository.setAlarm(
                        alarm = Alarm(
                            hour = time[0].toInt(),
                            minute = time[1].toInt(),
                            repeatType = repeatType.value,
                            uri = uri.value?.toString(),
                            remark = remark.value,
                            enable = 1,
                        ),
                    ) {
                        onBack()
                    }
                }
            } else { // 修改
                // 保存文件到内部存储
                if (uri.value != alarm!!.toUri()) { // 与之前不同才需要保存
                    uri.value = context.copyFile(uri.value)
                }
                // 设置闹钟并保存到数据库
                val time = selectedTime.value.split(":")
                viewModelScope.launch(Dispatchers.IO) {
                    Repository.updateAlarm(
                        alarm = alarm!!.copy(
                            hour = time[0].toInt(),
                            minute = time[1].toInt(),
                            repeatType = repeatType.value,
                            uri = uri.value.toString(),
                            remark = remark.value,
                        )
                    )
                    withContext(Dispatchers.Main) {
                        onBack()
                    }
                }
            }
        }
    }
}