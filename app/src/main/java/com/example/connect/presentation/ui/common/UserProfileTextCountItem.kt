package com.example.connect.presentation.ui.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

@Composable
fun UserProfileTextCountItem(text: String, count: Int, isCountSurroundedByBracket: Boolean = true) {
    Text(text = buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(text)
        }
        append(" ")
        withStyle(SpanStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp)) {
            val countText = if (isCountSurroundedByBracket) "($count)" else count.toString()
            append(countText)
        }
    }, textAlign = TextAlign.Center)
}