package com.example.connect.presentation.ui.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
private fun SpacerGeneric(height: Dp = 0.dp, width: Dp = 0.dp) {
    Spacer(
        modifier = Modifier
            .height(height)
            .width(width)
    )
}


//height
@Composable
fun SpacerHeight6() {
    SpacerGeneric(height = 6.dp)
}

@Composable
fun SpacerHeight8() {
    SpacerGeneric(height = 8.dp)
}


@Composable
fun SpacerHeight12() {
    SpacerGeneric(height = 12.dp)
}

@Composable
fun SpacerHeight18() {
    SpacerGeneric(height = 18.dp)
}

@Composable
fun SpacerHeight24() {
    SpacerGeneric(height = 24.dp)
}


@Composable
fun SpacerHeight48() {
    SpacerGeneric(height = 48.dp)
}

//width
@Composable
fun SpacerWidth12() {
    SpacerGeneric(width = 12.dp)
}

@Composable
fun SpacerWidth18() {
    SpacerGeneric(width = 18.dp)
}


@Composable
fun SpacerWidth8() {
    SpacerGeneric(width = 8.dp)
}


@Composable
fun SpacerWidth6() {
    SpacerGeneric(width = 6.dp)
}




