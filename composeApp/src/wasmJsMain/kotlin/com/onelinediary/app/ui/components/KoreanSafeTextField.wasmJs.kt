package com.onelinediary.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onelinediary.app.ui.theme.SpaceCardBackground
import com.onelinediary.app.ui.theme.StarDim
import com.onelinediary.app.ui.theme.StarlightBlue
import com.onelinediary.app.ui.theme.getAppFontFamily
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent

@JsFun("(e) => Boolean(e.isComposing)")
private external fun isEventComposing(e: Event): Boolean

/**
 * Web(Wasm) 전용 한 줄 일기 텍스트 입력 컴포넌트.
 * 브라우저 및 OS 레벨 IME(한/영 키 `HangulMode` 전환 및 일본어 `KanaMode`/로마자 전환)가
 * 캔버스 상에서 비활성화되는 문제를 해결하기 위해, 실제 DOM `<input>` 요소와 Compose 레이아웃을
 * 실시간 동기화하는 고성능 브릿지 패턴을 적용합니다.
 */
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
    val density = LocalDensity.current.density
    val appFontFamily = getAppFontFamily()

    // 1. 실제 DOM HTMLInputElement 생성 및 속성 초기화
    val inputElement = remember {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "text"
        input.className = "wasm-diary-input"
        input.placeholder = placeholder
        input.maxLength = maxChars
        input.value = value
        input.setAttribute("autocomplete", "off")
        input.setAttribute("autocorrect", "off")
        input.setAttribute("autocapitalize", "off")
        input.setAttribute("spellcheck", "false")
        input
    }

    // 2. 외부에서 value가 갱신되었을 때 (초기 로드, 날짜 전환 등) DOM input 동기화
    LaunchedEffect(value) {
        if (inputElement.value != value) {
            inputElement.value = value
        }
    }

    // 3. enabled 상태 (다이얼로그 열림/닫힘 등)에 따른 가시성 및 활성화 제어
    LaunchedEffect(enabled) {
        inputElement.disabled = !enabled
        inputElement.style.display = if (enabled) "block" else "none"
        inputElement.style.visibility = if (enabled) "visible" else "hidden"
        if (!enabled) {
            inputElement.blur()
        }
    }

    // 4. Compose 레이아웃 좌표와 실제 DOM input 엘리먼트 위치 동기화
    var lastCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val updatePosition: () -> Unit = remember(density, enabled) {
        {
            val coords = lastCoordinates
            if (coords != null && coords.isAttached && enabled) {
                val canvas = document.querySelector("canvas")
                val canvasRect = canvas?.getBoundingClientRect()
                val canvasLeft = canvasRect?.left ?: 0.0
                val canvasTop = canvasRect?.top ?: 0.0

                val posInWindow = coords.positionInWindow()
                val x = canvasLeft + (posInWindow.x / density)
                val y = canvasTop + (posInWindow.y / density)
                val w = coords.size.width / density
                val h = coords.size.height / density

                inputElement.style.left = "${x}px"
                inputElement.style.top = "${y}px"
                inputElement.style.width = "${w}px"
                inputElement.style.height = "${h}px"

                // 뷰포트 밖으로 스크롤되었을 때 가상 숨김 처리
                val isOffScreen = y < -80 || y > (window.innerHeight + 80)
                inputElement.style.display = if (isOffScreen) "none" else "block"
                inputElement.style.visibility = if (isOffScreen) "hidden" else "visible"
            }
        }
    }

    // 5. DOM 이벤트 리스너 등록 및 언마운트 시 정리
    DisposableEffect(inputElement) {
        val root = document.getElementById("root") ?: document.body
        root?.appendChild(inputElement)

        val inputListener: (Event) -> Unit = {
            val newText = inputElement.value
            if (newText != value) {
                onValueChange(newText)
            }
        }

        val keydownListener: (Event) -> Unit = { event ->
            val keyEvent = event as? KeyboardEvent
            if (keyEvent?.key == "Enter") {
                // 한글/일본어 IME 조합(Composition) 중인 엔터는 제출을 차단하고 문자 완성만 허용
                if (!isEventComposing(event)) {
                    onDone()
                }
            }
        }

        val resizeListener: (Event) -> Unit = {
            updatePosition()
        }

        val scrollListener: (Event) -> Unit = {
            updatePosition()
        }

        inputElement.addEventListener("input", inputListener)
        inputElement.addEventListener("keydown", keydownListener)
        window.addEventListener("resize", resizeListener)
        window.addEventListener("scroll", scrollListener)

        onDispose {
            inputElement.removeEventListener("input", inputListener)
            inputElement.removeEventListener("keydown", keydownListener)
            window.removeEventListener("resize", resizeListener)
            window.removeEventListener("scroll", scrollListener)
            inputElement.remove()
        }
    }

    // 6. Compose 캔버스 렌더링 영역 (자리 표시자 및 하단 글자 수 카운터)
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SpaceCardBackground)
                .border(
                    width = 1.dp,
                    color = StarlightBlue.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (enabled) {
                        inputElement.focus()
                    }
                }
                .onGloballyPositioned { coordinates ->
                    lastCoordinates = coordinates
                    updatePosition()
                }
        )

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
                text = "${value.length} / $maxChars",
                color = if (value.length >= maxChars) Color(0xFFFF8E8E) else StarDim.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontFamily = appFontFamily
            )
        }
    }
}
