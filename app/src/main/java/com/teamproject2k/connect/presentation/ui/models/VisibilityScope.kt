package com.teamproject2k.connect.presentation.ui.models

import androidx.annotation.DrawableRes
import com.teamproject2k.connect.domain.utils.VisibilityScopeEnum

data class VisibilityScope(
    val id: Int,
    val scopeName: String,
    val scopeEnum: VisibilityScopeEnum,
    val scopeDescription: String,
    @DrawableRes val drawableId: Int
)