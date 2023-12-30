package com.example.connect.presentation.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.utils.ConstantsHelper

@Composable
fun StoryItem(
    user: UsersBean,
    stories: MutableList<StoryBean>
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .size(ConstantsHelper.StoryItemHeight)
                .clip(CircleShape)
                .border(1.dp, Color.Red, CircleShape),
            model = user.profilePhoto,
            contentDescription = user.name,
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_default_user)
        )
        SpacerHeight6()
        Text(text = user.name, fontSize = 12.sp)
    }
}