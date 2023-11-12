package com.example.connect.presentation.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun getHeightToMaintainAspectRatio(
    horizontalPadding: Dp,
    verticalPadding: Dp,
    itemsRequiredPerRow: Int,
    itemsHorizontalPadding: Dp,
    noOfRows: Int,
    itemsVerticalPadding: Dp
): Dp {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val availableScreenWidth =
        screenWidth - horizontalPadding * 2 - itemsHorizontalPadding * (itemsRequiredPerRow - 1)
    val singleItemHeight = availableScreenWidth / itemsRequiredPerRow
    return singleItemHeight * noOfRows + itemsVerticalPadding * (noOfRows - 1) + verticalPadding * 2
}


@Composable
fun getWidthToMaintainAspectRatio(
    horizontalPadding: Dp,
    itemsRequiredPerRow: Int,
    itemsHorizontalPadding: Dp
): Dp {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val availableWidth =
        screenWidth - horizontalPadding * 2 - itemsHorizontalPadding * (itemsRequiredPerRow - 1)
    return availableWidth / itemsRequiredPerRow
}