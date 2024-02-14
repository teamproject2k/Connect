package com.teamproject2k.connect.presentation.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ImageTextItem(imageVector: ImageVector, text: String, fontWeight: FontWeight? = null) {
    Row(horizontalArrangement = Arrangement.Center) {
        Image(
            imageVector = imageVector,
            contentDescription = text,
            modifier = Modifier.size(16.dp)
        )
        SpacerWidth6()
        Text(text = text, fontSize = 12.sp, fontWeight = fontWeight)
    }
}