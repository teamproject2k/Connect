package com.example.connect.presentation.ui.models

import androidx.annotation.DrawableRes

data class VisibilityScope(
    val id: Int,
    val scopeName: String,
    val scopeDescription: String,
    @DrawableRes val drawableId: Int
)