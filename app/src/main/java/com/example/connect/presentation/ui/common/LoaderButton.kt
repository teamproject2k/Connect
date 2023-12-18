package com.example.connect.presentation.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.connect.R
import com.example.connect.presentation.ui.enums.ButtonStateEnum

@Composable
fun LoaderButton(
    loaderButtonState: MutableState<ButtonStateEnum>,
    buttonText: String,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    loadingText: String = stringResource(id = R.string.please_wait),
    successText: String = buttonText,
    errorText: String = stringResource(R.string.try_again),
    onClick: () -> Unit
) {
    val currentButtonState by remember {
        loaderButtonState
    }
    val updatedIsEnabled = currentButtonState != ButtonStateEnum.Loading
    Button(
        onClick = { onClick() },
        enabled = updatedIsEnabled && isEnabled,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = .4f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = if (currentButtonState == ButtonStateEnum.Error) MaterialTheme.colorScheme.error.copy(
                alpha = .85f
            ) else MaterialTheme.colorScheme.primary
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!updatedIsEnabled) {
                NBallLoader(
                    activatedColor = MaterialTheme.colorScheme.onPrimary,
                    deactivatedColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.size(27.dp)
                )
                SpacerWidth12()
            }
            if (currentButtonState == ButtonStateEnum.Error) {
                Image(
                    imageVector = Icons.Filled.Refresh,
                    modifier = Modifier.size(18.dp),
                    contentDescription = errorText,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                )
                SpacerWidth12()
            }
            Text(
                text = when (currentButtonState) {
                    ButtonStateEnum.Loading -> {
                        loadingText
                    }

                    ButtonStateEnum.NotLoading -> {
                        buttonText
                    }

                    ButtonStateEnum.Error -> {
                        errorText
                    }

                    ButtonStateEnum.Success -> {
                        successText
                    }
                }
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewLoaderButton() {
    val buttonLoadingState = remember {
        mutableStateOf(ButtonStateEnum.Loading)
    }
    LoaderButton(
        loaderButtonState = buttonLoadingState,
        buttonText = "Hi Checking"
    ) {

    }
}