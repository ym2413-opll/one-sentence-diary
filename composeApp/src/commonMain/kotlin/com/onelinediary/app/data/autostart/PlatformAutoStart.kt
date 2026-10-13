package com.onelinediary.app.data.autostart

interface PlatformAutoStart {
    fun isSupported(): Boolean
    fun isAutoStartEnabled(): Boolean
    fun setAutoStartEnabled(enabled: Boolean): Boolean
}

expect fun getPlatformAutoStart(): PlatformAutoStart
