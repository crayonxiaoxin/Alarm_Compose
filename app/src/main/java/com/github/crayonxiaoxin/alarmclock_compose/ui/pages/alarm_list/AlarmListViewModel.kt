package com.github.crayonxiaoxin.alarmclock_compose.ui.pages.alarm_list

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.crayonxiaoxin.alarmclock_compose.App
import com.github.crayonxiaoxin.alarmclock_compose.data.Repository
import com.github.crayonxiaoxin.alarmclock_compose.model.Alarm
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AlarmListViewModel : ViewModel() {
    val list get() = Repository.alarmDao.getAllAsFlow()

    val isDeleteMode = mutableStateOf(false)

    private val deleteList = mutableStateListOf<Alarm>()

    fun updateAlarm(alarm: Alarm, status: Boolean) {
        viewModelScope.launch {
            Repository.updateAlarm(
                App.appContext,
                alarm.copy(enable = if (status) 1 else 0)
            )
        }
    }

    fun deleteSelectedAlarms() {
        viewModelScope.launch {
            Repository.unsetAlarms(App.appContext, deleteList.toList())
            toggleDeleteMode(false)
        }
    }

    fun toggleDeleteMode(enable: Boolean) {
        isDeleteMode.value = enable
        if (!enable) {
            deleteList.clear()
        }
    }

    fun toggleDeleteItem(alarm: Alarm) {
        if (isItemInDeleteList(alarm)) {
            deleteList.remove(alarm)
        } else {
            deleteList.add(alarm)
        }
    }

    fun selectAllDeleteItems() {
        viewModelScope.launch {
            list.collectLatest {
                if (deleteList.size == it.size) {
                    deleteList.removeAll(it)
                } else {
                    val tmp = it.filter { !isItemInDeleteList(it) }
                    deleteList.addAll(tmp)
                }
            }
        }
    }

    fun isItemInDeleteList(alarm: Alarm): Boolean {
        return deleteList.contains(alarm)
    }

    fun isDeleteListNotEmpty(): Boolean {
        return deleteList.isNotEmpty()
    }
}