package com.example.connect.presentation.ui.models

import androidx.annotation.DrawableRes
import com.example.connect.domain.utils.VisibilityScopeEnum

data class VisibilityScope(
    val id: Int,
    val scopeName: String,
    val scopeEnum: VisibilityScopeEnum,
    val scopeDescription: String,
    @DrawableRes val drawableId: Int
)