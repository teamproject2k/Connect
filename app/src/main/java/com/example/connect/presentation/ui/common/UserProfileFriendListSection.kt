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
import com.example.connect.presentation.ui.destinations.CurrentUserProfileScreenDestination
import com.example.connect.presentation.ui.destinations.OtherUserProfileScreenDestination
import com.example.connect.presentation.ui.destinations.SearchScreenDestination
import com.example.connect.presentation.ui.destinations.UserRequestScreenDestination
import com.example.connect.presentation.utils.ConstantsHelper
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
fun UserProfileFriendsListSection(
    navigator: DestinationsNavigator,
    friendsList: List<UsersBean>?,
    loggedInUserFirebaseId: String,
    isLoggedInUser: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        TextCountSeeAll(
            text = stringResource(id = R.string.friends),
            count = friendsList?.size ?: 0,
            showSeeAll = (friendsList?.size ?: 0) > ConstantsHelper.PROFILE_FRIENDS_COLUMN_COUNT
        ) {
            navigator.navigate(UserRequestScreenDestination())
        }
        SpacerHeight12()
        if (friendsList.isNullOrEmpty()) {
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
                            navigator.navigate(SearchScreenDestination)
                        })
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth()) {
                val friendCount =
                    minOf(friendsList.count(), ConstantsHelper.PROFILE_FRIENDS_COLUMN_COUNT)
                repeat(friendCount) { index ->
                    UserProfileFriendItem(
                        friendDetails = friendsList[index],
                        modifier = Modifier.weight(1f)
                    ) {
                        if (friendsList[index].firebaseUserId == loggedInUserFirebaseId) {
                            navigator.navigate(CurrentUserProfileScreenDestination())
                        } else {
                            navigator.navigate(OtherUserProfileScreenDestination(friendsList[index]))
                        }
                    }
                    SpacerWidth8()
                }
                repeat(ConstantsHelper.PROFILE_FRIENDS_COLUMN_COUNT - friendCount) {
                    SpacerWidth8()
                    UserProfileFriendItem(friendDetails = null, modifier = Modifier.weight(1f)) {
                        // no need to handle
                    }
                }
            }
        }
    }
}
