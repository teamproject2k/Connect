package com.example.connect.presentation.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun TextBoldOnPrimary28(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 28.sp,
        color = MaterialTheme.colorScheme.onPrimary,
        fontWeight = FontWeight.Bold
    )
}


@Composable

fun TextBold18(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun TextBlack18(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.scrim,
        fontWeight = FontWeight.SemiBold
    )
}