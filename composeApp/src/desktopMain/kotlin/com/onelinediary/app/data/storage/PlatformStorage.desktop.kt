package com.onelinediary.app.data.storage

import java.io.File
import java.util.Properties

class DesktopPlatformStorage : PlatformStorage {
    private val storageDir = File(System.getProperty("user.home"), ".onelinediary").apply { mkdirs() }
    private val storageFile = File(storageDir, "app_storage.properties")
    private val properties = Properties()

    init {
        if (storageFile.exists()) {
            storageFile.inputStream().use { properties.load(it) }
        }
    }

    override fun getString(key: String, defaultValue: String): String {
        return properties.getProperty(key, defaultValue)
    }

    override fun putString(key: String, value: String) {
        properties.setProperty(key, value)
        storageFile.outputStream().use { properties.store(it, "OneLineDiary App Storage") }
    }

    override fun remove(key: String) {
        properties.remove(key)
        storageFile.outputStream().use { properties.store(it, "OneLineDiary App Storage") }
    }
}

private val desktopStorageInstance by lazy { DesktopPlatformStorage() }

actual fun getPlatformStorage(): PlatformStorage = desktopStorageInstance
