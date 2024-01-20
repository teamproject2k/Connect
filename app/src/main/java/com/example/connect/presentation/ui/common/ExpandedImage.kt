package com.example.connect.presentation.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.presentation.utils.FunctionHelper.showToast

@Composable
fun ExpandedImage(imageUrl: String?, onDismiss: () -> Unit) {
    var isImageLoading by remember {
        mutableStateOf(false)
    }
    val imageSize = LocalConfiguration.current.screenWidthDp * 0.8
    val context = LocalContext.current
    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        val modifier = Modifier
            .clickable {
                onDismiss()
            }
            .size(imageSize.dp)
            .clip(RoundedCornerShape(4.dp))
        AsyncImage(
            model = imageUrl,
            contentDescription = stringResource(R.string.profile_photo),
            contentScale = ContentScale.Crop,
            onLoading = {
                isImageLoading = true
            },
            onError = {
                context.showToast(context.getString(R.string.something_went_wrong))
                isImageLoading = false
            },
            onSuccess = {
                isImageLoading = false
            },
            modifier = if (isImageLoading) {
                modifier.shimmer()
            } else {
                modifier
            }
        )
    }
}