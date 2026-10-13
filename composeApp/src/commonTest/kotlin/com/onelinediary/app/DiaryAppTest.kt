package com.onelinediary.app

import com.onelinediary.app.data.generator.AnonymousDiaryGenerator
import com.onelinediary.app.data.repository.DiaryRepository
import com.onelinediary.app.data.security.SecurityRepository
import com.onelinediary.app.data.storage.PlatformStorage
import com.onelinediary.app.util.CryptoUtils
import com.onelinediary.app.util.DateUtils
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InMemoryPlatformStorage : PlatformStorage {
    val map = mutableMapOf<String, String>()

    override fun getString(key: String, defaultValue: String): String = map[key] ?: defaultValue
    override fun putString(key: String, value: String) { map[key] = value }
    override fun remove(key: String) { map.remove(key) }
}

class DiaryAppTest {

    @Test
    fun testLogicalDiaryDateBoundary() {
        val kst = TimeZone.of("Asia/Seoul")
        val before6am = Instant.parse("2026-09-29T20:59:00Z").toEpochMilliseconds() // 2026-09-30 05:59 KST
        val after6am = Instant.parse("2026-09-29T21:01:00Z").toEpochMilliseconds()  // 2026-09-30 06:01 KST

        val diaryDateBefore = DateUtils.getLogicalDiaryDate(before6am, kst)
        val diaryDateAfter = DateUtils.getLogicalDiaryDate(after6am, kst)

        assertEquals("2026-09-29", diaryDateBefore)
        assertEquals("2026-09-30", diaryDateAfter)
        assertTrue(DateUtils.isNewDiaryDay("2026-09-29", after6am))
        assertFalse(DateUtils.isNewDiaryDay("2026-09-29", before6am))
    }

    @Test
    fun testAnonymousDiaryGenerator1000Items() {
        val testDate = "2026-09-30"
        val diaries = AnonymousDiaryGenerator.generate1000Diaries(testDate)

        // 1. 정확히 1000개 생성
        assertEquals(1000, diaries.size)

        // 2. 발신지와 현실적 일기 문장 검증 (우주 SF가 아닌 현실적 일상 공간 확인)
        diaries.take(20).forEach { diary ->
            assertTrue(diary.content.isNotBlank())
            assertTrue(diary.universeOrigin.isNotBlank())
            // AI 티를 내지 않는 자연스러운 발신지
            assertFalse(diary.universeOrigin.contains("AI"))
            assertFalse(diary.universeOrigin.contains("Gemini"))
        }

        // 3. 날짜별 고유성
        val nextDayDiaries = AnonymousDiaryGenerator.generate1000Diaries("2026-10-01")
        assertNotEquals(diaries[0].content, nextDayDiaries[0].content)
    }

    @Test
    fun testCryptoUtilsHashAndEncryption() {
        val password = "mySecretPassword123"
        val salt = CryptoUtils.generateRandomSalt(16)
        val hash1 = CryptoUtils.hashPassword(password, salt)
        val hash2 = CryptoUtils.hashPassword(password, salt)

        // 해시 결정론성 검증
        assertEquals(hash1, hash2)
        assertNotEquals(password, hash1)

        // 대칭키 암호화 및 복호화 검증
        val plainText = "오늘 하루는 정말 힘들었지만 버텨냈다."
        val cipherText = CryptoUtils.encrypt(plainText, password)

        assertTrue(cipherText.startsWith("ENC:"))
        assertNotEquals(plainText, cipherText)

        val decrypted = CryptoUtils.decrypt(cipherText, password)
        assertEquals(plainText, decrypted)

        // 틀린 비밀번호로 복호화 시 원문 불일치
        val wrongDecrypted = CryptoUtils.decrypt(cipherText, "wrongPassword")
        assertNotEquals(plainText, wrongDecrypted)
    }

    @Test
    fun testEncryptedDiaryStorageSecurity() {
        val storage = InMemoryPlatformStorage()
        val repo = DiaryRepository(storage)
        val password = "testPassword@2026"

        // 1. 일기 저장
        val originalText = "나만의 비밀스러운 속마음 일기."
        repo.saveEntry("2026-09-30", originalText, password)

        // 2. 물리 저장소 검증: 저장소에 평문이 전혀 없고 암호문(ENC:)으로 저장되어 있는지 검증!
        val rawStoredJson = storage.getString("user_diary_entries_v2", "")
        assertTrue(rawStoredJson.contains("ENC:"))
        assertFalse(rawStoredJson.contains(originalText), "저장소에 원본 평문이 노출되면 안 됩니다!")

        // 3. 메모리 클리어 후 재로드 테스트
        repo.clearMemory()
        assertEquals(0, repo.getAllEntries().size)

        // 올바른 비밀번호로 복호화 로드
        repo.loadEntries(password)
        val loaded = repo.getEntryForDate("2026-09-30")
        assertNotNull(loaded)
        assertEquals(originalText, loaded.content)

        // 백업 및 복원
        val exportedJson = repo.exportBackupJson()
        assertTrue(exportedJson.contains(originalText))

        val storage2 = InMemoryPlatformStorage()
        val repo2 = DiaryRepository(storage2)
        val result = repo2.importBackupJson(exportedJson, password)
        assertTrue(result.isSuccess)
        assertEquals(1, repo2.getAllEntries().size)
    }

    @Test
    fun testSecurityRepositoryFlow() {
        val storage = InMemoryPlatformStorage()
        val security = SecurityRepository(storage)

        assertFalse(security.isPasswordConfigured())
        assertFalse(security.isUnlockedFlow.value)

        // 최초 비밀번호 설정
        val setupSuccess = security.setupInitialPassword("safePass99")
        assertTrue(setupSuccess)
        assertTrue(security.isPasswordConfigured())
        assertTrue(security.isUnlockedFlow.value)

        // 잠금
        security.lock()
        assertFalse(security.isUnlockedFlow.value)

        // 틀린 비밀번호 잠금 해제 시도
        val failUnlock = security.unlock("wrongPass")
        assertFalse(failUnlock)
        assertFalse(security.isUnlockedFlow.value)

        // 올바른 비밀번호 잠금 해제
        val okUnlock = security.unlock("safePass99")
        assertTrue(okUnlock)
        assertTrue(security.isUnlockedFlow.value)
    }
}
