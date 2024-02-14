package com.teamproject2k.connect.presentation.ui.theme

import androidx.compose.material3.Typography

private val DefaultTypography = Typography()
val PoppinsTypography = Typography(
    displayLarge = DefaultTypography.displayLarge.copy(fontFamily = Roboto),
    displayMedium = DefaultTypography.displayMedium.copy(fontFamily = Roboto),
    displaySmall = DefaultTypography.displaySmall.copy(fontFamily = Roboto),

    headlineLarge = DefaultTypography.headlineLarge.copy(fontFamily = Roboto),
    headlineMedium = DefaultTypography.headlineMedium.copy(fontFamily = Roboto),
    headlineSmall = DefaultTypography.headlineSmall.copy(fontFamily = Roboto),

    titleLarge = DefaultTypography.titleLarge.copy(fontFamily = Roboto),
    titleMedium = DefaultTypography.titleMedium.copy(fontFamily = Roboto),
    titleSmall = DefaultTypography.titleSmall.copy(fontFamily = Roboto),

    bodyLarge = DefaultTypography.bodyLarge.copy(fontFamily = Roboto),
    bodyMedium = DefaultTypography.bodyMedium.copy(fontFamily = Roboto),
    bodySmall = DefaultTypography.bodySmall.copy(fontFamily = Roboto),

    labelLarge = DefaultTypography.labelLarge.copy(fontFamily = Roboto),
    labelMedium = DefaultTypography.labelMedium.copy(fontFamily = Roboto),
    labelSmall = DefaultTypography.labelSmall.copy(fontFamily = Roboto)
)