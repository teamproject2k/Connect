package com.example.connect.presentation.ui.theme

import androidx.compose.material3.Typography

private val DefaultTypography = Typography()
val PoppinsTypography = Typography(
    displayLarge = DefaultTypography.displayLarge.copy(fontFamily = Poppins),
    displayMedium = DefaultTypography.displayMedium.copy(fontFamily = Poppins),
    displaySmall = DefaultTypography.displaySmall.copy(fontFamily = Poppins),

    headlineLarge = DefaultTypography.headlineLarge.copy(fontFamily = Poppins),
    headlineMedium = DefaultTypography.headlineMedium.copy(fontFamily = Poppins),
    headlineSmall = DefaultTypography.headlineSmall.copy(fontFamily = Poppins),

    titleLarge = DefaultTypography.titleLarge.copy(fontFamily = Poppins),
    titleMedium = DefaultTypography.titleMedium.copy(fontFamily = Poppins),
    titleSmall = DefaultTypography.titleSmall.copy(fontFamily = Poppins),

    bodyLarge = DefaultTypography.bodyLarge.copy(fontFamily = Poppins),
    bodyMedium = DefaultTypography.bodyMedium.copy(fontFamily = Poppins),
    bodySmall = DefaultTypography.bodySmall.copy(fontFamily = Poppins),

    labelLarge = DefaultTypography.labelLarge.copy(fontFamily = Poppins),
    labelMedium = DefaultTypography.labelMedium.copy(fontFamily = Poppins),
    labelSmall = DefaultTypography.labelSmall.copy(fontFamily = Poppins)
)