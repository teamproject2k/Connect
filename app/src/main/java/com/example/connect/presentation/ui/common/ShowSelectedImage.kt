package com.example.connect.presentation.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.example.connect.R
import com.example.connect.presentation.ui.models.MediaData

@Composable
fun ShowSelectedImage(selectedMediaData: MediaData, onError: () -> Unit) {
    AsyncImage(
        model = selectedMediaData.uri,
        contentDescription = stringResource(R.string.story_image),
        modifier = Modifier.fillMaxWidth(),
        contentScale = ContentScale.Crop,
        onError = {
            onError()
        }
    )
}