package com.example.connect.presentation.ui.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.connect.presentation.ui.theme.WarningColor

object ColorsHelper {

    @Composable
    fun lightGray(): Color {
        return if (isSystemInDarkTheme()) Color.LightGray else Color.LightGray
    }

    @Composable
    fun gray(): Color {
        return if (isSystemInDarkTheme()) Color.Gray else Color.Gray
    }

    @Composable
    fun warning(): Color {
        return if (isSystemInDarkTheme()) WarningColor else WarningColor
    }
}