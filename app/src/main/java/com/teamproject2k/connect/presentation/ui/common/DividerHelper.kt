package com.teamproject2k.connect.presentation.ui.common

import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable

@Composable
fun DividerLightGrayAlpha40() {
    Divider(color = ColorsHelper.lightGray().copy(alpha = 0.4f))
}

@Composable
fun DividerLightGrayAlpha50() {
    Divider(color = ColorsHelper.lightGray().copy(alpha = 0.5f))
}