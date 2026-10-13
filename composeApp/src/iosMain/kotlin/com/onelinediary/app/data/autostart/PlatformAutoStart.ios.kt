package com.onelinediary.app.data.autostart

class IosAutoStart : PlatformAutoStart {
    override fun isSupported(): Boolean = false
    override fun isAutoStartEnabled(): Boolean = false
    override fun setAutoStartEnabled(enabled: Boolean): Boolean = false
}

private val iosAutoStartInstance by lazy { IosAutoStart() }

actual fun getPlatformAutoStart(): PlatformAutoStart = iosAutoStartInstance
