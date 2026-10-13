package com.onelinediary.app.data.storage

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri

internal object AppContextHolder {
    var appContext: Context? = null
}

class AppInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        AppContextHolder.appContext = context?.applicationContext
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}

class AndroidPlatformStorage : PlatformStorage {
    private val prefs: SharedPreferences?
        get() = AppContextHolder.appContext?.getSharedPreferences("oneline_diary_prefs", Context.MODE_PRIVATE)

    override fun getString(key: String, defaultValue: String): String {
        return prefs?.getString(key, defaultValue) ?: defaultValue
    }

    override fun putString(key: String, value: String) {
        prefs?.edit()?.putString(key, value)?.apply()
    }

    override fun remove(key: String) {
        prefs?.edit()?.remove(key)?.apply()
    }
}

private val androidStorageInstance by lazy { AndroidPlatformStorage() }

actual fun getPlatformStorage(): PlatformStorage = androidStorageInstance
