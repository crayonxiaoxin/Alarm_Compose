package com.github.crayonxiaoxin.alarmclock_compose.ui

import androidx.lifecycle.ViewModel
import com.github.crayonxiaoxin.alarmclock_compose.Routes
import com.github.crayonxiaoxin.alarmclock_compose.ui.pages.alarm_add.AlarmAddViewModel
import com.github.crayonxiaoxin.alarmclock_compose.ui.pages.alarm_list.AlarmListViewModel


/**
 * 用于传参的 viewModel
 */
class ParamsViewModel : ViewModel() {

    /**
     * 用于 [Routes.AlarmList] 的参数传递
     */
    lateinit var alarmListViewModel: AlarmListViewModel

    /**
     * 用于 [Routes.AlarmAdd] 和 [Routes.AlarmEdit] 的参数传递
     */
    lateinit var alarmAddViewModel: AlarmAddViewModel

}