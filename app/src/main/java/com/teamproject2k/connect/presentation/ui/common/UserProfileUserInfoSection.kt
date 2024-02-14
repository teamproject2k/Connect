package com.teamproject2k.connect.presentation.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamproject2k.connect.domain.models.UsersBean
import com.teamproject2k.connect.domain.utils.VisibilityScopeEnum
import com.teamproject2k.connect.presentation.utils.FunctionHelper

@Composable
fun UserProfileUserInfoSection(userDetails: UsersBean, loggedInUserFirebaseId: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        TextBold18(text = userDetails.name)
        SpacerHeight6()
        Text(text = userDetails.bio, fontSize = 12.sp)
        SpacerHeight12()
        ImageTextItem(
            imageVector = Icons.Default.Person,
            text = userDetails.connectUserId,
            FontWeight.Medium
        )
        val showDobSection =
            userDetails.firebaseUserId == loggedInUserFirebaseId ||
                    userDetails.dobVisibility == VisibilityScopeEnum.Public.name ||
                    (userDetails.dobVisibility == VisibilityScopeEnum.FriendsOnly.name && userDetails.friendList.contains(
                        loggedInUserFirebaseId
                    ))
        if (showDobSection) {
            SpacerHeight8()
            ImageTextItem(
                Icons.Default.DateRange,
                FunctionHelper.getFormattedDateTime(userDetails.dateOfBirth)
            )
        }
        val showGenderSection =
            userDetails.firebaseUserId == loggedInUserFirebaseId ||
                    userDetails.genderVisibility == VisibilityScopeEnum.Public.name ||
                    (userDetails.genderVisibility == VisibilityScopeEnum.FriendsOnly.name && userDetails.friendList.contains(
                        loggedInUserFirebaseId
                    ))
        if (showGenderSection) {
            SpacerHeight8()
            ImageTextItem(imageVector = Icons.Default.Face, text = userDetails.gender)
        }
    }
}
