package com.teamproject2k.connect.presentation.ui.models

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomAppBarItemData(
    val text: String,
    val selectedIcon: ImageVector,
    val unSelectedIcon: ImageVector,
    val routeName: String
)