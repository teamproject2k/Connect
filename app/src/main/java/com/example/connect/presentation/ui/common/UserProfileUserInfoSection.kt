package com.example.connect.presentation.ui.common

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
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.utils.FunctionHelper

@Composable
fun UserProfileUserInfoSection(userDetails: UsersBean) {
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
        SpacerHeight8()
        ImageTextItem(
            Icons.Default.DateRange,
            FunctionHelper.getFormattedDate(userDetails.dateOfBirth)
        )
        SpacerHeight8()
        ImageTextItem(imageVector = Icons.Default.Face, text = userDetails.gender)
    }
}
