package com.onelinediary.app.data.repository

import com.onelinediary.app.data.model.BackupData
import com.onelinediary.app.data.model.DiaryEntry
import com.onelinediary.app.data.storage.PlatformStorage
import com.onelinediary.app.data.storage.getPlatformStorage
import com.onelinediary.app.util.CryptoUtils
import com.onelinediary.app.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DiaryRepository(
    private val storage: PlatformStorage = getPlatformStorage()
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val KEY_ENTRIES = "user_diary_entries_v2"

    private val _entriesFlow = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val entriesFlow: StateFlow<List<DiaryEntry>> = _entriesFlow.asStateFlow()

    /**
     * 비밀번호를 사용하여 디스크의 암호화된 일기를 복호화하고 메모리에 로드합니다.
     */
    fun loadEntries(password: String) {
        if (password.isBlank()) {
            _entriesFlow.value = emptyList()
            return
        }

        val raw = storage.getString(KEY_ENTRIES, "[]")
        try {
            val rawList = json.decodeFromString<List<DiaryEntry>>(raw)
            val decryptedList = rawList.map { entry ->
                val decryptedContent = CryptoUtils.decrypt(entry.content, password)
                entry.copy(content = decryptedContent)
            }
            _entriesFlow.value = decryptedList.sortedByDescending { it.date }
        } catch (_: Exception) {
            _entriesFlow.value = emptyList()
        }
    }

    /**
     * 앱 잠금 시 메모리의 평문 일기 목록을 안전하게 비웁니다.
     */
    fun clearMemory() {
        _entriesFlow.value = emptyList()
    }

    /**
     * 메모리에는 평문 일기를 유지하고, 디스크에는 비밀번호로 암호화하여 영속화합니다.
     */
    private fun persistEncrypted(plainList: List<DiaryEntry>, password: String) {
        val sortedPlain = plainList.sortedByDescending { it.date }
        _entriesFlow.value = sortedPlain

        // 디스크 저장용 리스트: 모든 content를 암호화
        val encryptedList = sortedPlain.map { entry ->
            val cipherText = CryptoUtils.encrypt(entry.content, password)
            entry.copy(content = cipherText)
        }

        storage.putString(KEY_ENTRIES, json.encodeToString(encryptedList))
    }

    fun getAllEntries(): List<DiaryEntry> = _entriesFlow.value

    fun getEntryForDate(date: String): DiaryEntry? {
        return _entriesFlow.value.find { it.date == date }
    }

    /**
     * 특정 일기 기준일(YYYY-MM-DD)의 한 줄 일기를 저장 또는 수정합니다. (암호화 영속화)
     */
    fun saveEntry(date: String, content: String, password: String): DiaryEntry {
        val trimmed = content.trim()
        val now = Clock.System.now().toEpochMilliseconds()
        val currentList = _entriesFlow.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.date == date }

        val updatedEntry: DiaryEntry
        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            updatedEntry = existing.copy(
                content = trimmed,
                updatedAt = now
            )
            currentList[existingIndex] = updatedEntry
        } else {
            updatedEntry = DiaryEntry(
                id = "diary-$date",
                date = date,
                content = trimmed,
                createdAt = now,
                updatedAt = now
            )
            currentList.add(updatedEntry)
        }

        persistEncrypted(currentList, password)
        return updatedEntry
    }

    fun deleteEntry(id: String, password: String) {
        val currentList = _entriesFlow.value.toMutableList()
        currentList.removeAll { it.id == id }
        persistEncrypted(currentList, password)
    }

    /**
     * 백업용 JSON 문자열을 생성하여 반환합니다.
     */
    fun exportBackupJson(): String {
        val entries = _entriesFlow.value
        val backup = BackupData(
            exportedAt = DateUtils.currentTimestampString(),
            totalCount = entries.size,
            entries = entries
        )
        return json.encodeToString(backup)
    }

    /**
     * 외부 JSON 백업 데이터를 파싱하여 로컬 DB에 복원합니다.
     */
    fun importBackupJson(jsonString: String, password: String, overwrite: Boolean = false): Result<Int> {
        return try {
            val backup = json.decodeFromString<BackupData>(jsonString)
            val newEntries = backup.entries

            val finalList = if (overwrite) {
                newEntries
            } else {
                val map = _entriesFlow.value.associateBy { it.date }.toMutableMap()
                for (entry in newEntries) {
                    val existing = map[entry.date]
                    if (existing == null || entry.updatedAt >= existing.updatedAt) {
                        map[entry.date] = entry
                    }
                }
                map.values.toList()
            }

            persistEncrypted(finalList, password)
            Result.success(finalList.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
