package com.github.crayonxiaoxin.alarmclock_compose.utils

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.Role

/**
 * 默认防抖间隔
 */
const val DEFAULT_DEBOUNCE_INTERVAL = 300L

/**
 * 去抖动点击：防止点击过快，导致重复触发 [onClick] 事件
 */
@SuppressLint("ModifierFactoryUnreferencedReceiver")
fun Modifier.clickableDebounced(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    delay: Long = DEFAULT_DEBOUNCE_INTERVAL,
    onClick: () -> Unit
) = composed(inspectorInfo = debugInspectorInfo {
    name = "clickable"
    properties["enabled"] = enabled
    properties["onClickLabel"] = onClickLabel
    properties["role"] = role
    properties["onClick"] = onClick
}) {
    var lastClickTime by remember {
        mutableLongStateOf(0L)
    }
    Modifier.clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        onClick = {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime >= delay) {
                onClick()
                lastClickTime = currentTime
            }
        },
        role = role,
        indication = LocalIndication.current,
        interactionSource = remember { MutableInteractionSource() }
    )
}

/**
 * 去抖动点击：防止点击过快，导致重复触发 [onClick] 事件
 *
 * ```
 * Button(
 *     onClick=onClickDebounced{
 *          // ...
 *     }
 * )
 * ```
 */
@Composable
fun onClickDebounced(
    delay: Long = DEFAULT_DEBOUNCE_INTERVAL,
    onClick: () -> Unit = {}
): () -> Unit {
    var lastClickTime by remember {
        mutableLongStateOf(0L)
    }
    // 因为 [onClick] 需要返回一个 () -> Unit 方法
    // 所以这里返回一个防抖后的无返回值方法
    val newClick = {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= delay) {
            onClick()
            lastClickTime = currentTime
        }
    }
    return newClick
}

@Composable
fun BackHandlerDebounced(
    enabled: Boolean = true,
    delay: Long = DEFAULT_DEBOUNCE_INTERVAL,
    onBack: () -> Unit
) {
    var lastClickTime by remember {
        mutableLongStateOf(0L)
    }
    val currentTime = System.currentTimeMillis()
    val newEnabled = if (enabled && currentTime - lastClickTime >= delay) {
        lastClickTime = currentTime
        true
    } else enabled
    BackHandler(
        enabled = newEnabled,
        onBack = onBack
    )
}