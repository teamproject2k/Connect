package com.example.connect.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.connect.R
import com.example.connect.utils.enums.ButtonLoadingState

@Composable
fun LoaderButton(
    loaderButtonState: MutableState<ButtonLoadingState>,
    buttonText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val currentButtonState by remember {
        loaderButtonState
    }

    val isEnabled = currentButtonState == ButtonLoadingState.NotLoading

    Button(
        onClick = { onClick() },
        enabled = isEnabled,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isEnabled) {
                NBallLoader(
                    activatedColor = MaterialTheme.colorScheme.onPrimary,
                    deactivatedColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.size(27.dp)
                )
                SpacerWidth12()
            }
            Text(text = if (isEnabled) buttonText else stringResource(R.string.please_wait))
        }
    }
}