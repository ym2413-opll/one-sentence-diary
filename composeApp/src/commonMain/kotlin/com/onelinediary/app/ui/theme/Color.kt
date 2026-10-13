package com.onelinediary.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val SpaceBlack = Color(0xFF090A10)
val SpaceDeepBlue = Color(0xFF101426)
val SpaceSurface = Color(0xFF171B33)
val SpaceSurfaceTranslucent = Color(0xF414182E)
val SpaceCardBackground = Color(0xDE1B2244)

val StarWhite = Color(0xFFF0F4FF)
val StarDim = Color(0xFF8B95B8)
val StarGold = Color(0xFFFFD56B)
val StarlightBlue = Color(0xFF82B1FF)
val StarNebulaPurple = Color(0xFFC084FC)

val DarkColorScheme = darkColorScheme(
    primary = StarlightBlue,
    onPrimary = SpaceBlack,
    secondary = StarGold,
    onSecondary = SpaceBlack,
    tertiary = StarNebulaPurple,
    background = SpaceBlack,
    surface = SpaceSurface,
    onBackground = StarWhite,
    onSurface = StarWhite
)
