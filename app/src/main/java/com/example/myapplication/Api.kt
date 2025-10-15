package com.example.myapplication

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object Api {
    private const val BASER = "http://100.112.215.22:8002"
    private const val BASEOL = "http://100.112.215.22:8001"
    private val client = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    private suspend fun get(url: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url(url).get().build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use Result.failure(Exception("HTTP ${resp.code}"))
                Result.success(resp.body?.string().orEmpty())
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    // ROOF CONTROL METHODS
    suspend fun openRoof(): Result<String> = get("$BASER/open")
    suspend fun closeRoof(): Result<String> = get("$BASER/close")

    // ADD THESE TWO NEW METHODS for Stop and Resume:
    suspend fun stopRoof(): Result<String> = get("$BASER/stop")
    suspend fun resumeRoof(): Result<String> = get("$BASER/resume")

    // LED PATTERN CONTROL
    suspend fun applyPattern(patterns: List<Int>): Result<String> {
        val n = patterns.joinToString(",")
        val encoded = URLEncoder.encode(n, "UTF-8")
        return get("$BASEOL/hook?n=$encoded")
    }
}