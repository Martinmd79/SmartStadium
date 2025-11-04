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

    private val url = "http://100.112.215.22:8086"     // Use the Tailscale IP
    private val token = "XFBK3_Qax-C1GTMMXOtpuXWquWcKUsFGK6MlyRHupX6A6K2fO-oaUtH_8AFni1UWaK-9GPt0TX-HcTi6OCqK8Q=="
    private val org = "ENG30002-Group-3.5"
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
                      |> range(start: -7d)
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

    suspend fun latestFields(measurement: String, fields: List<String>): Map<String, Double> =
        withContext(Dispatchers.IO) {
            val flux = """
            import "influxdata/influxdb/schema"
            from(bucket: "$bucket")
              |> range(start: -30m)
              |> filter(fn: (r) => r._measurement == "$measurement" and contains(value: r._field, set: ${fields.joinToString(prefix="[\"", separator="\",\"", postfix="\"]")}))
              |> last()
        """.trimIndent()
            val out = mutableMapOf<String, Double>()
            try {
                client.queryApi.query(flux, org).forEach { table ->
                    table.records.forEach { rec ->
                        val field = rec.field
                        val value = (rec.value as? Number)?.toDouble()
                        if (field != null && value != null) out[field] = value
                    }
                }
            } catch (_: Exception) {}
            out
        }



    fun close() {
        client.close()
    }
}