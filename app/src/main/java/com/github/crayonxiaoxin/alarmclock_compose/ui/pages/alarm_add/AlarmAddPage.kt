package com.github.crayonxiaoxin.alarmclock_compose.ui.pages.alarm_add

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.crayonxiaoxin.alarmclock_compose.R
import com.github.crayonxiaoxin.alarmclock_compose.Routes
import com.github.crayonxiaoxin.alarmclock_compose.ui.ParamsViewModel
import com.github.crayonxiaoxin.alarmclock_compose.ui.widgets.SettingItem
import com.github.crayonxiaoxin.alarmclock_compose.ui.widgets.WheelTimePicker
import com.github.crayonxiaoxin.alarmclock_compose.utils.clickableDebounced
import com.github.crayonxiaoxin.alarmclock_compose.utils.dialog2Bottom
import com.github.crayonxiaoxin.alarmclock_compose.utils.getFileNameFromUri
import com.github.crayonxiaoxin.alarmclock_compose.utils.onClickDebounced


@Composable
fun AlarmAddPage(
    controller: NavHostController,
    paramsViewModel: ParamsViewModel,
    vm: AlarmAddViewModel = viewModel()
) {

    val onBack: () -> Unit = {
        controller.popBackStack()
    }

    // 处理返回事件
    BackHandler(onBack = onBack)

    // 是否展示备注 dialog
    val showRemarkDialogState = remember {
        mutableStateOf(false)
    }
    // 备注输入框焦点请求
    val remarkFocusRequest = remember {
        FocusRequester()
    }
    // 选中的文件名
    val pickFilename = if (vm.uri.value != null) {
        controller.context.getFileNameFromUri(vm.uri.value!!)
    } else ""

    // 文件选择器
    val pickFile =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) {
            it?.let {
                vm.uri.value = it
            }
        }

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // 顶部导航
            TopBar(
                vm = vm,
                onBack = onBack,
            )
        },
    ) {
        Column(Modifier.padding(it)) {
            // 选择时间
            WheelTimePicker(
                hour = if (vm.alarm == null) -1 else vm.alarm!!.hour,
                minute = if (vm.alarm == null) -1 else vm.alarm!!.minute,
            ) {
                vm.selectedTime.value = it
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 选择音乐文件
            SettingItem(
                label = stringResource(R.string.music),
                value = pickFilename,
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.music),
                        tint = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                },
                onClick = onClickDebounced {
                    pickFile.launch(arrayOf("audio/*"))
                }
            )


            // 选择重复类型
            SettingItem(
                label = stringResource(R.string.repeat),
                value = vm.repeatType.value.nameFmt(),
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.repeat),
                        tint = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                },
                onClick = onClickDebounced {
                    controller.navigate(Routes.RepeatType.route)
                }
            )

            SettingItem(
                label = stringResource(R.string.remark),
                value = vm.remark.value,
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.remark),
                        tint = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                },
                onClick = onClickDebounced {
                    showRemarkDialogState.value = true
                }
            )

            // 备注
            if (showRemarkDialogState.value) {
                AlertDialog(
                    modifier = Modifier.dialog2Bottom(30.dp),
                    shape = RoundedCornerShape(30.dp),
                    onDismissRequest = {
                        showRemarkDialogState.value = false
                        remarkFocusRequest.freeFocus()
                    },
                    confirmButton = {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                modifier = Modifier
                                    .weight(1.0f),
                                colors = ButtonColors(
                                    containerColor = Color.LightGray.copy(alpha = 0.2f),
                                    contentColor = MaterialTheme.colorScheme.onBackground,
                                    disabledContainerColor = Color.LightGray,
                                    disabledContentColor = Color.White
                                ),
                                onClick = onClickDebounced {
                                    showRemarkDialogState.value = false
                                    remarkFocusRequest.freeFocus()
                                }) {
                                Text(text = stringResource(id = R.string.cancel))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Button(
                                modifier = Modifier.weight(1.0f),
                                onClick = onClickDebounced {
                                    showRemarkDialogState.value = false
                                    remarkFocusRequest.freeFocus()
                                }) {
                                Text(text = stringResource(id = R.string.confirm))
                            }
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.remark),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .clip(shape = RoundedCornerShape(16.dp))
                        ) {
                            OutlinedTextField(
                                value = vm.remark.value,
                                onValueChange = {
                                    vm.remark.value = it
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.focusRequester(remarkFocusRequest)
                            )
                        }
                    }
                )

                // 请求焦点
                LaunchedEffect(Unit) {
                    remarkFocusRequest.requestFocus()
                }
            }

        }
    }
}

@Composable
private fun TopBar(
    vm: AlarmAddViewModel,
    onBack: () -> Unit,
) {
    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(
                R.string.back
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickableDebounced {
                    // 返回
                    onBack()
                }
                .padding(8.dp))
        Text(
            text = if (vm.alarm == null) stringResource(R.string.add_alarm) else stringResource(
                R.string.update_alarm
            ),
            style = MaterialTheme.typography.titleSmall,
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
                    // 提交
                    vm.submit(onBack)
                }
                .padding(8.dp))
    }
}

