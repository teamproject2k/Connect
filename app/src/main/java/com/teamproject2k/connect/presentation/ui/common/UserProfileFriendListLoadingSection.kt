package com.teamproject2k.connect.presentation.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teamproject2k.connect.R

@Composable
fun UserProfileFriendsListLoadingSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        TextCountSeeAll(
            text = stringResource(id = R.string.friends),
            count = 0,
            showSeeAll = false
        )
        SpacerHeight12()
        Row(modifier = Modifier.fillMaxWidth()) {
            UserProfileFriendItem(
                friendDetails = null,
                modifier = Modifier.weight(1f),
                showShimmer = true
            )
            SpacerWidth8()
            UserProfileFriendItem(
                friendDetails = null,
                modifier = Modifier.weight(1f),
                showShimmer = true
            )
            SpacerWidth8()
            UserProfileFriendItem(
                friendDetails = null,
                modifier = Modifier.weight(1f),
                showShimmer = true
            )
            SpacerWidth8()
            UserProfileFriendItem(
                friendDetails = null,
                modifier = Modifier.weight(1f),
                showShimmer = true
            )
        }
    }
}