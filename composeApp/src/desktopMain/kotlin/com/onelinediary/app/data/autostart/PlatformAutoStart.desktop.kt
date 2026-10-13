package com.onelinediary.app.data.autostart

import com.onelinediary.app.data.storage.getPlatformStorage
import java.io.File

class DesktopAutoStart : PlatformAutoStart {
    private val storage = getPlatformStorage()
    private val PREF_KEY = "desktop_autostart_enabled"
    private val REG_KEY = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run"
    private val APP_NAME = "OneLineDiary"

    private val isWindows: Boolean
        get() = System.getProperty("os.name")?.lowercase()?.contains("win") == true

    override fun isSupported(): Boolean = isWindows

    override fun isAutoStartEnabled(): Boolean {
        if (!isWindows) return false

        // 1. 레지스트리 쿼리 시도
        try {
            val process = ProcessBuilder("reg", "query", REG_KEY, "/v", APP_NAME)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            if (process.exitValue() == 0 && output.contains(APP_NAME)) {
                return true
            }
        } catch (_: Exception) {}

        // 2. 저장된 설정 폴백
        return storage.getString(PREF_KEY, "false") == "true"
    }

    override fun setAutoStartEnabled(enabled: Boolean): Boolean {
        if (!isWindows) return false

        storage.putString(PREF_KEY, enabled.toString())

        return try {
            if (enabled) {
                // 현재 실행 경로를 파악하여 레지스트리에 등록
                val javaHome = System.getProperty("java.home")
                val javaw = File(javaHome, "bin/javaw.exe").absolutePath
                val classPath = System.getProperty("java.class.path") ?: ""

                val cmd = if (classPath.endsWith(".jar") && !classPath.contains(";")) {
                    "\"$javaw\" -jar \"$classPath\""
                } else {
                    // 기본 프로세스 실행 커맨드
                    "\"$javaw\" -cp \"$classPath\" com.onelinediary.app.MainKt"
                }

                val process = ProcessBuilder(
                    "reg", "add", REG_KEY, "/v", APP_NAME, "/t", "REG_SZ", "/d", cmd, "/f"
                ).redirectErrorStream(true).start()
                process.waitFor() == 0
            } else {
                val process = ProcessBuilder(
                    "reg", "delete", REG_KEY, "/v", APP_NAME, "/f"
                ).redirectErrorStream(true).start()
                process.waitFor()
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}

private val desktopAutoStartInstance by lazy { DesktopAutoStart() }

actual fun getPlatformAutoStart(): PlatformAutoStart = desktopAutoStartInstance
