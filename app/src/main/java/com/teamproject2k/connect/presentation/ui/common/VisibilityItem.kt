package com.teamproject2k.connect.presentation.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VisibilityItem(
    modifier: Modifier = Modifier,
    @DrawableRes drawableId: Int,
    scopeName: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                ColorsHelper.gray(),
                RoundedCornerShape(12.dp),
            )
            .clickable {
                onClick()
            }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Image(
            painterResource(id = drawableId),
            contentDescription = scopeName,
            modifier = Modifier.size(14.dp)
        )
        SpacerWidth6()
        Text(text = scopeName, fontSize = 12.sp)
    }
}
