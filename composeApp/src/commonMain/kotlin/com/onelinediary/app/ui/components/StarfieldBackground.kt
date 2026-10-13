package com.onelinediary.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onelinediary.app.data.model.StarDiary
import com.onelinediary.app.ui.theme.SpaceBlack
import com.onelinediary.app.ui.theme.SpaceCardBackground
import com.onelinediary.app.ui.theme.SpaceDeepBlue
import com.onelinediary.app.ui.theme.StarDim
import com.onelinediary.app.ui.theme.StarGold
import com.onelinediary.app.ui.theme.StarlightBlue
import com.onelinediary.app.ui.theme.StarWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

private data class StarParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val baseAlpha: Float
)

@Composable
fun StarfieldBackground(
    starPool: List<StarDiary>,
    onBackgroundClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 배경 별 파티클 (고정된 무작위 좌표)
    val stars = remember {
        val random = Random(42)
        List(60) {
            StarParticle(
                xRatio = random.nextFloat(),
                yRatio = random.nextFloat(),
                radius = random.nextFloat() * 1.8f + 0.8f,
                baseAlpha = random.nextFloat() * 0.5f + 0.2f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "star_twinkle")
    val twinklePulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle_alpha"
    )

    // 화면 탭 시 모든 부유 일기를 강제로 새로 스폰시키기 위한 트리거 카운터
    var globalTrigger by remember { mutableIntStateOf(0) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SpaceBlack,
                        SpaceDeepBlue.copy(alpha = 0.85f),
                        SpaceBlack
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                globalTrigger++
                onBackgroundClick()
            }
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // 1. 반짝이는 우주 별빛 렌더링
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            for (star in stars) {
                val cx = star.xRatio * width
                val cy = star.yRatio * height
                val alpha = (star.baseAlpha * twinklePulse).coerceIn(0.1f, 0.9f)
                drawCircle(
                    color = StarWhite.copy(alpha = alpha),
                    radius = star.radius,
                    center = Offset(cx, cy)
                )
            }
        }

        // 2. 밤하늘을 떠서 움직이고, 사라지면 새로운 일기가 튀어나오는 3개의 부유 일기 스트림
        if (starPool.isNotEmpty()) {
            FloatingDiaryDrifter(
                slotIndex = 0,
                starPool = starPool,
                triggerKey = globalTrigger,
                initialDelayMs = 0L,
                baseYPercent = 0.12f, // 화면 상단 영역
                screenWidthPx = screenWidth.value,
                screenHeightPx = screenHeight.value
            )

            FloatingDiaryDrifter(
                slotIndex = 1,
                starPool = starPool,
                triggerKey = globalTrigger,
                initialDelayMs = 4500L,
                baseYPercent = 0.48f, // 화면 중단 영역
                screenWidthPx = screenWidth.value,
                screenHeightPx = screenHeight.value
            )

            FloatingDiaryDrifter(
                slotIndex = 2,
                starPool = starPool,
                triggerKey = globalTrigger,
                initialDelayMs = 9000L,
                baseYPercent = 0.76f, // 화면 하단 영역
                screenWidthPx = screenWidth.value,
                screenHeightPx = screenHeight.value
            )
        }
    }
}

/**
 * 하나의 일기가 은은하게 떠서 움직이고(drift),
 * 사라지면 즉시 새로운 일기가 튀어나오는(re-spawn) 독립적인 부유 컴포넌트입니다.
 */
@Composable
private fun FloatingDiaryDrifter(
    slotIndex: Int,
    starPool: List<StarDiary>,
    triggerKey: Int,
    initialDelayMs: Long,
    baseYPercent: Float,
    screenWidthPx: Float,
    screenHeightPx: Float
) {
    var currentDiary by remember { mutableStateOf<StarDiary?>(null) }
    var diarySpawnCounter by remember { mutableIntStateOf(0) }

    val alphaAnim = remember { Animatable(0f) }
    val offsetXAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(0f) }

    // 부유 애니메이션 및 새 일기 교체 생명주기 루프
    LaunchedEffect(triggerKey, diarySpawnCounter) {
        if (starPool.isEmpty()) return@LaunchedEffect

        // 첫 시작 시 슬롯별 시간차(staggered) 딜레이 적용
        if (diarySpawnCounter == 0 && initialDelayMs > 0) {
            delay(initialDelayMs)
        }

        while (isActive) {
            // 1. 1000개의 풀에서 무작위 새 일기 선택
            currentDiary = starPool.random()

            // 2. 부유 경로 계산 (랜덤 시작 위치 및 유영 거리)
            val random = Random.Default
            val isMoveRight = random.nextBoolean()

            // 시작 좌표 설정
            val startX = if (isMoveRight) random.nextInt(10, 40).toFloat() else random.nextInt(60, 100).toFloat()
            val targetX = if (isMoveRight) startX + random.nextInt(80, 140) else startX - random.nextInt(80, 140)

            val startY = (baseYPercent * screenHeightPx) + random.nextInt(-20, 20)
            val targetY = startY + random.nextInt(-40, 40) // 위아래로 살랑살랑 유영

            offsetXAnim.snapTo(startX)
            offsetYAnim.snapTo(startY)
            alphaAnim.snapTo(0f)

            // 3. 서서히 밤하늘에 나타남 (Fade In: 2.2초)
            val fadeInJob = launch {
                alphaAnim.animateTo(
                    targetValue = 0.52f,
                    animationSpec = tween(durationMillis = 2200, easing = LinearEasing)
                )
            }

            // 4. 유영하며 떠다님 (Drift: 11~14초 동안 천천히 부드럽게 이동)
            val driftDuration = random.nextInt(11000, 14500)
            val moveXJob = launch {
                offsetXAnim.animateTo(
                    targetValue = targetX,
                    animationSpec = tween(durationMillis = driftDuration, easing = LinearEasing)
                )
            }
            val moveYJob = launch {
                offsetYAnim.animateTo(
                    targetValue = targetY,
                    animationSpec = tween(durationMillis = driftDuration, easing = FastOutSlowInEasing)
                )
            }

            // 부유 중 유지 시간 대기 (약 8초 뒤부터 페이드아웃 준비)
            delay((driftDuration - 2200).coerceAtLeast(3000).toLong())

            // 5. 밤하늘 속으로 서서히 스며들며 사라짐 (Fade Out: 2초)
            alphaAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
            )

            fadeInJob.cancel()
            moveXJob.cancel()
            moveYJob.cancel()

            // 6. 완전히 없어지면 잠시 숨을 고르고, 곧바로 다음 새로운 일기가 튀어나옴!
            delay(random.nextLong(1200, 2500))
        }
    }

    // 렌더링: 떠다니는 일기 카드
    currentDiary?.let { diary ->
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = offsetXAnim.value.dp.roundToPx(),
                        y = offsetYAnim.value.dp.roundToPx()
                    )
                }
                .alpha(alphaAnim.value)
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(SpaceCardBackground.copy(alpha = 0.55f))
                .border(
                    width = 1.dp,
                    color = StarlightBlue.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // 사용자가 탭하면 바로 페이드아웃되고 다음 새 일기가 튀어나오도록 트리거
                    diarySpawnCounter++
                }
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = "★ ${diary.universeOrigin}",
                    color = StarGold.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"${diary.content}\"",
                    color = StarWhite.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
