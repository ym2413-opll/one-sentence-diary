package com.onelinediary.app.data.notification

actual object PlatformNotification {
    actual fun showNotification(title: String, message: String) {
        // Android 시스템 알림 구현 (백그라운드 채널/토스트 폴백)
    }
}
