package com.example.connect.presentation.ui.common

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.connect.R
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.FunctionHelper
import com.example.connect.presentation.utils.FunctionHelper.getFileSize
import com.example.connect.presentation.utils.FunctionHelper.showToast


@Composable
fun mediaPicker(
    maxMediaSize: Long = ConstantsHelper.MAX_ALLOWED_FILE_SIZE,
    restrictForMaxFileSize: Boolean = true,
    onMediaPick: (uri: Uri) -> Unit
): ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?> {
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) {
                // do not give any callback as file is not picked
            } else if (
                restrictForMaxFileSize && getFileSize(context.contentResolver, uri) > maxMediaSize
            ) {
                context.showToast(
                    context.getString(
                        R.string.file_size_can_t_be_greater_than_max_allowed,
                        FunctionHelper.formatFileSize(maxMediaSize)
                    )
                )
            } else {
                onMediaPick(uri)
            }
        }
    return launcher
}


