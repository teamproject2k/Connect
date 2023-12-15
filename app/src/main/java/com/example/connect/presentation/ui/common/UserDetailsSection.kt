package com.example.connect.presentation.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.models.UsersBean


@Composable
fun UserDetailsSection(
    user: UsersBean,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        AsyncImage(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            model = user.profilePhoto,
            contentDescription = user.name,
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_default_user)
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp),
        ) {
            TextBold16(text = user.name)
            Text(text = user.bio, fontSize = 13.sp, overflow = TextOverflow.Ellipsis, maxLines = 1)
        }
    }
}