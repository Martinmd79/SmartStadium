package com.example.myapplication

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit

object Api {
    private const val BASER = "http://100.112.215.22:8002" // roof FastAPI
    private const val BASEOL = "http://100.112.215.22:8001" // lighting FastAPI
    private const val BASEML = "http://100.112.215.22:8003" // MAIN stadium lights  ✅
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
        } catch (t: Throwable) { Result.failure(t) }
    }

    // NEW: POST helper with empty body
    private suspend fun post(url: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url(url)
                .post(RequestBody.create(null, ByteArray(0)))
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use Result.failure(Exception("HTTP ${resp.code}"))
                Result.success(resp.body?.string().orEmpty())
            }
        } catch (t: Throwable) { Result.failure(t) }
    }

    // ROOF CONTROL (match your curl: all POST)
    suspend fun openRoof(): Result<String>   = post("$BASER/open")
    suspend fun closeRoof(): Result<String>  = post("$BASER/close")
    suspend fun stopRoof(): Result<String>   = post("$BASER/stop")
    suspend fun resumeRoof(): Result<String> = post("$BASER/resume")

    // LED PATTERN CONTROL (raw commas, no URL-encoding)
    suspend fun applyPattern(patterns: List<Int>): Result<String> {
        val n = patterns.joinToString(",")                    // e.g., 14,4,5
        return get("$BASEOL/hook?n=$n")                       // GET as your hook expects
    }
    // ✅ Main lights: send 1 (off) or 2 (on) to :8003
    suspend fun setMainLights(isOn: Boolean): Result<String> {
        val n = if (isOn) 2 else 1
        return get("$BASEML/hook?n=$n")
    }
}
