package com.onelinediary.app.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object DateUtils {
    // 오전 6시 기준 하루 오프셋 (6시간 = 21,600,000 밀리초)
    private const val SIX_HOURS_MILLIS = 6 * 60 * 60 * 1000L

    /**
     * 현재 시각의 일기 논리적 날짜(YYYY-MM-DD)를 반환합니다.
     * 오전 6시 이전(00:00~05:59)은 전날의 일기 날짜로 계산됩니다.
     */
    fun getLogicalDiaryDate(
        epochMillis: Long = Clock.System.now().toEpochMilliseconds(),
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        val adjustedMillis = epochMillis - SIX_HOURS_MILLIS
        val instant = Instant.fromEpochMilliseconds(adjustedMillis)
        val localDate = instant.toLocalDateTime(timeZone).date
        return localDate.toString() // YYYY-MM-DD
    }

    /**
     * 현재 시각 기준 포맷팅된 문자열 반환 (예: "2026. 09. 30 (수)")
     */
    fun formatDisplayDate(
        dateString: String,
        isLogicalToday: Boolean = false
    ): String {
        return try {
            val parts = dateString.split("-")
            val year = parts[0]
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            val base = "${year}년 ${month}월 ${day}일"
            if (isLogicalToday) "$base (오늘)" else base
        } catch (_: Exception) {
            dateString
        }
    }

    /**
     * 마지막 갱신 일자와 현재 일자를 비교하여,
     * 오전 6시 기준 새로운 일기 주기(하루 경과)에 도달했는지 확인합니다.
     */
    fun isNewDiaryDay(
        lastUpdatedDate: String,
        currentMillis: Long = Clock.System.now().toEpochMilliseconds()
    ): Boolean {
        if (lastUpdatedDate.isBlank()) return true
        val currentDate = getLogicalDiaryDate(currentMillis)
        return currentDate != lastUpdatedDate
    }

    /**
     * ISO 타임스탬프 문자열 반환
     */
    fun currentTimestampString(): String {
        return Clock.System.now().toString()
    }

    /**
     * 현재 시각의 시간(0~23)을 반환합니다.
     */
    fun getCurrentHour(
        epochMillis: Long = Clock.System.now().toEpochMilliseconds(),
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Int {
        val instant = Instant.fromEpochMilliseconds(epochMillis)
        return instant.toLocalDateTime(timeZone).hour
    }

    /**
     * 늦은 밤(지정된 시간 이후부터 익일 오전 5시 59분까지)인지 확인합니다.
     * 오전 6시는 일기 기준일이 갱신되는 시점입니다.
     */
    fun isLateNightTime(
        reminderStartHour: Int = 21,
        epochMillis: Long = Clock.System.now().toEpochMilliseconds(),
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Boolean {
        val hour = getCurrentHour(epochMillis, timeZone)
        return hour >= reminderStartHour || hour < 6
    }
}
