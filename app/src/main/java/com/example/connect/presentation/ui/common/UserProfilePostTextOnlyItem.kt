package com.example.connect.presentation.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UserProfilePostTextOnlyItem(caption: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = caption,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onPrimary,
            lineHeight = 12.sp,
            overflow = TextOverflow.Ellipsis
        )
    }
}