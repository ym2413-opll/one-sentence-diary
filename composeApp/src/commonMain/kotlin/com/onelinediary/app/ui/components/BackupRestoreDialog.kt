package com.onelinediary.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.onelinediary.app.ui.theme.SpaceCardBackground
import com.onelinediary.app.ui.theme.SpaceSurface
import com.onelinediary.app.ui.theme.StarDim
import com.onelinediary.app.ui.theme.StarGold
import com.onelinediary.app.ui.theme.StarlightBlue
import com.onelinediary.app.ui.theme.StarWhite

@Composable
fun BackupRestoreDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onExportJson: () -> String,
    onImportJson: (jsonString: String, overwrite: Boolean) -> Result<Int>
) {
    if (!isOpen) return

    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: 백업(내보내기), 1: 복원(불러오기)

    var exportJsonString by remember { mutableStateOf("") }
    var importInputText by remember { mutableStateOf("") }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }
    var isOverwriteMode by remember { mutableStateOf(false) }
    var copyNotice by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 620.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceSurface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "일기 백업과 복원",
                    color = StarWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "일기 데이터를 안전하게 내보내거나 가져옵니다.",
                    color = StarDim,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SpaceCardBackground,
                    contentColor = StarlightBlue
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            exportJsonString = onExportJson()
                            copyNotice = false
                        },
                        text = { Text("내보내기 (Backup)", fontSize = 13.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            importStatusMessage = null
                        },
                        text = { Text("불러오기 (Restore)", fontSize = 13.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // --- 백업 (내보내기) 섹션 ---
                    if (exportJsonString.isEmpty()) {
                        exportJsonString = onExportJson()
                    }

                    Text(
                        text = "백업 데이터 (JSON):",
                        color = StarWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SpaceCardBackground)
                            .border(1.dp, StarlightBlue.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = exportJsonString,
                            color = StarDim,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(exportJsonString))
                            copyNotice = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = StarlightBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (copyNotice) "✓ 클립보드에 복사되었습니다!" else "클립보드로 전체 복사",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    // --- 복원 (불러오기) 섹션 ---
                    Text(
                        text = "백업된 JSON 문자열 붙여넣기:",
                        color = StarWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = importInputText,
                        onValueChange = { importInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        placeholder = { Text("이곳에 백업 JSON 코드를 붙여넣으세요...", color = StarDim.copy(0.5f), fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StarlightBlue,
                            unfocusedBorderColor = StarlightBlue.copy(alpha = 0.3f),
                            focusedTextColor = StarWhite,
                            unfocusedTextColor = StarWhite
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOverwriteMode) "모드: 덮어쓰기 (기존 삭제)" else "모드: 날짜별 병합 (기존 유지)",
                            color = if (isOverwriteMode) Color(0xFFFF9E80) else StarGold,
                            fontSize = 12.sp
                        )
                        OutlinedButton(
                            onClick = { isOverwriteMode = !isOverwriteMode },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "모드 전환", fontSize = 11.sp, color = StarWhite)
                        }
                    }

                    if (importStatusMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = importStatusMessage ?: "",
                            color = if (importStatusMessage?.startsWith("✓") == true) StarlightBlue else Color(0xFFFF6E6E),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val result = onImportJson(importInputText.trim(), isOverwriteMode)
                            result.onSuccess { count ->
                                importStatusMessage = "✓ 성공적으로 ${count}개의 일기를 복원했습니다."
                                importInputText = ""
                            }.onFailure { err ->
                                importStatusMessage = "× 복원 실패: 잘못된 백업 형식이거나 오류가 있습니다."
                            }
                        },
                        enabled = importInputText.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = StarGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "일기 복원 실행",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("닫기", color = StarWhite, fontSize = 13.sp)
                }
            }
        }
    }
}
