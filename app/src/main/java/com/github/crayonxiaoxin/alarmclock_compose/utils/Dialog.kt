package com.github.crayonxiaoxin.alarmclock_compose.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.dialog2Bottom(bottom: Dp = 30.dp) =
    this.then(Modifier.layout { measurable, constraints ->
        // 测量 dialog 真实大小
        val placeable = measurable.measure(constraints)
        // 距离底部的偏移
        val offsetPx = bottom.toPx().toInt()
        // 摆放到底部
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(
                0, constraints.maxHeight - placeable.height - offsetPx, 10f
            )
        }
    })