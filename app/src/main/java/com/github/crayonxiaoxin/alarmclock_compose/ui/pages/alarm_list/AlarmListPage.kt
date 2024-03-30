package com.github.crayonxiaoxin.alarmclock_compose.ui.pages.alarm_list

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.crayonxiaoxin.alarmclock_compose.R
import com.github.crayonxiaoxin.alarmclock_compose.Routes
import com.github.crayonxiaoxin.alarmclock_compose.model.Alarm
import com.github.crayonxiaoxin.alarmclock_compose.ui.ParamsViewModel
import com.github.crayonxiaoxin.alarmclock_compose.ui.widgets.SelectedIcon
import com.github.crayonxiaoxin.alarmclock_compose.utils.clickableDebounced
import com.github.crayonxiaoxin.alarmclock_compose.utils.onClickDebounced

@Composable
fun AlarmListPage(
    controller: NavHostController,
    paramsViewModel: ParamsViewModel,
    vm: AlarmListViewModel = viewModel()
) {

    // 首页不要使用 [popBackStack()]，当系统返回键连按时，会导致出现空白页
    BackHandler(enabled = vm.isDeleteMode.value) {
        vm.toggleDeleteMode(false)
    }

    val list = vm.list.collectAsState(initial = listOf())

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // 顶部标题
            TopBar(vm = vm)
        },
        floatingActionButton = {
            if (!vm.isDeleteMode.value) {
                // 添加闹钟
                FloatingButton(controller, paramsViewModel)
            }
        },
        bottomBar = {
            if (vm.isDeleteMode.value) {
                BottomBar(vm)
            }
        }
    ) {
        if (list.value.isEmpty()) {
            NoData()
        }

        // 列表
        val lazyColumnState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier
                .padding(it),
            state = lazyColumnState
        ) {
            items(
                items = list.value,
                key = { it.id }
            ) { alarm ->
                AlarmItem(alarm, controller, paramsViewModel, vm)
            }

//            item(key = "footer") {
//                Spacer(modifier = Modifier.height(50.dp))
//            }
        }

    }
}

@Composable
private fun BottomBar(vm: AlarmListViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.LightGray.copy(alpha = 0.1f))
            .clickableDebounced(enabled = vm.isDeleteListNotEmpty()) {
                vm.deleteSelectedAlarms()
            }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val color = if (vm.isDeleteListNotEmpty()) Color.Black else Color.LightGray
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = stringResource(R.string.delete),
            tint = color
        )
        Text(
            text = stringResource(R.string.delete),
            modifier = Modifier.padding(top = 5.dp),
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
private fun NoData() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = stringResource(R.string.no_data))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AlarmItem(
    alarm: Alarm,
    controller: NavHostController,
    paramsViewModel: ParamsViewModel,
    vm: AlarmListViewModel
) {
    val colorAlpha = if (alarm.isEnable()) 1f else 0.5f
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                spotColor = Color.LightGray,
                ambientColor = Color.LightGray,
                shape = RoundedCornerShape(16.dp),
                clip = true
            )
            .combinedClickable(
                onClick = onClickDebounced {
                    if (vm.isDeleteMode.value) {
                        // 选中
                        vm.toggleDeleteItem(alarm)
                    } else {
                        paramsViewModel.alarmAddViewModel.initEdit(alarm)
                        controller.navigate(Routes.AlarmEdit.route)
                    }
                },
                onLongClick = {
                    if (vm.isDeleteMode.value) {
                        // 选中
                        vm.toggleDeleteItem(alarm)
                    } else {
                        vm.toggleDeleteMode(true)
                        vm.toggleDeleteItem(alarm)
                    }
                }
            )
            .background(color = Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1.0f)
                .padding(end = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = alarm.time(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black.copy(alpha = colorAlpha)
                )
                if (alarm.remark.isNotEmpty()) {
                    Text(
                        text = alarm.remark,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray.copy(alpha = colorAlpha),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = alarm.repeatTypeName(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray.copy(alpha = colorAlpha)
            )
        }
        if (vm.isDeleteMode.value) {
            SelectedIcon(isSelected = vm.isItemInDeleteList(alarm))
        } else {
            Switch(
                checked = alarm.isEnable(),
                onCheckedChange = {
                    // 更新闹钟状态
                    Log.e("TAG", "AlarmListPage: $it")
                    vm.updateAlarm(alarm, it)
                }
            )
        }
    }
}

@Composable
private fun FloatingButton(
    controller: NavHostController,
    paramsViewModel: ParamsViewModel,
) {
    FloatingActionButton(
        shape = RoundedCornerShape(50.dp),
        onClick = onClickDebounced {
            // 添加闹钟
            paramsViewModel.alarmAddViewModel.initAdd()
            controller.navigate(Routes.AlarmAdd.route) {
                this.popUpTo(Routes.AlarmList.route)
            }
        }
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.add_alarm)
        )
    }
}

@Composable
private fun TopBar(vm: AlarmListViewModel) {
    Row(
        modifier = Modifier
            .height(80.dp)
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = if (vm.isDeleteMode.value) Arrangement.SpaceBetween else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (vm.isDeleteMode.value) {
            Icon(imageVector = Icons.Default.Close,
                contentDescription = stringResource(
                    R.string.back
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickableDebounced {
                        vm.toggleDeleteMode(false)
                    }
                    .padding(8.dp))
            Text(
                text = stringResource(R.string.pls_select_delete_items),
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1.0f)
            )
            Icon(imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = stringResource(R.string.done),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickableDebounced {
                        vm.selectAllDeleteItems()
                    }
                    .padding(8.dp))
        } else {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }

}