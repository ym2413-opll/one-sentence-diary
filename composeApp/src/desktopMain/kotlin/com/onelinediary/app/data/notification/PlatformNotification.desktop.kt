package com.onelinediary.app.data.notification

import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.image.BufferedImage
import kotlin.concurrent.thread

actual object PlatformNotification {
    actual fun showNotification(title: String, message: String) {
        try {
            if (SystemTray.isSupported()) {
                val tray = SystemTray.getSystemTray()
                // 16x16 투명/심플 기본 이미지 생성
                val image = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
                val trayIcon = TrayIcon(image, "한줄일기")
                trayIcon.isImageAutoSize = true
                tray.add(trayIcon)
                trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO)
                
                // 6초 후 트레이에서 자동 제거
                thread(isDaemon = true) {
                    Thread.sleep(6000)
                    try {
                        tray.remove(trayIcon)
                    } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {
            // 데스크톱 환경 헤드리스 또는 권한 미지원 시 안전하게 무시
        }
    }
}
