package com.teamproject2k.connect.presentation.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.teamproject2k.connect.R

@Composable
fun LoaderDialog(loadingText: String = stringResource(R.string.loading)) {
    Dialog(
        onDismissRequest = { },
        DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.onPrimary,
                    RoundedCornerShape(12.dp)
                )
                .padding(24.dp)
        ) {
            CircularProgressIndicator()
            SpacerHeight12()
            Text(text = loadingText)
        }
    }
}

@Preview
@Composable
fun PreviewLoaderDialog() {
    LoaderDialog()
}