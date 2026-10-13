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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onelinediary.app.ui.theme.SpaceCardBackground
import com.onelinediary.app.ui.theme.StarDim
import com.onelinediary.app.ui.theme.StarlightBlue
import com.onelinediary.app.ui.theme.StarWhite

/**
 * Jetpack Compose 환경에서 한글 조합(IME) 자모 분리나 글자 중복 버그를 방지하고
 * 한 줄(문장) 입력을 쾌적하게 돕는 안전한 한 줄 텍스트 필드입니다.
 */
@Composable
fun KoreanSafeOneLineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "오늘 하루를 한 문장으로 남겨보세요...",
    modifier: Modifier = Modifier,
    maxChars: Int = 100,
    onDone: () -> Unit = {}
) {
    // 한글 조합(Composition) 정보를 보존하기 위해 TextFieldValue를 내부 상태로 유지합니다.
    var textFieldValueState by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }

    // 외부에서 value가 변경되었을 때 (예: 초기화, 다른 날짜 선택) 동기화
    LaunchedEffect(value) {
        if (value != textFieldValueState.text) {
            textFieldValueState = TextFieldValue(text = value, selection = TextRange(value.length))
        }
    }

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
            if (textFieldValueState.text.isEmpty()) {
                Text(
                    text = placeholder,
                    color = StarDim.copy(alpha = 0.6f),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }

            BasicTextField(
                value = textFieldValueState,
                onValueChange = { newTfv ->
                    // 1. 엔터(줄바꿈) 차단 - 한 줄 일기이므로 개행 문자는 배제하고 완료 액션 처리
                    val sanitizedText = newTfv.text.replace("\n", "").replace("\r", "")

                    // 2. 한글 조합 중(composition != null)일 때는 글자 수 제한 등의 필터링을 미루어
                    // IME 자모 분리 및 중복 현상을 완벽하게 방지합니다.
                    if (newTfv.composition != null) {
                        textFieldValueState = newTfv.copy(text = sanitizedText)
                        onValueChange(sanitizedText)
                    } else {
                        // 조합이 완료된 시점에만 최대 글자 수를 제한합니다.
                        val limitedText = if (sanitizedText.length > maxChars) {
                            sanitizedText.take(maxChars)
                        } else {
                            sanitizedText
                        }
                        val finalSelection = TextRange(minOf(newTfv.selection.end, limitedText.length))
                        textFieldValueState = newTfv.copy(
                            text = limitedText,
                            selection = finalSelection,
                            composition = null
                        )
                        onValueChange(limitedText)
                    }

                    if (newTfv.text.contains("\n")) {
                        onDone()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    color = StarWhite,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                ),
                cursorBrush = SolidColor(StarlightBlue),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onDone() }
                )
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
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${textFieldValueState.text.length} / $maxChars",
                color = if (textFieldValueState.text.length >= maxChars) Color(0xFFFF8E8E) else StarDim.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
    }
}
