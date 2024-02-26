package com.teamproject2k.connect.presentation.ui.common

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
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.teamproject2k.connect.R
import com.teamproject2k.connect.domain.models.PostBean
import com.teamproject2k.connect.domain.models.UserBean
import com.teamproject2k.connect.presentation.ui.destinations.AddPostScreenDestination
import com.teamproject2k.connect.presentation.ui.destinations.PostDetailsScreenDestination
import kotlin.math.ceil

@Composable
fun UserProfilePostSection(
    navigator: DestinationsNavigator,
    postDetailsList: List<PostBean>?,
    isLoggedInUser: Boolean = false,
    userDetails: UserBean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        TextCountSeeAll(
            text = stringResource(id = R.string.posts),
            count = postDetailsList?.size ?: 0,
            showSeeAll = false
        )
        SpacerHeight12()
        if (postDetailsList.isNullOrEmpty()) {
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
                if (isLoggedInUser) {
                    Text(text = stringResource(R.string.add_post),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            navigator.navigate(AddPostScreenDestination())
                        })
                }
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
                items(postDetailsList, key = { it.postFirebaseId }) { details ->
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
                        navigator.navigate(
                            PostDetailsScreenDestination(
                                details,
                                userDetails,
                            )
                        )
                    }
                }
            }
        }
    }
}
