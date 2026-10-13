package com.onelinediary.app.data.notification

/**
 * 플랫폼별 시스템 알림(트레이, 웹 알림, OS 푸시 등)을 발송하는 expect 객체입니다.
 */
expect object PlatformNotification {
    fun showNotification(title: String, message: String)
}
