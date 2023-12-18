package com.example.connect.presentation.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.connect.R

@Composable
fun ExpandedImage(imageUrl: String?, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = stringResource(R.string.profile_photo),
            modifier = Modifier
                .clickable {
                    onDismiss()
                }
                .padding(16.dp)
                .background(ColorsHelper.lightGray())
        )
    }
}