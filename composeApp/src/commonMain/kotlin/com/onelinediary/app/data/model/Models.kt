package com.onelinediary.app.data.model

import kotlinx.serialization.Serializable

object AppConfig {
    const val APP_NAME = "한줄일기"
    const val VERSION_NAME = "Anima.1aad"
    const val VERSION_CODE = 1
}

@Serializable
data class DiaryEntry(
    val id: String,
    val date: String,          // 기준 날짜 YYYY-MM-DD (오전 6시 기준 일기 날짜)
    val content: String,       // 한 줄(문장) 일기
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class StarDiary(
    val id: String,
    val content: String,       // 우주 어딘가에서 작성한 한 줄 일기
    val universeOrigin: String,// 발신지 (예: "오리온자리 4호 정거장", "안드로메다 궤도 탐사선")
    val date: String,          // 갱신 기준일
    val timestamp: Long
)

@Serializable
data class BackupData(
    val app: String = "OneLineDiary",
    val version: Int = AppConfig.VERSION_CODE,
    val exportedAt: String,
    val totalCount: Int,
    val entries: List<DiaryEntry>
)
