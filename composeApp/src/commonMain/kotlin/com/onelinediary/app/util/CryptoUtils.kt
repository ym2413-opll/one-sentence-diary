package com.onelinediary.app.util

import kotlin.random.Random

/**
 * Kotlin Multiplatform (Android, iOS, Desktop, WasmJs) 전체에서
 * 외부 의존성 없이 100% 동일하게 동작하는 SHA-256 해시 및 대칭키 암호화 유틸리티입니다.
 */
object CryptoUtils {

    // --- 1. SHA-256 표준 해시 구현 (FIPS 180-4) ---

    private val K = intArrayOf(
        0x428a2f98.toInt(), 0x71374491.toInt(), 0xb5c0fbcf.toInt(), 0xe9b5dba5.toInt(),
        0x3956c25b, 0x59f111f1, 0x923f82a4.toInt(), 0xab1c5ed5.toInt(),
        0xd807aa98.toInt(), 0x12835b01, 0x243185be, 0x550c7dc3,
        0x72be5d74, 0x80deb1fe.toInt(), 0x9bdc06a7.toInt(), 0xc19bf174.toInt(),
        0xe49b69c1.toInt(), 0xefbe4786.toInt(), 0x0fc19dc6, 0x240ca1cc,
        0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
        0x983e5152.toInt(), 0xa831c66d.toInt(), 0xb00327c8.toInt(), 0xbf597fc7.toInt(),
        0xc6e00bf3.toInt(), 0xd5a79147.toInt(), 0x06ca6351, 0x14292967,
        0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13,
        0x650a7354, 0x766a0abb, 0x81c2c92e.toInt(), 0x92722c85.toInt(),
        0xa2bfe8a1.toInt(), 0xa81a664b.toInt(), 0xc24b8b70.toInt(), 0xc76c51a3.toInt(),
        0xd192e819.toInt(), 0xd6990624.toInt(), 0xf40e3585.toInt(), 0x106aa070,
        0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5,
        0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
        0x748f82ee, 0x78a5636f, 0x84c87814.toInt(), 0x8cc70208.toInt(),
        0x90befffa.toInt(), 0xa4506ceb.toInt(), 0xbef9a3f7.toInt(), 0xc67178f2.toInt()
    )

    fun sha256(data: ByteArray): ByteArray {
        var h0 = 0x6a09e667
        var h1 = 0xbb67ae85.toInt()
        var h2 = 0x3c6ef372
        var h3 = 0xa54ff53a.toInt()
        var h4 = 0x510e527f
        var h5 = 0x9b05688c.toInt()
        var h6 = 0x1f83d9ab
        var h7 = 0x5be0cd19

        val messageLen = data.size
        val bitLen = messageLen.toLong() * 8

        // 패딩 계산 (512비트 블록)
        val padLen = ((messageLen + 8).inv() and 63) + 1
        val totalLen = messageLen + padLen + 8
        val padded = ByteArray(totalLen)
        data.copyInto(padded)
        padded[messageLen] = 0x80.toByte()

        for (i in 0 until 8) {
            padded[totalLen - 1 - i] = ((bitLen ushr (i * 8)) and 0xFF).toByte()
        }

        val w = IntArray(64)

        var offset = 0
        while (offset < totalLen) {
            for (i in 0 until 16) {
                val j = offset + i * 4
                w[i] = ((padded[j].toInt() and 0xFF) shl 24) or
                        ((padded[j + 1].toInt() and 0xFF) shl 16) or
                        ((padded[j + 2].toInt() and 0xFF) shl 8) or
                        (padded[j + 3].toInt() and 0xFF)
            }
            for (i in 16 until 64) {
                val s0 = (w[i - 15].rotateRight(7)) xor (w[i - 15].rotateRight(18)) xor (w[i - 15] ushr 3)
                val s1 = (w[i - 2].rotateRight(17)) xor (w[i - 2].rotateRight(19)) xor (w[i - 2] ushr 10)
                w[i] = w[i - 16] + s0 + w[i - 7] + s1
            }

            var a = h0
            var b = h1
            var c = h2
            var d = h3
            var e = h4
            var f = h5
            var g = h6
            var h = h7

            for (i in 0 until 64) {
                val s1 = e.rotateRight(6) xor e.rotateRight(11) xor e.rotateRight(25)
                val ch = (e and f) xor (e.inv() and g)
                val temp1 = h + s1 + ch + K[i] + w[i]
                val s0 = a.rotateRight(2) xor a.rotateRight(13) xor a.rotateRight(22)
                val maj = (a and b) xor (a and c) xor (b and c)
                val temp2 = s0 + maj

                h = g
                g = f
                f = e
                e = d + temp1
                d = c
                c = b
                b = a
                a = temp1 + temp2
            }

            h0 += a
            h1 += b
            h2 += c
            h3 += d
            h4 += e
            h5 += f
            h6 += g
            h7 += h

            offset += 64
        }

        val result = ByteArray(32)
        writeInt(result, 0, h0)
        writeInt(result, 4, h1)
        writeInt(result, 8, h2)
        writeInt(result, 12, h3)
        writeInt(result, 16, h4)
        writeInt(result, 20, h5)
        writeInt(result, 24, h6)
        writeInt(result, 28, h7)
        return result
    }

    private fun writeInt(target: ByteArray, offset: Int, value: Int) {
        target[offset] = (value ushr 24).toByte()
        target[offset + 1] = (value ushr 16).toByte()
        target[offset + 2] = (value ushr 8).toByte()
        target[offset + 3] = value.toByte()
    }

    fun hashPassword(password: String, salt: String): String {
        val input = (password + ":" + salt).encodeToByteArray()
        // 1000회 반복 해시 (Key Stretching)
        var hash = sha256(input)
        for (i in 1..1000) {
            hash = sha256(hash + input)
        }
        return toHex(hash)
    }

    fun generateRandomSalt(length: Int = 16): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random.Default
        return (1..length).map { chars[random.nextInt(chars.length)] }.joinToString("")
    }

    // --- 2. 대칭키 스트림 암호화 (SHA-256 CTR 모드 기반) ---

    /**
     * 평문 일기를 비밀번호로 암호화하여 Hex 문자열로 반환합니다.
     * 암호문 포맷: [16글자 Hex IV] + [암호화된 데이터 Hex]
     */
    fun encrypt(plainText: String, password: String): String {
        if (plainText.isEmpty()) return ""
        val plainBytes = plainText.encodeToByteArray()
        
        // 8바이트 (16 hex) 무작위 IV 생성
        val ivBytes = ByteArray(8)
        Random.Default.nextBytes(ivBytes)
        val ivHex = toHex(ivBytes)

        val key = sha256((password + ":" + ivHex).encodeToByteArray())
        val cipherBytes = ByteArray(plainBytes.size)

        var blockCounter = 0
        var byteIndex = 0
        while (byteIndex < plainBytes.size) {
            val streamBlock = sha256(key + ivBytes + blockCounter.toString().encodeToByteArray())
            for (i in 0 until 32) {
                if (byteIndex + i < plainBytes.size) {
                    cipherBytes[byteIndex + i] = (plainBytes[byteIndex + i].toInt() xor streamBlock[i].toInt()).toByte()
                }
            }
            byteIndex += 32
            blockCounter++
        }

        return "ENC:" + ivHex + ":" + toHex(cipherBytes)
    }

    /**
     * 암호화된 일기를 복호화합니다. 복호화 실패 또는 잘못된 비밀번호 시 빈 문자열을 반환하거나 예외를 방지합니다.
     */
    fun decrypt(cipherText: String, password: String): String {
        if (!cipherText.startsWith("ENC:")) {
            // 암호화되지 않은 기존 레거시 데이터는 그대로 반환
            return cipherText
        }

        return try {
            val parts = cipherText.split(":")
            if (parts.size != 3) return ""
            val ivHex = parts[1]
            val ivBytes = fromHex(ivHex)
            val cipherBytes = fromHex(parts[2])

            val key = sha256((password + ":" + ivHex).encodeToByteArray())
            val plainBytes = ByteArray(cipherBytes.size)

            var blockCounter = 0
            var byteIndex = 0
            while (byteIndex < cipherBytes.size) {
                val streamBlock = sha256(key + ivBytes + blockCounter.toString().encodeToByteArray())
                for (i in 0 until 32) {
                    if (byteIndex + i < cipherBytes.size) {
                        plainBytes[byteIndex + i] = (cipherBytes[byteIndex + i].toInt() xor streamBlock[i].toInt()).toByte()
                    }
                }
                byteIndex += 32
                blockCounter++
            }

            plainBytes.decodeToString()
        } catch (_: Exception) {
            ""
        }
    }

    private fun toHex(bytes: ByteArray): String {
        val hexChars = "0123456789abcdef"
        val result = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            val i = b.toInt() and 0xFF
            result.append(hexChars[i ushr 4])
            result.append(hexChars[i and 0x0F])
        }
        return result.toString()
    }

    private fun fromHex(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((hex[i].digitToInt(16) shl 4) + hex[i + 1].digitToInt(16)).toByte()
            i += 2
        }
        return data
    }
}
