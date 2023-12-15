package com.example.connect.presentation.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connect.R
import com.example.connect.domain.models.PostBean
import kotlin.math.ceil

@Composable
fun UserProfilePostSection(postDetailsList: List<PostBean>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        TextCountSeeAll(
            text = stringResource(id = R.string.posts),
            count = postDetailsList.size,
            showSeeAll = false
        )
        SpacerHeight12()
        if (postDetailsList.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        getHeightToMaintainAspectRatio(
                            horizontalPadding = 16.dp,
                            verticalPadding = 0.dp,
                            itemsRequiredPerRow = 4,
                            itemsHorizontalPadding = 8.dp,
                            noOfRows = 1,
                            itemsVerticalPadding = 0.dp
                        )
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.no_posts_added),
                    fontSize = 14.sp,
                )
                SpacerWidth8()
                Text(text = stringResource(R.string.add_post),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        // TODO: navigate user to add post screen
                    })
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .height(
                        getHeightToMaintainAspectRatio(
                            horizontalPadding = 16.dp,
                            verticalPadding = 0.dp,
                            itemsRequiredPerRow = 3,
                            itemsHorizontalPadding = 8.dp,
                            noOfRows = ceil(postDetailsList.size.toFloat() / 3).toInt(),
                            itemsVerticalPadding = 8.dp
                        )
                    ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(postDetailsList) { details ->
                    UserProfilePostItem(
                        postDetails = details,
                        modifier = Modifier.size(
                            getWidthToMaintainAspectRatio(
                                horizontalPadding = 16.dp,
                                itemsRequiredPerRow = 3,
                                itemsHorizontalPadding = 8.dp
                            )
                        ),
                    ) {
                    }
                }
            }
        }
    }
}
