package com.example.connect.presentation.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


@Composable
fun BottomSheetItem(imageVector: ImageVector, text: String, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(imageVector = imageVector, contentDescription = text)
            SpacerWidth12()
            Text(text = text)
        }
        Divider()
    }
}
