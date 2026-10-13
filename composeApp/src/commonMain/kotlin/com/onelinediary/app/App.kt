package com.onelinediary.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.onelinediary.app.data.repository.DiaryRepository
import com.onelinediary.app.data.repository.StarDiaryRepository
import com.onelinediary.app.data.security.SecurityRepository
import com.onelinediary.app.ui.screens.AuthLockScreen
import com.onelinediary.app.ui.screens.MainScreen
import com.onelinediary.app.ui.theme.OneLineDiaryTheme
import com.onelinediary.app.util.DateUtils
import kotlinx.coroutines.launch

@Composable
fun App() {
    val coroutineScope = rememberCoroutineScope()
    val securityRepository = remember { SecurityRepository() }
    val diaryRepository = remember { DiaryRepository() }
    val starDiaryRepository = remember { StarDiaryRepository() }

    // 보안 및 잠금 상태
    val isUnlocked by securityRepository.isUnlockedFlow.collectAsState()
    var isPasswordConfigured by remember {
        mutableStateOf(securityRepository.isPasswordConfigured())
    }

    // 일기 목록 상태 (복호화된 평문)
    val entries by diaryRepository.entriesFlow.collectAsState()

    // 붙박이별 (익명 일기) 관련 상태
    val starPool by starDiaryRepository.poolFlow.collectAsState()
    val displayStarDiaries by starDiaryRepository.displayDiariesFlow.collectAsState()
    val isRefreshingStars by starDiaryRepository.isRefreshingFlow.collectAsState()
    val lastUpdateDate by starDiaryRepository.lastUpdateDateFlow.collectAsState()
    val apiKey by starDiaryRepository.apiKeyFlow.collectAsState()

    // 오전 6시 기준 오늘 날짜 계산
    val currentLogicalDate = remember { DateUtils.getLogicalDiaryDate() }

    // 오늘 일기와 지난 일기 분류
    val todayEntry = entries.find { it.date == currentLogicalDate }
    val pastEntries = entries.filter { it.date != currentLogicalDate }

    // 앱 시작 시 오전 6시 기준 1000개 붙박이별 갱신 여부 검사 및 로드
    LaunchedEffect(Unit) {
        starDiaryRepository.checkAndRefreshIfNeeded()
    }

    OneLineDiaryTheme {
        if (!isUnlocked) {
            // 암호 미설정 또는 잠금 상태일 때 보안 인증 화면 표시
            AuthLockScreen(
                isFirstTimeSetup = !isPasswordConfigured,
                onSetupPassword = { password ->
                    val success = securityRepository.setupInitialPassword(password)
                    if (success) {
                        isPasswordConfigured = true
                        diaryRepository.loadEntries(password)
                    }
                    success
                },
                onUnlock = { password ->
                    val success = securityRepository.unlock(password)
                    if (success) {
                        diaryRepository.loadEntries(password)
                    }
                    success
                }
            )
        } else {
            // 잠금 해제 시 메인 일기 화면 표시
            val sessionPass = securityRepository.sessionPassword ?: ""

            MainScreen(
                currentLogicalDate = currentLogicalDate,
                todayEntry = todayEntry,
                pastEntries = pastEntries,
                displayStarDiaries = displayStarDiaries,
                starPool = starPool,
                starPoolSize = starPool.size,
                lastUpdateDate = lastUpdateDate,
                apiKey = apiKey,
                isRefreshingStars = isRefreshingStars,
                onSaveDiary = { date, content ->
                    diaryRepository.saveEntry(date, content, sessionPass)
                },
                onDeleteDiary = { id ->
                    diaryRepository.deleteEntry(id, sessionPass)
                },
                onShuffleStars = {
                    starDiaryRepository.shuffleDisplayDiaries()
                },
                onSaveApiKey = { key ->
                    starDiaryRepository.setApiKey(key)
                },
                onTriggerRefreshStars = {
                    coroutineScope.launch {
                        starDiaryRepository.checkAndRefreshIfNeeded(force = true)
                    }
                },
                onExportBackupJson = {
                    diaryRepository.exportBackupJson()
                },
                onImportBackupJson = { jsonStr, overwrite ->
                    diaryRepository.importBackupJson(jsonStr, sessionPass, overwrite)
                },
                onLockApp = {
                    securityRepository.lock()
                    diaryRepository.clearMemory()
                }
            )
        }
    }
}
