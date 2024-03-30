package com.github.crayonxiaoxin.alarmclock_compose

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.crayonxiaoxin.alarmclock_compose.ui.ParamsViewModel
import com.github.crayonxiaoxin.alarmclock_compose.ui.pages.alarm_add.AlarmAddPage
import com.github.crayonxiaoxin.alarmclock_compose.ui.pages.alarm_list.AlarmListPage
import com.github.crayonxiaoxin.alarmclock_compose.ui.pages.alarm_repeat_type.RepeatTypePage

/**
 * 导航
 */
@Composable
fun Navigation(
    navController: NavHostController = rememberNavController()
) {
    // 转场动画时间
    val navAnimatedDuration = 200
    // 屏幕宽度
    val screenWidth = with(LocalDensity.current) {
        LocalConfiguration.current.screenWidthDp.dp.toPx().toInt()
    }
    // 参数传递：初始化 viewModels
    val paramsViewModel: ParamsViewModel = viewModel()
    paramsViewModel.alarmListViewModel = viewModel()
    paramsViewModel.alarmAddViewModel = viewModel()
    // 导航
    NavHost(
        navController = navController,
        startDestination = Routes.AlarmList.route,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { screenWidth },
                animationSpec = tween(navAnimatedDuration)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -screenWidth },
                animationSpec = tween(navAnimatedDuration)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -screenWidth },
                animationSpec = tween(navAnimatedDuration)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { screenWidth },
                animationSpec = tween(navAnimatedDuration)
            )
        }
    ) {
        composable(route = Routes.AlarmList.route) {
            AlarmListPage(
                controller = navController,
                paramsViewModel = paramsViewModel,
                vm = paramsViewModel.alarmListViewModel
            )
        }
        composable(route = Routes.AlarmAdd.route) {
            AlarmAddPage(
                controller = navController,
                paramsViewModel = paramsViewModel,
                vm = paramsViewModel.alarmAddViewModel
            )
        }
        composable(route = Routes.AlarmEdit.route) {
            AlarmAddPage(
                controller = navController,
                paramsViewModel = paramsViewModel,
                vm = paramsViewModel.alarmAddViewModel
            )
        }
        composable(route = Routes.RepeatType.route) {
            RepeatTypePage(
                controller = navController,
                paramsViewModel = paramsViewModel
            )
        }
    }
}

/**
 * 路由
 */
sealed class Routes(val route: String) {
    data object AlarmList : Routes("alarm-list")
    data object AlarmAdd : Routes("alarm-add")
    data object AlarmEdit : Routes("alarm-edit")
    data object RepeatType : Routes("repeat-type")
}