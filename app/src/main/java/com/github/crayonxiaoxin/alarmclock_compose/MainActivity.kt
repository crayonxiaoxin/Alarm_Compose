package com.github.crayonxiaoxin.alarmclock_compose

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.crayonxiaoxin.alarmclock_compose.service.AlarmService
import com.github.crayonxiaoxin.alarmclock_compose.ui.theme.AlarmClock_ComposeTheme
import com.github.crayonxiaoxin.alarmclock_compose.ui.widgets.CustomAlertDialog

class MainActivity : ComponentActivity() {

    // 需要的权限
    private val permissions = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.SCHEDULE_EXACT_ALARM,
        Manifest.permission.READ_EXTERNAL_STORAGE,
    )

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val requestPermissions =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) {
                    val intent = Intent(this, AlarmService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }
                }


            // 申请权限
            LaunchedEffect(Unit) {
                requestPermissions.launch(permissions)
            }

            // 申请悬浮窗权限
            RequestOverlayDialog()

            // 主体
            AlarmClock_ComposeTheme {
                // 树作用域：向下传递隐式数据流
                CompositionLocalProvider(
                    // 更改 Density，使 .sp 不跟随系统缩放
                    LocalDensity provides Density(LocalDensity.current.density, 1f)
                ) {
                    Navigation()
                }
            }
        }
    }

    /**
     * 悬浮窗权限 dialog
     */
    @Composable
    private fun RequestOverlayDialog() {
        val requestOverlayPermission =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {

            }

        // 悬浮窗申请弹窗的状态
        val showOverlayDialogState = remember {
            mutableStateOf(!checkOverlayPermission())
        }

        // 悬浮窗权限
        if (showOverlayDialogState.value) {
            CustomAlertDialog(
                modifier = Modifier.padding(bottom = 30.dp),
                gravity = Gravity.BOTTOM,
                shape = RoundedCornerShape(30.dp),
                onDismissRequest = {
                    showOverlayDialogState.value = false
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
                            onClick = {
                                showOverlayDialogState.value = false
                            }) {
                            Text(text = stringResource(id = R.string.cancel))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            modifier = Modifier.weight(1.0f),
                            onClick = {
                                showOverlayDialogState.value = false
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    requestOverlayPermission.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestOverlayPermission.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                                        data = Uri.parse("package:${packageName}")
                                    })
                                }
                            }) {
                            Text(text = stringResource(id = R.string.confirm))
                        }
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.permission_overlay),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.permission_overlay_content),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            )
        }
    }

    // 检查悬浮窗权限
    private fun checkOverlayPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appOpsManager =
                getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOpsManager.unsafeCheckOpNoThrow(
                    "android:system_alert_window",
                    android.os.Process.myUid(),
                    packageName
                )
            } else {
                appOpsManager.checkOpNoThrow(
                    "android:system_alert_window",
                    android.os.Process.myUid(),
                    packageName
                )
            }
            Log.e("TAG", "checkOverlayPermission: $mode")
            return mode == AppOpsManager.MODE_ALLOWED
        } else {
            return Settings.canDrawOverlays(this)
        }
    }


}