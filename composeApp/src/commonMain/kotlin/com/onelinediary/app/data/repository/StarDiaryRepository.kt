package com.onelinediary.app.data.repository

import com.onelinediary.app.data.api.StarDiaryApiClient
import com.onelinediary.app.data.generator.AnonymousDiaryGenerator
import com.onelinediary.app.data.model.StarDiary
import com.onelinediary.app.data.storage.PlatformStorage
import com.onelinediary.app.data.storage.getPlatformStorage
import com.onelinediary.app.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

class StarDiaryRepository(
    private val storage: PlatformStorage = getPlatformStorage(),
    private val apiClient: StarDiaryApiClient = StarDiaryApiClient()
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val KEY_STAR_POOL = "star_diaries_pool_v1"
    private val KEY_LAST_UPDATE_DATE = "star_diaries_last_update_date"
    private val KEY_API_KEY = "gemini_api_key"

    // 캐시된 1000개의 우주 일기 전체 풀
    private val _poolFlow = MutableStateFlow<List<StarDiary>>(emptyList())
    val poolFlow: StateFlow<List<StarDiary>> = _poolFlow.asStateFlow()

    // 화면 배경에 무작위로 은은히 표시될 활성 별빛 일기 목록 (6개 내외)
    private val _displayDiariesFlow = MutableStateFlow<List<StarDiary>>(emptyList())
    val displayDiariesFlow: StateFlow<List<StarDiary>> = _displayDiariesFlow.asStateFlow()

    // 갱신 중 여부
    private val _isRefreshingFlow = MutableStateFlow(false)
    val isRefreshingFlow: StateFlow<Boolean> = _isRefreshingFlow.asStateFlow()

    // 마지막 갱신 일자 (오전 6시 기준 일기 날짜)
    private val _lastUpdateDateFlow = MutableStateFlow("")
    val lastUpdateDateFlow: StateFlow<String> = _lastUpdateDateFlow.asStateFlow()

    // AI API 키
    private val _apiKeyFlow = MutableStateFlow("")
    val apiKeyFlow: StateFlow<String> = _apiKeyFlow.asStateFlow()

    init {
        _lastUpdateDateFlow.value = storage.getString(KEY_LAST_UPDATE_DATE, "")
        _apiKeyFlow.value = storage.getString(KEY_API_KEY, "")
        loadCachedPool()
    }

    private fun loadCachedPool() {
        val raw = storage.getString(KEY_STAR_POOL, "")
        if (raw.isNotBlank()) {
            try {
                val list = json.decodeFromString<List<StarDiary>>(raw)
                _poolFlow.value = list
                shuffleDisplayDiaries()
            } catch (_: Exception) {
                _poolFlow.value = emptyList()
            }
        }
    }

    fun setApiKey(apiKey: String) {
        val trimmed = apiKey.trim()
        _apiKeyFlow.value = trimmed
        storage.putString(KEY_API_KEY, trimmed)
    }

    /**
     * 배경에 띄울 5~7개의 우주 일기를 1000개 풀에서 무작위로 샘플링하여 갱신합니다.
     */
    fun shuffleDisplayDiaries() {
        val pool = _poolFlow.value
        if (pool.isNotEmpty()) {
            val count = minOf(6, pool.size)
            _displayDiariesFlow.value = pool.shuffled().take(count)
        }
    }

    /**
     * 오전 6시 기준 일기 날짜를 검사하여 하루 1회 갱신을 실행합니다.
     * force가 true이면 강제로 즉시 갱신합니다.
     */
    suspend fun checkAndRefreshIfNeeded(force: Boolean = false) {
        val currentDate = DateUtils.getLogicalDiaryDate()
        val lastDate = _lastUpdateDateFlow.value

        val isNeedUpdate = force || _poolFlow.value.isEmpty() || DateUtils.isNewDiaryDay(lastDate)

        if (!isNeedUpdate) {
            if (_displayDiariesFlow.value.isEmpty()) {
                shuffleDisplayDiaries()
            }
            return
        }

        _isRefreshingFlow.value = true
        try {
            val apiKey = _apiKeyFlow.value
            var aiLines: List<String> = emptyList()

            // 1. 만약 사용자가 API 키를 설정했다면, 하루 1회 Gemini API를 호출하여 AI 일기 생성
            if (apiKey.isNotBlank()) {
                try {
                    aiLines = apiClient.fetchAiStarDiaries(apiKey, currentDate)
                } catch (_: Throwable) {
                    // API 호출 실패 시에도 안정적으로 로컬 생성기로 진행
                }
            }

            // 2. 날짜 시드 기반 1000개의 익명 일기 생성 (서버 부하 0, 항상 보장)
            val generatedPool = AnonymousDiaryGenerator.generate1000Diaries(currentDate).toMutableList()

            // 3. 만약 새로 수신된 문장이 있다면, 1000개 풀의 상위 위치에 블렌딩
            if (aiLines.isNotEmpty()) {
                for (i in aiLines.indices) {
                    if (i < generatedPool.size) {
                        generatedPool[i] = generatedPool[i].copy(
                            content = aiLines[i],
                            universeOrigin = "어딘가에서 온 익명의 편지"
                        )
                    }
                }
            }

            // 4. 로컬 캐시 영속화 및 상태 갱신
            val finalPool = generatedPool.toList()
            _poolFlow.value = finalPool
            _lastUpdateDateFlow.value = currentDate

            storage.putString(KEY_LAST_UPDATE_DATE, currentDate)
            storage.putString(KEY_STAR_POOL, json.encodeToString(finalPool))

            shuffleDisplayDiaries()
        } finally {
            _isRefreshingFlow.value = false
        }
    }
}
