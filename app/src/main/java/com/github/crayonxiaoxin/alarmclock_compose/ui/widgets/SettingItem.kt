package com.github.crayonxiaoxin.alarmclock_compose.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SettingItem(
    label: String = "Label",
    value: String = "",
    labelColor: Color = Color.Black,
    valueColor: Color = Color.Gray,
    showDivider: Boolean = false,
    paddingValues: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
    onClick: () -> Unit = {},
    icon: (@Composable RowScope.() -> Unit)? = null,
    valueContent: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(paddingValues = paddingValues),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = labelColor,
        )
        if (valueContent != null) {
            valueContent()
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1.0f),
                textAlign = TextAlign.End,
                color = valueColor,
            )
        }
        if (icon != null) {
            icon()
        }
    }
    if (showDivider) {
        MyDivider()
    }
}