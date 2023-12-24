package com.example.connect.presentation.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Dot(size: Dp = 6.dp, fillColor: Color = ColorsHelper.lightGray()) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(fillColor)
    )
}