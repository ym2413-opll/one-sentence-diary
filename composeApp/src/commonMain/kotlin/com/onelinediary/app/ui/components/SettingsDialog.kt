package com.onelinediary.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.onelinediary.app.data.autostart.getPlatformAutoStart
import com.onelinediary.app.ui.theme.SpaceCardBackground
import com.onelinediary.app.ui.theme.SpaceSurface
import com.onelinediary.app.ui.theme.StarDim
import com.onelinediary.app.ui.theme.StarGold
import com.onelinediary.app.ui.theme.StarlightBlue
import com.onelinediary.app.ui.theme.StarNebulaPurple
import com.onelinediary.app.ui.theme.StarWhite
import com.onelinediary.app.util.DateUtils

@Composable
fun SettingsDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    currentPoolSize: Int,
    lastUpdateDate: String,
    apiKey: String,
    isRefreshing: Boolean,
    onSaveApiKey: (String) -> Unit,
    onTriggerRefresh: () -> Unit,
    onLockApp: () -> Unit
) {
    if (!isOpen) return

    val autoStart = remember { getPlatformAutoStart() }
    var isAutoStartOn by remember { mutableStateOf(autoStart.isAutoStartEnabled()) }

    var isRedPillTaken by remember { mutableStateOf(false) }
    var inputKey by remember(apiKey) { mutableStateOf(apiKey) }
    var saveKeyNotice by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceSurface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isRedPillTaken) "시스템 코어 (빨간약 모드)" else "설정",
                        color = if (isRedPillTaken) Color(0xFFFF5252) else StarWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (isRedPillTaken) {
                        Text(
                            text = "DEVELOPER CORE",
                            color = Color(0xFFFF5252).copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isRedPillTaken)
                        "진실의 코드가 드러났습니다. AI 모델 및 연동 키를 제어합니다."
                    else
                        "오전 6시 갱신 규칙 및 일기장 환경을 관리합니다.",
                    color = StarDim,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- 1. 일기 풀 상태 ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SpaceCardBackground)
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("★ 어딘가의 일기 풀 현황", color = StarGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("$currentPoolSize 개 보관됨", color = StarlightBlue, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "• 현재 일기 기준일: ${DateUtils.getLogicalDiaryDate()}",
                            color = StarWhite.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "• 마지막 갱신 일자: ${lastUpdateDate.ifBlank { "아직 갱신 안 됨" }}",
                            color = StarWhite.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "• 갱신 규칙: 매일 오전 6시 정각에 새로운 1000개의 일기가 도착합니다.",
                            color = StarDim,
                            fontSize = 11.sp
                        )
                    }
                }

                // --- 2. 데스크톱 부팅 시 자동 시작 설정 ---
                if (autoStart.isSupported()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SpaceCardBackground)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "컴퓨터 부팅 시 자동 시작",
                                    color = StarWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "PC가 켜질 때 백그라운드에서 한줄일기를 자동으로 시작합니다.",
                                    color = StarDim.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = isAutoStartOn,
                                onCheckedChange = { checked ->
                                    isAutoStartOn = checked
                                    autoStart.setAutoStartEnabled(checked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = StarlightBlue,
                                    checkedTrackColor = StarlightBlue.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- 3. 즉시 새로고침 버튼 ---
                Button(
                    onClick = onTriggerRefresh,
                    enabled = !isRefreshing,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = StarNebulaPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("1000개 일기 갱신 중...", color = Color.White, fontSize = 13.sp)
                    } else {
                        Text("지금 즉시 1000개 일기 새로고침", color = Color.White, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // --- 4. 앱 잠금 버튼 ---
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onLockApp()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF9E80)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("일기장 즉시 잠그기", fontSize = 13.sp)
                }

                // --- 5. 빨간약 모드 (기술적 API 설정 이스터에그) ---
                AnimatedVisibility(
                    visible = isRedPillTaken,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1F0B10))
                            .border(1.dp, Color(0xFFFF3366).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("매트릭스 코어: API Key & Model", color = Color(0xFFFF5252), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("RED PILL", color = Color(0xFFFF5252), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "평소에는 완전히 숨겨진 생성형 AI(Gemini 1.5 Flash) 백엔드 키입니다. 등록 시 매일 오전 6시 가상 일기 배치 프롬프트를 원격 수신합니다.",
                            color = StarDim.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = inputKey,
                            onValueChange = {
                                inputKey = it
                                saveKeyNotice = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Gemini API Key...", color = StarDim.copy(0.3f), fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF5252),
                                unfocusedBorderColor = Color(0xFFFF5252).copy(alpha = 0.3f),
                                focusedTextColor = StarWhite,
                                unfocusedTextColor = StarWhite
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (saveKeyNotice) {
                                Text("✓ Key Updated", color = Color(0xFFFF5252), fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Button(
                                onClick = {
                                    onSaveApiKey(inputKey)
                                    saveKeyNotice = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3366)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("API Key 저장", color = Color.White, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { isRedPillTaken = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF263238)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("• 파란약 복용 (기술 설정 다시 숨기기)", color = StarlightBlue, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 하단 닫기 버튼 및 은밀한 빨간약 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("닫기", color = StarWhite, fontSize = 13.sp)
                    }

                    if (!isRedPillTaken) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33FF3366))
                                .clickable { isRedPillTaken = true }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text("RED PILL", color = Color(0xFFFF80AB), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
