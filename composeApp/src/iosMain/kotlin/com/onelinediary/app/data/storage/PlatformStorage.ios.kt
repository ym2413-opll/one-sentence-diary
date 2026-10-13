package com.onelinediary.app.data.storage

import platform.Foundation.NSUserDefaults

class IosPlatformStorage : PlatformStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override fun getString(key: String, defaultValue: String): String {
        return userDefaults.stringForKey(key) ?: defaultValue
    }

    override fun putString(key: String, value: String) {
        userDefaults.setObject(value, forKey = key)
    }

    override fun remove(key: String) {
        userDefaults.removeObjectForKey(key)
    }
}

private val iosStorageInstance by lazy { IosPlatformStorage() }

actual fun getPlatformStorage(): PlatformStorage = iosStorageInstance
