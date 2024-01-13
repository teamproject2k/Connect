package com.example.connect.presentation.ui.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    showNavigationIcon: Boolean = false,
    navigationIcon: ImageVector = Icons.Default.ArrowBack,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavigationIconClick: () -> Unit = {}
) {
    Surface(shadowElevation = 3.dp) {
        TopAppBar(
            title = {
                TextBold18(text = title)
            },
            actions = {
                actions()
            },
            modifier = modifier,
            colors = colors,
            windowInsets = windowInsets,
            scrollBehavior = scrollBehavior,
            navigationIcon = {
                if (showNavigationIcon) {
                    IconButton(onClick = { onNavigationIconClick() }) {
                        Icon(
                            imageVector = navigationIcon,
                            contentDescription = title
                        )
                    }
                }
            }
        )
    }
}