package com.onelinediary.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import com.onelinediary.app.generated.resources.Res
import com.onelinediary.app.generated.resources.pretendard_regular
import com.onelinediary.app.generated.resources.pretendard_bold
import com.onelinediary.app.generated.resources.noto_sans_symbols2

@Composable
fun getAppFontFamily(): FontFamily {
    return FontFamily(
        // 1. Primary 본문/제목 글꼴: Pretendard (한글, 영문, 숫자 고품질 렌더링)
        Font(Res.font.pretendard_regular, FontWeight.Normal, FontStyle.Normal),
        Font(Res.font.pretendard_regular, FontWeight.Medium, FontStyle.Normal),
        Font(Res.font.pretendard_bold, FontWeight.Bold, FontStyle.Normal),
        Font(Res.font.pretendard_bold, FontWeight.SemiBold, FontStyle.Normal),

        // 2. Fallback 특수 기호 글꼴: Noto Sans Symbols 2 (Primary에 없는 특수 기호 자동 fallback)
        Font(Res.font.noto_sans_symbols2, FontWeight.Normal, FontStyle.Normal),
        Font(Res.font.noto_sans_symbols2, FontWeight.Medium, FontStyle.Normal),
        Font(Res.font.noto_sans_symbols2, FontWeight.Bold, FontStyle.Normal),
        Font(Res.font.noto_sans_symbols2, FontWeight.SemiBold, FontStyle.Normal)
    )
}

@Composable
fun getAppTypography(): Typography {
    val fontFamily = getAppFontFamily()
    val default = Typography()
    return Typography(
        displayLarge = default.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = default.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = default.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = default.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = default.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = default.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = default.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = default.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = default.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = default.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = default.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = default.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = default.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = default.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = default.labelSmall.copy(fontFamily = fontFamily)
    )
}

@Composable
fun OneLineDiaryTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = getAppTypography(),
        content = content
    )
}
