package com.example.connect.presentation.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.ui.destinations.UserRequestScreenDestination
import com.example.connect.presentation.utils.ConstantsHelper
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
fun UserProfileFriendsListSection(
    navigator: DestinationsNavigator,
    friendsList: List<UsersBean>,
    isLoggedInUser: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        TextCountSeeAll(
            text = stringResource(id = R.string.friends),
            count = friendsList.size,
            showSeeAll = friendsList.size > ConstantsHelper.UserProfileFriendColumns
        ) {
            navigator.navigate(UserRequestScreenDestination())
        }
        SpacerHeight12()
        if (friendsList.isEmpty()) {
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
                    text = stringResource(R.string.no_friends_added),
                    fontSize = 14.sp,
                )
                SpacerWidth8()
                if (isLoggedInUser) {
                    Text(text = stringResource(R.string.add_friends),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            // Todo: navigate to friends screen
                        })
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth()) {
                val postCount = minOf(friendsList.count(), ConstantsHelper.UserProfileFriendColumns)
                repeat(postCount) { index ->
                    UserProfileFriendItem(
                        friendDetails = friendsList[index],
                        modifier = Modifier.weight(1f)
                    ) {

                    }
                    SpacerWidth8()
                }
                repeat(ConstantsHelper.UserProfileFriendColumns - postCount) {
                    SpacerWidth8()
                    UserProfileFriendItem(friendDetails = null, modifier = Modifier.weight(1f)) {
                        // no need to handle
                    }
                }
            }
        }
    }
}
