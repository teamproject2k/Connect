package com.example.connect.presentation.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.models.UsersBean

@Composable
fun UserProfileFriendItem(
    modifier: Modifier = Modifier,
    friendDetails: UsersBean?,
    showShimmer: Boolean = false,
    onClick: () -> Unit = {}
) {
    val updatedModifier = if (friendDetails != null) {
        modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable { onClick() }
    } else {
        modifier
            .aspectRatio(1f)
            .clip(CircleShape)
    }
    Column(modifier = if (showShimmer) updatedModifier.shimmer() else updatedModifier) {
        if (friendDetails == null || showShimmer) return
        AsyncImage(
            model = friendDetails.profilePhoto,
            contentDescription = friendDetails.name,
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_default_user)
        )
    }
}
