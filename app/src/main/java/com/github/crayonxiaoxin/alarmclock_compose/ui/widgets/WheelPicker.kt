package com.github.crayonxiaoxin.alarmclock_compose.ui.widgets

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> WheelPicker(
    list: List<T>,
    modifier: Modifier = Modifier,
    selectedIndex: Int = 0,
    displayCount: Int = 3,
    itemHeight: Dp = 40.dp,
    isLoop: Boolean = false,
    selectedBoxShape: Shape = RoundedCornerShape(16.dp),
    selectedBoxColor: Color = Color.Gray.copy(alpha = 0.5f),
    onItemSelected: (index: Int, data: T) -> Unit = { _, _ -> },
    itemContent: @Composable (index: Int, data: T) -> Unit
) {
    val wheelScope = rememberCoroutineScope()
    val offset = (displayCount - 1) / 2 // 占位
    var startIndex = selectedIndex
    var totalCount = list.size
    if (isLoop) {
        totalCount = 50000
        val half = totalCount / 2
        val tmpIndex = half % list.size
        val diff = tmpIndex - startIndex
        startIndex = half - diff
    }
    Log.e("TAG", "WheelPicker: $selectedIndex, $startIndex")
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    // 滚动监听
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect {
                val index = if (isLoop) {
                    it % list.size
                } else {
                    it
                }
                onItemSelected(index, list[index])
            }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight),
            shape = selectedBoxShape,
            color = selectedBoxColor,
        ) {}
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * displayCount),
            state = lazyListState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)
        ) {
            items(offset) {
                WheelItem(itemHeight) {
                    Text(text = "")
                }
            }
            items(totalCount) {
                WheelItem(itemHeight, onClick = {
                    val index = if (isLoop) {
                        it % list.size
                    } else {
                        it
                    }
                    onItemSelected(index, list[index])
                    wheelScope.launch {
                        lazyListState.scrollToItem(index)
                    }
                }) {
                    val index = if (isLoop) {
                        it % list.size
                    } else {
                        it
                    }
                    itemContent(index, list[index])
                }
            }
            items(offset) {
                WheelItem(itemHeight) {
                    Text(text = "")
                }
            }
        }
    }
}

@Composable
private fun WheelItem(
    itemHeight: Dp,
    onClick: () -> Unit = {},
    text: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .height(itemHeight),
        contentAlignment = Alignment.Center
    ) {
        text()
    }
}