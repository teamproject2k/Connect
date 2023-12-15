package com.example.connect.presentation.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.example.connect.R

@Composable
fun TextCountSeeAll(
    text: String,
    count: Int,
    areBracketsVisible: Boolean = true,
    showSeeAll: Boolean = true,
    onSeeAllClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserProfileTextCountItem(text = text, count = count, areBracketsVisible)
        if (showSeeAll) {
            Text(
                text = stringResource(R.string.see_all),
                textDecoration = TextDecoration.Underline,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    onSeeAllClick()
                }
            )
        }
    }
}