package com.example.myapplication

import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.client.InfluxDBClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class InfluxDBManager {

    companion object {
        private const val TAG = "InfluxDB"
    }

    private val url = "http://100.112.215.22:8086"     // Use the Tailscale IP, NOT 192.168.0.12
    private val token = "XFBK3_Qax-C1GTMMXOtpuXWquWcKUsFGK6MlyRHupX6A6K2fO-oaUtH_8AFni1UWaK-9GPt0TX-HcTi6OCqK8Q=="
    private val org = "Group-3.5"
    private val bucket = "Smart-Stadium"

    private val client: InfluxDBClient by lazy {
        InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket)
    }
    // Single test function
    suspend fun testConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val query = """
                    from(bucket: "$bucket")
                      |> range(start: -1h)
                      |> limit(n: 1)
                """.trimIndent()

                Log.d(TAG, "Testing connection to $url")
                val queryApi = client.queryApi
                val tables = queryApi.query(query, org)

                var hasData = false
                for (table in tables) {
                    for (record in table.records) {
                        hasData = true
                        Log.d(TAG, "Found data: ${record.field} = ${record.value}")
                    }
                }

                hasData
            } catch (e: Exception) {
                Log.e(TAG, "Connection failed", e)
                false
            }
        }
    }

    fun close() {
        client.close()
    }
}