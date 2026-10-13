package com.onelinediary.app.data.autostart

class AndroidAutoStart : PlatformAutoStart {
    override fun isSupported(): Boolean = false
    override fun isAutoStartEnabled(): Boolean = false
    override fun setAutoStartEnabled(enabled: Boolean): Boolean = false
}

private val androidAutoStartInstance by lazy { AndroidAutoStart() }

actual fun getPlatformAutoStart(): PlatformAutoStart = androidAutoStartInstance
