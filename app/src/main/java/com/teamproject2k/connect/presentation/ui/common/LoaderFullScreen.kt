package com.teamproject2k.connect.presentation.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teamproject2k.connect.R

@Composable
fun LoaderFullScreen(loadingText: String = stringResource(id = R.string.please_wait)) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(Modifier.size(36.dp))
        SpacerWidth12()
        Text(text = loadingText)
    }
}

@Preview
@Composable
fun PreviewFullScreenLoaderDialog() {
    LoaderFullScreen(loadingText = "Loading...")
}