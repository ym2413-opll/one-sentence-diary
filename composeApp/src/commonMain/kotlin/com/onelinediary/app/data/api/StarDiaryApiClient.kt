package com.onelinediary.app.data.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

class StarDiaryApiClient {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    /**
     * Gemini 생성형 AI API를 호출하여 새로운 우주 일기 테마 문장들을 가져옵니다.
     * 하루 1회만 호출하여 서버 부하 및 비용을 최소화합니다.
     */
    suspend fun fetchAiStarDiaries(apiKey: String, dateString: String): List<String> {
        if (apiKey.isBlank()) return emptyList()

        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        val prompt = """
            현실 속 보통 사람들의 진솔한 고민과 감정, 피로, 위로가 담긴 한 문장 일기를 한국어로 15줄 작성해주세요.
            퇴근길, 늦은 밤 침대 위, 비 내리는 버스 정류장 등에서 느끼는 현실적인 감정이어야 합니다.
            한 줄에 하나씩, 번호나 기호 없이 문장만 출력하세요.
            예시:
            - 오늘도 괜찮은 척 웃느라 정작 나 자신을 안아주지 못했다.
            - 퇴근길 붕어빵 냄새에 문득 어릴 적 생각이 나서 조금 울컥했다.
        """.trimIndent()

        return try {
            val response = httpClient.post(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(
                    GeminiRequest(
                        contents = listOf(
                            GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                        )
                    )
                )
            }

            val bodyText = response.bodyAsText()
            val parsed = json.decodeFromString<GeminiResponse>(bodyText)
            val fullText = parsed.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text.orEmpty()

            fullText.lines()
                .map { it.trim().removePrefix("-").removePrefix("•").trim() }
                .filter { it.isNotBlank() && it.length >= 5 }
        } catch (_: Throwable) {
            emptyList()
        }
    }
}
