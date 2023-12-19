package com.example.connect.presentation.ui.models

import androidx.annotation.DrawableRes
import com.example.connect.common.VisibilityScopeEnum

data class VisibilityScope(
    val id: Int,
    val scopeName: String,
    val scopeEnum: VisibilityScopeEnum,
    val scopeDescription: String,
    @DrawableRes val drawableId: Int
)