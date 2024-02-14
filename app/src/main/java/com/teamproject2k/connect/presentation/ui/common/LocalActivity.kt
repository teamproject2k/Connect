package com.teamproject2k.connect.presentation.ui.common

import androidx.activity.ComponentActivity
import androidx.compose.runtime.staticCompositionLocalOf

val LocalActivity = staticCompositionLocalOf<ComponentActivity> {
    noLocalProvidedFor()
}

private fun noLocalProvidedFor(): Nothing {
    error("CompositionLocal LocalActivity not present")
}