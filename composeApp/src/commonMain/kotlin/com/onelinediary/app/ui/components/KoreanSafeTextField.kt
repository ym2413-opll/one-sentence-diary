package com.onelinediary.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onelinediary.app.ui.theme.SpaceCardBackground
import com.onelinediary.app.ui.theme.StarDim
import com.onelinediary.app.ui.theme.StarlightBlue
import com.onelinediary.app.ui.theme.StarWhite
import com.onelinediary.app.ui.theme.getAppFontFamily

/**
 * 기본 Compose 1.7+ TextFieldState 기반 텍스트 입력 컴포넌트.
 * Android, Desktop(JVM), iOS 등 플랫폼 네이티브 환경에서 한글 자모 분리나 글자 중복을 원천 방지합니다.
 */
@Composable
fun DefaultKoreanSafeOneLineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "오늘 하루를 한 문장으로 남겨보세요...",
    modifier: Modifier = Modifier,
    maxChars: Int = 100,
    enabled: Boolean = true,
    onDone: () -> Unit = {}
) {
    val textFieldState = rememberTextFieldState(initialText = value)
    val appFontFamily = getAppFontFamily()

    // 1. 외부에서 value가 변경되었을 때 (초기 로드, 날짜 전환 등) TextFieldState와 동기화
    LaunchedEffect(value) {
        if (value != textFieldState.text.toString()) {
            textFieldState.setTextAndPlaceCursorAtEnd(value)
        }
    }

    // 2. 사용자가 입력한 내용을 상위 상태에 반영
    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .collect { newText ->
                if (newText != value) {
                    onValueChange(newText)
                }
            }
    }

    val currentTextLength = textFieldState.text.length

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SpaceCardBackground)
                .border(
                    width = 1.dp,
                    color = StarlightBlue.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                state = textFieldState,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                textStyle = TextStyle(
                    fontFamily = appFontFamily,
                    color = StarWhite,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                ),
                cursorBrush = SolidColor(StarlightBlue),
                lineLimits = TextFieldLineLimits.SingleLine,
                inputTransformation = InputTransformation.maxLength(maxChars),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                onKeyboardAction = {
                    onDone()
                },
                decorator = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (textFieldState.text.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = StarDim.copy(alpha = 0.6f),
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                fontFamily = appFontFamily
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "한 문장으로 기록",
                color = StarDim.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontFamily = appFontFamily
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$currentTextLength / $maxChars",
                color = if (currentTextLength >= maxChars) Color(0xFFFF8E8E) else StarDim.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontFamily = appFontFamily
            )
        }
    }
}

/**
 * 플랫폼별 최적화된 한 줄 일기 텍스트 입력 컴포넌트.
 * Web(Wasm)에서는 브라우저 OS IME(한/영 키 및 일본어 가나 전환)를 100% 보장하는 DOM Bridge를 사용하고,
 * Android / Desktop(JVM) / iOS에서는 Compose Native TextFieldState를 사용합니다.
 */
@Composable
expect fun KoreanSafeOneLineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "오늘 하루를 한 문장으로 남겨보세요...",
    modifier: Modifier = Modifier,
    maxChars: Int = 100,
    enabled: Boolean = true,
    onDone: () -> Unit = {}
)
