package com.example.connect.presentation.ui.common

import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable

@Composable
fun DividerLightGrayAlpha50() {
    Divider(color = ColorsHelper.lightGray().copy(alpha = 0.5f))
}