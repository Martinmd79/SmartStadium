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

    // Get these from your teammate
    private val token = "PASTE_TOKEN_HERE"
    private val org = "PASTE_ORG_HERE"
    private val bucket = "PASTE_BUCKET_HERE"
    private val url = "http://PASTE_TAILSCALE_IP_HERE:8086"

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