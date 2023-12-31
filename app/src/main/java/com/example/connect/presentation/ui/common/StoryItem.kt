package com.example.connect.presentation.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.utils.ConstantsHelper
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
fun StoryItem(
    storyPoster: UsersBean,
    allStories: MutableMap<String, ArrayList<StoryBean>>,
    allStoryPosters: ArrayList<UsersBean>,
    loggedInUserFirebaseId: String,
    navigator: DestinationsNavigator
) {
    val isLoggedInUser = loggedInUserFirebaseId == storyPoster.firebaseUserId

    val currentUserStories = allStories[storyPoster.firebaseUserId]
    if (currentUserStories != null) {
        Column(modifier = Modifier.clickable {
            //  navigator.navigate(ShowStoryScreenDestination(storyPoster, allStories, allStoryPosters))
        }, horizontalAlignment = Alignment.CenterHorizontally) {
            BreakCircularBorder(
                storyPoster.profilePhoto,
                parts = currentUserStories.size,
                getColorListFromStories(currentUserStories, loggedInUserFirebaseId)
            )
            SpacerHeight6()
            Text(
                text = if (isLoggedInUser) stringResource(R.string.your_story) else storyPoster.name,
                fontSize = 12.sp
            )
        }
    }
}

fun getColorListFromStories(
    currentUserStories: ArrayList<StoryBean>,
    loggedInUserFirebaseId: String
): List<List<Color>> {
    val colorList = mutableListOf<List<Color>>()
    val numberOfStoriesSeen =
        currentUserStories.count { it.seenList.contains(loggedInUserFirebaseId) }

    repeat(numberOfStoriesSeen) {
        colorList.add(listOf(Color.Gray, Color.LightGray))
    }

    repeat(currentUserStories.size - numberOfStoriesSeen) {
        colorList.add(listOf(Color.Red, Color.Magenta))
    }

    return colorList.toList()
}

@Composable
fun BreakCircularBorder(
    imageUrl: String?,
    parts: Int,
    colorList: List<List<Color>>,
    gapAngle: Float = 10f,
    strokeWidth: Dp = 4.dp,
) {
    if (colorList.size != parts || parts == 0) throw IllegalArgumentException("either parts is 0 or color list size not equal to parts")

    val partAngle = if (parts > 1) (360f - parts * gapAngle) / parts else 360f
    Box(
        modifier = Modifier
            .size(ConstantsHelper.StoryItemHeight)
            .clip(CircleShape)
    ) {
        repeat(parts) { index ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .drawWithContent {
                        drawCircularBorder(
                            brush = Brush.linearGradient(colorList[index]),
                            strokeWidth = strokeWidth.toPx(),
                            startAngle = index * (partAngle + gapAngle) + 90,
                            sweepAngle = partAngle
                        )
                    }
            )
        }
        AsyncImage(
            modifier = Modifier
                .padding(strokeWidth + 1.dp)
                .fillMaxSize()
                .clip(CircleShape),
            model = imageUrl,
            contentDescription = stringResource(id = R.string.profile_image),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_default_user)
        )
    }
}

fun DrawScope.drawCircularBorder(
    brush: Brush,
    strokeWidth: Float,
    startAngle: Float,
    sweepAngle: Float
) {
    drawArc(
        brush = brush,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        style = Stroke(width = strokeWidth)
    )
}