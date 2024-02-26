package com.teamproject2k.connect.presentation.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teamproject2k.connect.domain.models.UserBean

@Composable
fun UsersListItem(userBean: UserBean, onClick: () -> Unit) {
    Column {
        UserDetailsSection(
            user = userBean,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
                .padding(16.dp)
        )
        Divider()
    }
}

