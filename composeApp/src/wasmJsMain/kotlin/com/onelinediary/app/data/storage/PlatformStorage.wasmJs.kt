package com.onelinediary.app.data.storage

import kotlinx.browser.localStorage

class WasmPlatformStorage : PlatformStorage {
    override fun getString(key: String, defaultValue: String): String {
        return try {
            localStorage.getItem(key) ?: defaultValue
        } catch (e: Throwable) {
            defaultValue
        }
    }

    override fun putString(key: String, value: String) {
        try {
            localStorage.setItem(key, value)
        } catch (_: Throwable) {
        }
    }

    override fun remove(key: String) {
        try {
            localStorage.removeItem(key)
        } catch (_: Throwable) {
        }
    }
}

private val wasmStorageInstance by lazy { WasmPlatformStorage() }

actual fun getPlatformStorage(): PlatformStorage = wasmStorageInstance
