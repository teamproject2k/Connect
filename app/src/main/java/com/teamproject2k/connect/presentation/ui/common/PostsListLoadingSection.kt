package com.teamproject2k.connect.presentation.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teamproject2k.connect.presentation.utils.ConstantsHelper

@Composable
fun PostListLoadingSection() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(4) {
            UserDetailsSectionLoading(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp)
            )
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(14.dp)
                    .shimmer()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ConstantsHelper.POST_DISPLAY_MEDIA_HEIGHT)
                    .shimmer()
            )
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(24.dp)
                    .shimmer()
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(14.dp)
                    .shimmer()
            )
            SpacerHeight16()
            DividerLightGrayAlpha40()
        }
    }
}

