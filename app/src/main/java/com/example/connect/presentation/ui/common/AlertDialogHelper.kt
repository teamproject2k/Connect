package com.example.connect.presentation.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.connect.R

@Composable
fun TitleMessageIconOkCancelDialog(
    title: String,
    subTitle: String,
    imageVector: ImageVector? = null,
    iconTint: Color = LocalContentColor.current,
    positiveButtonText: String = stringResource(id = R.string.ok),
    negativeButtonText: String = stringResource(id = R.string.cancel),
    dialogProperties: DialogProperties = DialogProperties(),
    onCancel: () -> Unit,
    onOk: () -> Unit
) {
    AlertDialog(
        properties = dialogProperties,
        onDismissRequest = { onCancel() },
        confirmButton = {
            Text(text = positiveButtonText, modifier = Modifier.clickable {
                onOk()
            })
        },
        dismissButton = {
            Text(
                text = negativeButtonText,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clickable {
                        onCancel()
                    }
            )
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (imageVector != null) {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = title,
                        tint = iconTint,
                    )
                    SpacerWidth12()
                }
                TextBold18(text = title)
            }
        },
        text = {
            Text(text = subTitle)
        }
    )
}