package com.onelinediary.app.data.security

import com.onelinediary.app.data.storage.PlatformStorage
import com.onelinediary.app.data.storage.getPlatformStorage
import com.onelinediary.app.util.CryptoUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecurityRepository(
    private val storage: PlatformStorage = getPlatformStorage()
) {
    private val KEY_SALT = "app_auth_salt_v1"
    private val KEY_HASH = "app_auth_hash_v1"

    // 현재 세션의 마스터 비밀번호 (잠금 해제 시 메모리에만 유지되며 파일에 저장되지 않음)
    private var _sessionPassword: String? = null
    val sessionPassword: String? get() = _sessionPassword

    // 잠금 상태 Flow
    private val _isUnlockedFlow = MutableStateFlow(false)
    val isUnlockedFlow: StateFlow<Boolean> = _isUnlockedFlow.asStateFlow()

    fun isPasswordConfigured(): Boolean {
        val hash = storage.getString(KEY_HASH, "")
        val salt = storage.getString(KEY_SALT, "")
        return hash.isNotBlank() && salt.isNotBlank()
    }

    /**
     * 최초 실행 시 비밀번호와 랜덤 솔트를 해시하여 저장소에 보관합니다.
     */
    fun setupInitialPassword(password: String): Boolean {
        if (password.length < 4) return false
        val salt = CryptoUtils.generateRandomSalt(16)
        val hash = CryptoUtils.hashPassword(password, salt)

        storage.putString(KEY_SALT, salt)
        storage.putString(KEY_HASH, hash)

        _sessionPassword = password
        _isUnlockedFlow.value = true
        return true
    }

    /**
     * 입력된 비밀번호를 검증하고 세션을 잠금 해제합니다.
     */
    fun unlock(password: String): Boolean {
        val salt = storage.getString(KEY_SALT, "")
        val storedHash = storage.getString(KEY_HASH, "")
        if (salt.isBlank() || storedHash.isBlank()) return false

        val computedHash = CryptoUtils.hashPassword(password, salt)
        return if (computedHash == storedHash) {
            _sessionPassword = password
            _isUnlockedFlow.value = true
            true
        } else {
            false
        }
    }

    /**
     * 앱을 즉시 잠급니다. (메모리의 세션 비밀번호 초기화)
     */
    fun lock() {
        _sessionPassword = null
        _isUnlockedFlow.value = false
    }

    /**
     * 비밀번호 변경
     */
    fun changePassword(oldPass: String, newPass: String): Boolean {
        if (!unlock(oldPass)) return false
        if (newPass.length < 4) return false
        return setupInitialPassword(newPass)
    }
}
