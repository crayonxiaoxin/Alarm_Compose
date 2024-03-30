package com.github.crayonxiaoxin.alarmclock_compose.ui.widgets

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

/**
 * 滚轮式时间选择器
 */
@Composable
fun WheelTimePicker(
    hour: Int = -1,
    minute: Int = -1,
    wheelItemHeight: Dp = 80.dp,
    wheelItemFontSize: TextUnit = 40.sp,
    selectedTextColor: Color = MaterialTheme.colorScheme.primary,
    unselectedTextColor: Color = Color.LightGray,
    onTimeChanged: (String) -> Unit = {},
) {
    // 小时和分钟列表
    val minuteList = mutableListOf<String>()
    val hourList = mutableListOf<String>()
    for (i in 0 until 60) {
        minuteList.add(String.format("%02d", i))
        if (i < 24) {
            hourList.add(String.format("%02d", i))
        }
    }

    // 当前日历
    val c = Calendar.getInstance()

    // 选中的小时
    val selectedHourIndex = remember {
        mutableIntStateOf(if (hour >= 0) hour else c.get(Calendar.HOUR_OF_DAY))
    }
    // 选中的分钟
    val selectedMinuteIndex = remember {
        mutableIntStateOf(if (minute >= 0) minute else c.get(Calendar.MINUTE))
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        WheelPicker(modifier = Modifier
            .fillMaxWidth()
            .weight(1.0f),
            list = hourList,
            isLoop = true,
            selectedIndex = selectedHourIndex.intValue,
            selectedBoxColor = Color.Transparent,
            itemHeight = wheelItemHeight,
            onItemSelected = { index, data ->
                selectedHourIndex.intValue = index
                onTimeChanged("${hourList[selectedHourIndex.intValue]}:${minuteList[selectedMinuteIndex.intValue]}")
            }) { index, data ->
            val color = if (index == selectedHourIndex.intValue) {
                selectedTextColor
            } else {
                unselectedTextColor
            }
            Text(text = data, fontSize = wheelItemFontSize, color = color)
        }
        Text(
            text = ":",
            fontSize = wheelItemFontSize,
            color = selectedTextColor,
            modifier = Modifier.padding(bottom = (wheelItemFontSize.value / 5).dp)
        )
        WheelPicker(modifier = Modifier
            .fillMaxWidth()
            .weight(1.0f),
            list = minuteList,
            isLoop = true,
            selectedIndex = selectedMinuteIndex.intValue,
            selectedBoxColor = Color.Transparent,
            itemHeight = wheelItemHeight,
            onItemSelected = { index, data ->
                selectedMinuteIndex.intValue = index
                onTimeChanged("${hourList[selectedHourIndex.intValue]}:${minuteList[selectedMinuteIndex.intValue]}")
            }) { index, data ->
            val color = if (index == selectedMinuteIndex.intValue) {
                selectedTextColor
            } else {
                unselectedTextColor
            }
            Text(text = data, fontSize = wheelItemFontSize, color = color)
        }
    }
}