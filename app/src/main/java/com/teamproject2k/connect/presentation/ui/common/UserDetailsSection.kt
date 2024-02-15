package com.teamproject2k.connect.presentation.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.teamproject2k.connect.R
import com.teamproject2k.connect.domain.models.UsersBean

@Composable
fun UserDetailsSection(
    user: UsersBean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        AsyncImage(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(1.dp, ColorsHelper.gray(), CircleShape),
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
            Text(
                text = user.connectUserId,
                fontSize = 13.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}

@Composable
fun UserDetailsSectionLoading(
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        AsyncImage(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .shimmer(),
            model = null,
            contentDescription = null,
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .height(15.dp)
                    .fillMaxWidth()
                    .shimmer()
            )
            SpacerHeight2()
            Box(
                modifier = Modifier
                    .height(12.dp)
                    .fillMaxWidth()
                    .shimmer()
            )
        }
    }
}