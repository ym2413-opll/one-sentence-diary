package com.onelinediary.app.data.notification

import com.onelinediary.app.data.storage.PlatformStorage
import com.onelinediary.app.data.storage.getPlatformStorage
import com.onelinediary.app.util.DateUtils

/**
 * 늦은 시간 일기 작성 여부를 감지하고 알림을 관리하는 객체입니다.
 */
object ReminderManager {
    private const val KEY_REMINDER_ENABLED = "setting_reminder_enabled"
    private const val KEY_REMINDER_HOUR = "setting_reminder_hour"
    private const val KEY_LAST_REMINDED_DATE = "setting_last_reminded_date"

    private val storage: PlatformStorage by lazy { getPlatformStorage() }

    fun isReminderEnabled(): Boolean {
        val value = storage.getString(KEY_REMINDER_ENABLED, "true")
        return value != "false"
    }

    fun setReminderEnabled(enabled: Boolean) {
        storage.putString(KEY_REMINDER_ENABLED, enabled.toString())
    }

    fun getReminderHour(): Int {
        val value = storage.getString(KEY_REMINDER_HOUR, "21")
        return value.toIntOrNull() ?: 21
    }

    fun setReminderHour(hour: Int) {
        storage.putString(KEY_REMINDER_HOUR, hour.toString())
    }

    /**
     * 오늘 일기를 아직 쓰지 않았고 늦은 밤 시간이 된 경우,
     * 당일 1회 한정으로 시스템 알림을 발송합니다.
     */
    fun checkAndTriggerNotification(isTodayDiaryEmpty: Boolean): Boolean {
        if (!isReminderEnabled()) return false
        if (!isTodayDiaryEmpty) return false

        val reminderHour = getReminderHour()
        if (!DateUtils.isLateNightTime(reminderHour)) return false

        val todayLogicalDate = DateUtils.getLogicalDiaryDate()
        val lastRemindedDate = storage.getString(KEY_LAST_REMINDED_DATE, "")

        if (lastRemindedDate != todayLogicalDate) {
            storage.putString(KEY_LAST_REMINDED_DATE, todayLogicalDate)
            PlatformNotification.showNotification(
                title = "한줄일기",
                message = "오늘 밤이 지나가기 전에 당신의 소중한 하루를 한 줄로 기록해보세요 ✦"
            )
            return true
        }
        return false
    }

    /**
     * 알림 동작을 확인할 수 있는 즉시 테스트 발송 함수입니다.
     */
    fun triggerTestNotification() {
        PlatformNotification.showNotification(
            title = "한줄일기 (알림 테스트)",
            message = "오늘 밤이 지나가기 전에 당신의 소중한 하루를 한 줄로 기록해보세요 ✦"
        )
    }
}
