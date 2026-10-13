package com.onelinediary.app.data.storage

/**
 * 플랫폼 독립적인 로컬 키-값 및 텍스트 데이터 저장소 인터페이스
 * Android, iOS, Desktop, Web(Wasm) 모두에서 동작할 수 있도록 추상화합니다.
 */
interface PlatformStorage {
    fun getString(key: String, defaultValue: String = ""): String
    fun putString(key: String, value: String)
    fun remove(key: String)
}

expect fun getPlatformStorage(): PlatformStorage
