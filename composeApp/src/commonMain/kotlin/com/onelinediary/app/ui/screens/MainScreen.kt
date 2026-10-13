package com.onelinediary.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onelinediary.app.data.model.DiaryEntry
import com.onelinediary.app.data.model.StarDiary
import com.onelinediary.app.ui.components.BackupRestoreDialog
import com.onelinediary.app.ui.components.KoreanSafeOneLineTextField
import com.onelinediary.app.ui.components.SettingsDialog
import com.onelinediary.app.ui.components.StarfieldBackground
import com.onelinediary.app.ui.theme.SpaceCardBackground
import com.onelinediary.app.ui.theme.SpaceSurfaceTranslucent
import com.onelinediary.app.ui.theme.StarDim
import com.onelinediary.app.ui.theme.StarGold
import com.onelinediary.app.ui.theme.StarlightBlue
import com.onelinediary.app.ui.theme.StarNebulaPurple
import com.onelinediary.app.ui.theme.StarWhite
import com.onelinediary.app.util.DateUtils

@Composable
fun MainScreen(
    currentLogicalDate: String,
    todayEntry: DiaryEntry?,
    pastEntries: List<DiaryEntry>,
    displayStarDiaries: List<StarDiary>,
    starPool: List<StarDiary>,
    starPoolSize: Int,
    lastUpdateDate: String,
    apiKey: String,
    isRefreshingStars: Boolean,
    onSaveDiary: (date: String, content: String) -> Unit,
    onDeleteDiary: (id: String) -> Unit,
    onShuffleStars: () -> Unit,
    onSaveApiKey: (String) -> Unit,
    onTriggerRefreshStars: () -> Unit,
    onExportBackupJson: () -> String,
    onImportBackupJson: (String, Boolean) -> Result<Int>,
    onLockApp: () -> Unit
) {
    var diaryInputText by remember(todayEntry) {
        mutableStateOf(todayEntry?.content ?: "")
    }

    var isBackupDialogOpen by remember { mutableStateOf(false) }
    var isSettingsDialogOpen by remember { mutableStateOf(false) }
    var saveFeedbackMessage by remember { mutableStateOf<String?>(null) }

    // 저장 성공 피드백 3초 후 초기화
    LaunchedEffect(saveFeedbackMessage) {
        if (saveFeedbackMessage != null) {
            kotlinx.coroutines.delay(2500)
            saveFeedbackMessage = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. 밤하늘 배경 및 떠서 움직이다가 사라지면 새 일기가 튀어나오는 동적 부유 일기 시스템
        StarfieldBackground(
            starPool = if (starPool.isNotEmpty()) starPool else displayStarDiaries,
            onBackgroundClick = onShuffleStars,
            modifier = Modifier.fillMaxSize()
        )

        // 2. 메인 전경 인터페이스 (스크롤 가능한 콘텐츠)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onShuffleStars() }
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // --- 상단 헤더 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "한줄일기",
                        color = StarWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SpaceCardBackground.copy(alpha = 0.6f))
                            .clickable { onShuffleStars() }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "✦ 어딘가 누군가의 밤",
                            color = StarGold.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "↻ 교체",
                            color = StarlightBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 잠금 버튼
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SpaceCardBackground)
                        .clickable { onLockApp() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔒", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 백업/복원 버튼
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SpaceCardBackground)
                        .clickable { isBackupDialogOpen = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("💾", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 설정 버튼
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SpaceCardBackground)
                        .clickable { isSettingsDialogOpen = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚙️", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 스크롤 가능한 메인 일기 리스트 & 작성 뷰
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- 오늘 일기 작성 카드 ---
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = StarlightBlue.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SpaceSurfaceTranslucent)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = DateUtils.formatDisplayDate(currentLogicalDate, isLogicalToday = true),
                                    color = StarWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "오전 6시 갱신",
                                    color = StarDim.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 한글 안전 한 줄 텍스트 입력창
                            KoreanSafeOneLineTextField(
                                value = diaryInputText,
                                onValueChange = { diaryInputText = it },
                                placeholder = "오늘 하루를 나타내는 한 문장을 적어보세요...",
                                maxChars = 100,
                                onDone = {
                                    if (diaryInputText.isNotBlank()) {
                                        onSaveDiary(currentLogicalDate, diaryInputText)
                                        saveFeedbackMessage = "오늘의 일기가 별자리에 기록되었습니다."
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (saveFeedbackMessage != null) {
                                    Text(
                                        text = saveFeedbackMessage ?: "",
                                        color = StarlightBlue,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                if (todayEntry != null) {
                                    OutlinedButton(
                                        onClick = {
                                            onDeleteDiary(todayEntry.id)
                                            diaryInputText = ""
                                            saveFeedbackMessage = "일기를 삭제했습니다."
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text("삭제", color = Color(0xFFFF8E8E), fontSize = 12.sp)
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (diaryInputText.isNotBlank()) {
                                            onSaveDiary(currentLogicalDate, diaryInputText)
                                            saveFeedbackMessage = "오늘의 일기가 별자리에 기록되었습니다."
                                        }
                                    },
                                    enabled = diaryInputText.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = StarlightBlue,
                                        disabledContainerColor = StarlightBlue.copy(alpha = 0.2f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = if (todayEntry == null) "별에 새기기" else "수정 완료",
                                        color = if (diaryInputText.isNotBlank()) Color.Black else StarDim,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 지난 일기 목록 헤더 ---
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "지난 기록들",
                            color = StarWhite.copy(alpha = 0.9f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${pastEntries.size})",
                            color = StarDim,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SpaceCardBackground.copy(alpha = 0.5f))
                                .clickable { onShuffleStars() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "↻ 다른 일기 띄우기",
                                color = StarlightBlue.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // --- 지난 일기 카드 리스트 ---
                if (pastEntries.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = StarlightBlue.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(18.dp)
                                ),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SpaceSurfaceTranslucent)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp, horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "📖 아직 기록된 지난 일기가 없습니다",
                                    color = StarWhite.copy(alpha = 0.9f),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "오늘 하루를 나타내는 한 문장을 기록해보세요.\n날마다 기록된 당신의 하루가 이곳에 차곡차곡 보관됩니다.",
                                    color = StarDim.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(pastEntries, key = { it.id }) { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SpaceCardBackground)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = DateUtils.formatDisplayDate(entry.date),
                                        color = StarGold.copy(alpha = 0.85f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = "✕",
                                        color = StarDim.copy(alpha = 0.4f),
                                        fontSize = 12.sp,
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .clickable { onDeleteDiary(entry.id) }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = entry.content,
                                    color = StarWhite,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onShuffleStars() },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(SpaceSurfaceTranslucent)
                                .border(
                                    width = 1.dp,
                                    color = StarlightBlue.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "✦ 화면을 탭하면 어딘가의 새로운 익명 일기가 떠오릅니다",
                                color = StarDim.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. 백업 및 복원 다이얼로그
        BackupRestoreDialog(
            isOpen = isBackupDialogOpen,
            onDismiss = { isBackupDialogOpen = false },
            onExportJson = onExportBackupJson,
            onImportJson = onImportBackupJson
        )

        // 4. 설정 다이얼로그
        SettingsDialog(
            isOpen = isSettingsDialogOpen,
            onDismiss = { isSettingsDialogOpen = false },
            currentPoolSize = starPoolSize,
            lastUpdateDate = lastUpdateDate,
            apiKey = apiKey,
            isRefreshing = isRefreshingStars,
            onSaveApiKey = onSaveApiKey,
            onTriggerRefresh = onTriggerRefreshStars,
            onLockApp = onLockApp
        )
    }
}
