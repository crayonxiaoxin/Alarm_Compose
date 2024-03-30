package com.github.crayonxiaoxin.alarmclock_compose.utils

import android.content.Context
import android.widget.Toast
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val toastScope = CoroutineScope(Dispatchers.Main)

fun Context?.toast(
    message: Any,
    duration: Int = Toast.LENGTH_SHORT
) {
    if (this == null) return
    toastScope.launch {
        Toast.makeText(this@toast, message.toString(), duration).show()
    }
}

fun NavHostController.toast(
    message: Any,
    duration: Int = Toast.LENGTH_SHORT
) {
    this.context.toast(message, duration)
}
