package com.example.connect.presentation.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.connect.R

@Composable
fun UserProfilePostLoadingSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        UserProfileTextCountItem(
            text = stringResource(id = R.string.posts),
            count = 0,
        )
        SpacerHeight12()
        Row(modifier = Modifier.fillMaxWidth()) {
            repeat(3) {
                UserProfilePostItem(
                    postDetails = null, showShimmer = true, modifier = Modifier.size(
                        getWidthToMaintainAspectRatio(
                            horizontalPadding = 16.dp,
                            itemsRequiredPerRow = 3,
                            itemsHorizontalPadding = 8.dp
                        )
                    )
                )
                if (it != 2) {
                    SpacerWidth8()
                }
            }
        }
    }
}
