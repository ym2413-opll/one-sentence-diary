package com.onelinediary.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun KoreanSafeOneLineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier,
    maxChars: Int,
    enabled: Boolean,
    onDone: () -> Unit
) {
    DefaultKoreanSafeOneLineTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        modifier = modifier,
        maxChars = maxChars,
        enabled = enabled,
        onDone = onDone
    )
}
