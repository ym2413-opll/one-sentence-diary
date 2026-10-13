package com.onelinediary.app.data.autostart

class WasmAutoStart : PlatformAutoStart {
    override fun isSupported(): Boolean = false
    override fun isAutoStartEnabled(): Boolean = false
    override fun setAutoStartEnabled(enabled: Boolean): Boolean = false
}

private val wasmAutoStartInstance by lazy { WasmAutoStart() }

actual fun getPlatformAutoStart(): PlatformAutoStart = wasmAutoStartInstance
