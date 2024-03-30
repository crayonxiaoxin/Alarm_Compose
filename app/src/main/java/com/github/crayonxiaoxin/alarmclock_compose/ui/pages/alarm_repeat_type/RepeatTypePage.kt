package com.github.crayonxiaoxin.alarmclock_compose.ui.pages.alarm_repeat_type

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.github.crayonxiaoxin.alarmclock_compose.R
import com.github.crayonxiaoxin.alarmclock_compose.model.RepeatType
import com.github.crayonxiaoxin.alarmclock_compose.ui.ParamsViewModel
import com.github.crayonxiaoxin.alarmclock_compose.ui.widgets.SelectedIcon
import com.github.crayonxiaoxin.alarmclock_compose.utils.clickableDebounced
import com.github.crayonxiaoxin.alarmclock_compose.utils.dialog2Bottom
import com.github.crayonxiaoxin.alarmclock_compose.utils.onClickDebounced


@Composable
fun RepeatTypePage(controller: NavHostController, paramsViewModel: ParamsViewModel) {

    // 处理返回事件
    val onBack: () -> Unit = {
        controller.popBackStack()
    }
    BackHandler(onBack = onBack)

    // 如果上一个页面传有值过来
    val repeatType = paramsViewModel.alarmAddViewModel.repeatType.value

    // 选中的类型
    val selectedState = remember {
        mutableStateOf(repeatType.key)
    }
    // 自定义类型是否展开
    val isCustomExpandedState = remember {
        mutableStateOf(false)
    }
    // 自定义选中的 keys
    val weekdaySelectedListState = remember {
        mutableStateListOf<Int>()
    }
    if (repeatType.isCustom()) {
        weekdaySelectedListState.clear()
        weekdaySelectedListState.addAll(repeatType.customWeekdayList)
    }
    // 记录历史选择的值：取消时，还原之前的值
    val lastWeekdaySelectedList = remember {
        mutableStateOf<List<Int>>(listOf())
    }

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // 顶部导航
            TopBar(
                onBack = onBack,
                selectedState = selectedState,
                weekdaySelectedListState = weekdaySelectedListState,
                paramsViewModel = paramsViewModel
            )
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .verticalScroll(rememberScrollState())
        ) {
            RepeatType.list.forEach {
                RepeatTypeItem(
                    item = it,
                    selectedState = selectedState,
                    isCustomExpandedState = isCustomExpandedState,
                    lastWeekdaySelectedList = lastWeekdaySelectedList,
                    weekdaySelectedListState = weekdaySelectedListState,
                )
            }
        }
    }
}

@Composable
private fun RepeatTypeItem(
    item: RepeatType,
    selectedState: MutableState<String>,
    isCustomExpandedState: MutableState<Boolean>,
    lastWeekdaySelectedList: MutableState<List<Int>>,
    weekdaySelectedListState: SnapshotStateList<Int>,
) {
    // 颜色
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = Color.Black
    val isSelected = item.key == selectedState.value
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(shape = RoundedCornerShape(16.dp))
            .clickableDebounced {
                if (item.isCustom()) {
                    isCustomExpandedState.value = !isCustomExpandedState.value
                    lastWeekdaySelectedList.value = weekdaySelectedListState.toList()
                } else {
                    isCustomExpandedState.value = false
                }
                selectedState.value = item.key
            }
            .background((if (isSelected) selectedColor else Color.LightGray).copy(alpha = 0.2f))
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = "Selected",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(24.dp),
                tint = selectedColor
            )
        } else {
            Spacer(
                Modifier
                    .padding(end = 8.dp)
                    .size(24.dp)
            )
        }
        Text(
            modifier = Modifier.weight(1.0f),
            text = item.name,
            color = if (isSelected) selectedColor else unselectedColor
        )
        if (item.isCustom()) {
            Icon(
                imageVector = if (isCustomExpandedState.value) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Selected",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(24.dp),
                tint = selectedColor
            )
        }
    }
    if (isCustomExpandedState.value && item.isCustom()) {
        AlertDialog(modifier = Modifier.dialog2Bottom(30.dp),
            shape = RoundedCornerShape(30.dp),
            onDismissRequest = {
                isCustomExpandedState.value = false
            },
            confirmButton = {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(modifier = Modifier.weight(1.0f), colors = ButtonColors(
                        containerColor = Color.LightGray.copy(alpha = 0.2f),
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        disabledContainerColor = Color.LightGray,
                        disabledContentColor = Color.White
                    ), onClick = onClickDebounced {
                        isCustomExpandedState.value = false
                        weekdaySelectedListState.clear()
                        weekdaySelectedListState.addAll(lastWeekdaySelectedList.value)
                        if (weekdaySelectedListState.isEmpty()) {
                            selectedState.value = RepeatType.Once
                        }
                    }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(modifier = Modifier.weight(1.0f), onClick = onClickDebounced {
                        isCustomExpandedState.value = false
                        if (weekdaySelectedListState.isEmpty()) {
                            selectedState.value = RepeatType.Once
                        }
                    }) {
                        Text(text = stringResource(R.string.confirm))
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.clip(shape = RoundedCornerShape(16.dp))
                ) {
                    RepeatType.weekdayList.forEach {
                        Row(
                            modifier = Modifier
                                .clip(shape = RoundedCornerShape(16.dp))
                                .clickableDebounced {
                                    Log.e(
                                        "TAG",
                                        "RepeatTypePage Click: ${weekdaySelectedListState.toList()}",
                                    )
                                    if (weekdaySelectedListState.contains(it.key)) {
                                        weekdaySelectedListState.remove(it.key)
                                    } else {
                                        weekdaySelectedListState.add(it.key)
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                modifier = Modifier.weight(1.0f),
                                text = it.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            SelectedIcon(
                                isSelected = weekdaySelectedListState.contains(
                                    it.key
                                )
                            )
                        }
                    }
                }
            })
    }
}

@Composable
private fun TopBar(
    onBack: () -> Unit,
    selectedState: MutableState<String>,
    weekdaySelectedListState: SnapshotStateList<Int>,
    paramsViewModel: ParamsViewModel
) {
    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(
            R.string.back
        ), modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickableDebounced {
                // 返回
                onBack()
            }
            .padding(8.dp))
        Text(
            text = stringResource(R.string.repeat),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1.0f)
        )
        Icon(imageVector = Icons.Default.Done,
            contentDescription = stringResource(R.string.done),
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickableDebounced {
                    RepeatType.list
                        .find { it.key == selectedState.value }
                        ?.let {
                            if (it.isCustom()) {
                                it.customWeekdayList = weekdaySelectedListState.toList()
                            }
                            // 提交
                            Log.e("TAG", "AlarmAddPage: $it")
                            // 给上一个页面返回参数
                            paramsViewModel.alarmAddViewModel.repeatType.value = it
                            onBack()
                        }

                }
                .padding(8.dp))
    }
}