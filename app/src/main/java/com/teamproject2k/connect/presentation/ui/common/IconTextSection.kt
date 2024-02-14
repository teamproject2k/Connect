package com.teamproject2k.connect.presentation.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IconTextSection(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    contentArrangement: Arrangement.Horizontal = Arrangement.Start,
    imageSize: Dp = 20.dp,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = contentArrangement,
        modifier = modifier
            .clickable {
                onClick()
            }
            .padding(16.dp)
    ) {
        Image(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(imageSize)
        )
        SpacerWidth12()
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}