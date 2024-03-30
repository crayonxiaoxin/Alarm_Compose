package com.github.crayonxiaoxin.alarmclock_compose.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SelectedIcon(
    isSelected: Boolean = false,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = Color.LightGray.copy(
        alpha = 0.2f
    ),
    tintColor: Color = Color.White,
    size: Dp = 24.dp,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(size))
            .background(
                color = if (isSelected) selectedColor else unselectedColor
            )
            .size(size),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                modifier = Modifier.size(size = size.times(0.8f)),
                imageVector = Icons.Default.Done,
                tint = tintColor,
                contentDescription = "Selected"
            )
        }
    }
}