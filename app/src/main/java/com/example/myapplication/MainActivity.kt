package com.example.myapplication

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var cardPowerManagement: CardView
    private lateinit var cardMechanicalRoof: CardView
    private lateinit var cardLightingControl: CardView
    private lateinit var cardFieldSensors: CardView

    private val influxDB = InfluxDBManager()

    // Sensor data storage
    private var sensorData = mutableMapOf<String, Double>()

    // Roof state enum
    private enum class RoofState {
        OPEN, CLOSED, OPENING, CLOSING, STOPPED
    }

    // Roof state variables
    private var roofState = RoofState.OPEN
    private var targetRoofState = RoofState.OPEN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find the cards
        cardPowerManagement = findViewById(R.id.cardPowerManagement)
        cardMechanicalRoof = findViewById(R.id.cardMechanicalRoof)
        cardLightingControl = findViewById(R.id.cardLightingControl)
        cardFieldSensors = findViewById(R.id.cardFieldSensors)

        // Set click listeners
        cardPowerManagement.setOnClickListener {
            showPowerManagementDialog()
        }

        cardMechanicalRoof.setOnClickListener {
            showMechanicalRoofDialog()
        }

        cardLightingControl.setOnClickListener {
            showLightingControlDialog()
        }

        cardFieldSensors.setOnClickListener {
            showFieldSensorsDialog()
        }

        testInfluxDB()
    }

    // ==================== POWER MANAGEMENT DIALOG ====================
    private fun showPowerManagementDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.power_management_page)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnClose = dialog.findViewById<ImageView>(R.id.btnClose)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)
        val btnSave = dialog.findViewById<MaterialButton>(R.id.btnSave)

        btnClose.setOnClickListener { dialog.dismiss() }

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            Toast.makeText(this, "Changes Saved!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    // ==================== MECHANICAL ROOF DIALOG ====================
    private fun showMechanicalRoofDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.mechanical_roof_page)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Find views
        val btnCloseRoof = dialog.findViewById<ImageView>(R.id.btnCloseRoof)
        val txtHeaderAutoMode = dialog.findViewById<TextView>(R.id.txtHeaderAutoMode)
        val txtRoofPositionLabel = dialog.findViewById<TextView>(R.id.txtRoofPositionLabel)
        val txtMovementStatus = dialog.findViewById<TextView>(R.id.txtMovementStatus)
        val txtAutoModeStatus = dialog.findViewById<TextView>(R.id.txtAutoModeStatus)

        val switchAutoMode = dialog.findViewById<SwitchCompat>(R.id.switchAutoMode)
        val cardManualOverride = dialog.findViewById<CardView>(R.id.cardManualOverride)

        val btnOpenFully = dialog.findViewById<MaterialButton>(R.id.btnOpenFully)
        val btnCloseFully = dialog.findViewById<MaterialButton>(R.id.btnCloseFully)
        val btnStop = dialog.findViewById<MaterialButton>(R.id.btnStop)
        val btnResumeOperation = dialog.findViewById<MaterialButton>(R.id.btnResumeOperation)

        val txtLEDStatus = dialog.findViewById<TextView>(R.id.txtLEDStatus)
        val txtLEDReason = dialog.findViewById<TextView>(R.id.txtLEDReason)

        val btnCancelRoof = dialog.findViewById<MaterialButton>(R.id.btnCancelRoof)
        val btnSaveRoof = dialog.findViewById<MaterialButton>(R.id.btnSaveRoof)

        // Function to update LED status based on roof state and light level
        fun updateLEDStatus() {
            val lightLevel = sensorData["lux"] ?: 106.67 // Get current light level
            val isDark = lightLevel < 50.0 // Consider < 50 lux as dark

            val ledOn = when {
                roofState == RoofState.CLOSED && !isDark -> true  // Roof closed and not dark outside
                else -> false  // Roof open or it's dark outside
            }

            if (ledOn) {
                txtLEDStatus.text = "ON"
                txtLEDStatus.setTextColor(resources.getColor(android.R.color.holo_orange_light, null))
                txtLEDReason.text = "Roof closed, sufficient exterior light"
            } else {
                txtLEDStatus.text = "OFF"
                txtLEDStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                if (roofState == RoofState.OPEN) {
                    txtLEDReason.text = "Roof open, natural light available"
                } else {
                    txtLEDReason.text = "Exterior light level too low"
                }
            }
        }

        // Function to update roof display
        fun updateRoofDisplay() {
            // Update label based on state
            when (roofState) {
                RoofState.OPEN -> {
                    txtRoofPositionLabel.text = "OPEN"
                    txtRoofPositionLabel.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                }
                RoofState.CLOSED -> {
                    txtRoofPositionLabel.text = "CLOSED"
                    txtRoofPositionLabel.setTextColor(resources.getColor(android.R.color.holo_blue_dark, null))
                }
                RoofState.OPENING -> {
                    txtRoofPositionLabel.text = "OPENING..."
                    txtRoofPositionLabel.setTextColor(resources.getColor(android.R.color.holo_orange_light, null))
                }
                RoofState.CLOSING -> {
                    txtRoofPositionLabel.text = "CLOSING..."
                    txtRoofPositionLabel.setTextColor(resources.getColor(android.R.color.holo_orange_light, null))
                }
                RoofState.STOPPED -> {
                    txtRoofPositionLabel.text = "STOPPED"
                    txtRoofPositionLabel.setTextColor(resources.getColor(android.R.color.holo_red_light, null))
                }
            }

            // Update movement status
            when (roofState) {
                RoofState.OPEN, RoofState.CLOSED -> {
                    txtMovementStatus.text = "STATIONARY"
                    txtMovementStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                }
                RoofState.OPENING -> {
                    txtMovementStatus.text = "OPENING"
                    txtMovementStatus.setTextColor(resources.getColor(android.R.color.holo_orange_light, null))
                }
                RoofState.CLOSING -> {
                    txtMovementStatus.text = "CLOSING"
                    txtMovementStatus.setTextColor(resources.getColor(android.R.color.holo_orange_light, null))
                }
                RoofState.STOPPED -> {
                    txtMovementStatus.text = "STOPPED"
                    txtMovementStatus.setTextColor(resources.getColor(android.R.color.holo_red_light, null))
                }
            }

            // Update LED status
            updateLEDStatus()
        }

        // Function to update auto mode display
        fun updateAutoModeDisplay(isEnabled: Boolean) {
            if (isEnabled) {
                txtHeaderAutoMode.text = "● AUTO MODE ACTIVE"
                txtAutoModeStatus.text = "ENABLED"
                txtAutoModeStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                cardManualOverride.visibility = View.VISIBLE
            } else {
                txtHeaderAutoMode.text = "● MANUAL MODE"
                txtAutoModeStatus.text = "DISABLED"
                txtAutoModeStatus.setTextColor(resources.getColor(android.R.color.holo_red_light, null))
                cardManualOverride.visibility = View.GONE
            }
        }

        // Initialize display
        updateRoofDisplay()
        updateAutoModeDisplay(switchAutoMode.isChecked)

        // Set up listeners
        btnCloseRoof.setOnClickListener { dialog.dismiss() }

        switchAutoMode.setOnCheckedChangeListener { _, isChecked ->
            updateAutoModeDisplay(isChecked)
            if (isChecked) {
                Toast.makeText(this, "Auto mode enabled - Manual controls available", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Manual mode - Auto controls disabled", Toast.LENGTH_SHORT).show()
            }
        }

        btnOpenFully.setOnClickListener {
            if (roofState != RoofState.OPEN && roofState != RoofState.OPENING) {
                targetRoofState = RoofState.OPEN
                roofState = RoofState.OPENING
                updateRoofDisplay()

                // Simulate roof opening (3 seconds)
                lifecycleScope.launch {
                    kotlinx.coroutines.delay(3000)
                    if (roofState == RoofState.OPENING) {
                        roofState = RoofState.OPEN
                        updateRoofDisplay()
                        Toast.makeText(this@MainActivity, "Roof fully opened!", Toast.LENGTH_SHORT).show()
                    }
                }
                Toast.makeText(this, "Opening roof...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Roof is already open or opening", Toast.LENGTH_SHORT).show()
            }
        }

        btnCloseFully.setOnClickListener {
            if (roofState != RoofState.CLOSED && roofState != RoofState.CLOSING) {
                targetRoofState = RoofState.CLOSED
                roofState = RoofState.CLOSING
                updateRoofDisplay()

                // Simulate roof closing (3 seconds)
                lifecycleScope.launch {
                    kotlinx.coroutines.delay(3000)
                    if (roofState == RoofState.CLOSING) {
                        roofState = RoofState.CLOSED
                        updateRoofDisplay()
                        Toast.makeText(this@MainActivity, "Roof fully closed!", Toast.LENGTH_SHORT).show()
                    }
                }
                Toast.makeText(this, "Closing roof...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Roof is already closed or closing", Toast.LENGTH_SHORT).show()
            }
        }

        btnStop.setOnClickListener {
            if (roofState == RoofState.OPENING || roofState == RoofState.CLOSING) {
                roofState = RoofState.STOPPED
                updateRoofDisplay()
                Toast.makeText(this, "Roof movement stopped!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Roof is not moving", Toast.LENGTH_SHORT).show()
            }
        }

        btnResumeOperation.setOnClickListener {
            if (roofState == RoofState.STOPPED) {
                // Resume to target state
                roofState = if (targetRoofState == RoofState.OPEN) {
                    RoofState.OPENING
                } else {
                    RoofState.CLOSING
                }
                updateRoofDisplay()

                Toast.makeText(this, "Resuming operation...", Toast.LENGTH_SHORT).show()

                // Simulate completing the operation (2 seconds remaining)
                lifecycleScope.launch {
                    kotlinx.coroutines.delay(2000)
                    if (roofState == RoofState.OPENING || roofState == RoofState.CLOSING) {
                        roofState = targetRoofState
                        updateRoofDisplay()
                        val action = if (targetRoofState == RoofState.OPEN) "opened" else "closed"
                        Toast.makeText(this@MainActivity, "Roof $action!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Nothing to resume", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancelRoof.setOnClickListener { dialog.dismiss() }
        btnSaveRoof.setOnClickListener {
            Toast.makeText(this, "Roof settings saved!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    // ==================== LIGHTING CONTROL DIALOG ====================
    private fun showLightingControlDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.lighting_control_page)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCloseLighting = dialog.findViewById<ImageView>(R.id.btnCloseLighting)
        val switchMainLights = dialog.findViewById<SwitchCompat>(R.id.switchMainLights)
        val switchOutsideLights = dialog.findViewById<SwitchCompat>(R.id.switchOutsideLights)
        val txtMainLightStatus = dialog.findViewById<TextView>(R.id.txtMainLightStatus)
        val txtOutsideLightStatus = dialog.findViewById<TextView>(R.id.txtOutsideLightStatus)
        val editTextPattern = dialog.findViewById<TextInputEditText>(R.id.editTextPattern)
        val btnApplyPattern = dialog.findViewById<MaterialButton>(R.id.btnApplyPattern)
        val chipGroupActivePatterns = dialog.findViewById<ChipGroup>(R.id.chipGroupActivePatterns)
        val txtNoPatterns = dialog.findViewById<TextView>(R.id.txtNoPatterns)
        val btnStandardMode = dialog.findViewById<MaterialButton>(R.id.btnStandardMode)
        val btnEventMode = dialog.findViewById<MaterialButton>(R.id.btnEventMode)
        val btnAllPatterns = dialog.findViewById<MaterialButton>(R.id.btnAllPatterns)
        val btnCancelLighting = dialog.findViewById<MaterialButton>(R.id.btnCancelLighting)
        val btnSaveLighting = dialog.findViewById<MaterialButton>(R.id.btnSaveLighting)

        val activePatterns = mutableSetOf<Int>()

        fun updateChips() {
            chipGroupActivePatterns.removeAllViews()

            if (activePatterns.isEmpty()) {
                txtNoPatterns.visibility = TextView.VISIBLE
                chipGroupActivePatterns.visibility = ChipGroup.GONE
            } else {
                txtNoPatterns.visibility = TextView.GONE
                chipGroupActivePatterns.visibility = ChipGroup.VISIBLE

                activePatterns.sorted().forEach { pattern ->
                    val chip = Chip(this).apply {
                        text = pattern.toString()
                        isCloseIconVisible = true
                        setChipBackgroundColorResource(android.R.color.holo_orange_light)
                        setTextColor(resources.getColor(android.R.color.white, null))
                        setOnCloseIconClickListener {
                            activePatterns.remove(pattern)
                            updateChips()
                            editTextPattern.setText(activePatterns.sorted().joinToString(","))
                        }
                    }
                    chipGroupActivePatterns.addView(chip)
                }
            }
        }

        activePatterns.addAll(listOf(1, 3, 5, 7))
        updateChips()
        editTextPattern.setText("1,3,5,7")

        btnCloseLighting.setOnClickListener { dialog.dismiss() }

        switchMainLights.setOnCheckedChangeListener { _, isChecked ->
            txtMainLightStatus.text = if (isChecked) "Status: ON" else "Status: OFF"
            Toast.makeText(this, "Main lights ${if (isChecked) "ON" else "OFF"}", Toast.LENGTH_SHORT).show()
        }

        switchOutsideLights.setOnCheckedChangeListener { _, isChecked ->
            txtOutsideLightStatus.text = if (isChecked)
                "Status: ON • All zones operational"
            else
                "Status: OFF"
            Toast.makeText(this, "Outside lights ${if (isChecked) "ON" else "OFF"}", Toast.LENGTH_SHORT).show()
        }

        btnApplyPattern.setOnClickListener {
            val input = editTextPattern.text.toString().trim()
            if (input.isEmpty()) {
                Toast.makeText(this, "Please enter pattern numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val newPatterns = parsePatternInput(input)
                if (newPatterns.isEmpty()) {
                    Toast.makeText(this, "No valid patterns found", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                activePatterns.clear()
                activePatterns.addAll(newPatterns)
                updateChips()
                Toast.makeText(this, "Pattern applied: ${activePatterns.sorted().joinToString(",")}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid format. Use: 1,3,5 or 1-5", Toast.LENGTH_SHORT).show()
            }
        }

        btnStandardMode.setOnClickListener {
            activePatterns.clear()
            activePatterns.addAll(listOf(1, 2))
            updateChips()
            editTextPattern.setText("1,2")
            Toast.makeText(this, "Standard mode activated", Toast.LENGTH_SHORT).show()
        }

        btnEventMode.setOnClickListener {
            activePatterns.clear()
            activePatterns.addAll(listOf(3, 5, 8))
            updateChips()
            editTextPattern.setText("3,5,8")
            Toast.makeText(this, "Event mode activated", Toast.LENGTH_SHORT).show()
        }

        btnAllPatterns.setOnClickListener {
            activePatterns.clear()
            activePatterns.addAll(1..14)
            updateChips()
            editTextPattern.setText((1..14).joinToString(","))
            Toast.makeText(this, "All patterns activated", Toast.LENGTH_SHORT).show()
        }

        btnCancelLighting.setOnClickListener { dialog.dismiss() }
        btnSaveLighting.setOnClickListener {
            Toast.makeText(this, "Lighting settings saved!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    // ==================== FIELD SENSORS DIALOG ====================
    private fun showFieldSensorsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.field_sensors_page)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Find views
        val btnCloseFieldSensors = dialog.findViewById<ImageView>(R.id.btnCloseFieldSensors)
        val btnCloseFieldSensorsBottom = dialog.findViewById<MaterialButton>(R.id.btnCloseFieldSensorsBottom)
        val btnRefreshSensors = dialog.findViewById<MaterialButton>(R.id.btnRefreshSensors)

        // TextViews
        val txtTemperature = dialog.findViewById<TextView>(R.id.txtTemperature)
        val txtHumidity = dialog.findViewById<TextView>(R.id.txtHumidity)
        val txtSoilMoisture = dialog.findViewById<TextView>(R.id.txtSoilMoisture)
        val txtLux = dialog.findViewById<TextView>(R.id.txtLux)
        val txtCloudCover = dialog.findViewById<TextView>(R.id.txtCloudCover)
        val txtPrecipitation = dialog.findViewById<TextView>(R.id.txtPrecipitation)
        val txtRainDetection = dialog.findViewById<TextView>(R.id.txtRainDetection)
        val txtRainValue = dialog.findViewById<TextView>(R.id.txtRainValue)

        // ProgressBars
        val progressTemperature = dialog.findViewById<ProgressBar>(R.id.progressTemperature)
        val progressHumidity = dialog.findViewById<ProgressBar>(R.id.progressHumidity)
        val progressSoilMoisture = dialog.findViewById<ProgressBar>(R.id.progressSoilMoisture)
        val progressLux = dialog.findViewById<ProgressBar>(R.id.progressLux)
        val progressCloudCover = dialog.findViewById<ProgressBar>(R.id.progressCloudCover)
        val progressPrecipitation = dialog.findViewById<ProgressBar>(R.id.progressPrecipitation)

        // Function to update UI with sensor data
        fun updateSensorUI() {
            // Temperature (0-50°C range)
            val temp = sensorData["temperature_c"] ?: 0.0
            txtTemperature.text = String.format("%.2f", temp)
            progressTemperature.progress = temp.toInt()

            // Humidity (0-100%)
            val humidity = sensorData["humidity_pct"] ?: 0.0
            txtHumidity.text = String.format("%.2f", humidity)
            progressHumidity.progress = humidity.toInt()

            // Soil Moisture (0-100%)
            val soilMoisture = sensorData["soil_wet_pct"] ?: 0.0
            txtSoilMoisture.text = String.format("%.1f", soilMoisture)
            progressSoilMoisture.progress = soilMoisture.toInt()

            // Lux (0-200 range for display)
            val lux = sensorData["lux"] ?: 0.0
            txtLux.text = String.format("%.2f", lux)
            progressLux.progress = lux.toInt().coerceIn(0, 200)

            // Cloud Cover (0-100%)
            val cloudCover = sensorData["om_cloud_cover"] ?: 0.0
            txtCloudCover.text = String.format("%.1f", cloudCover)
            progressCloudCover.progress = cloudCover.toInt()

            // Precipitation (0-10mm range)
            val precipitation = sensorData["om_precipitation"] ?: 0.0
            txtPrecipitation.text = String.format("%.1f", precipitation)
            progressPrecipitation.progress = (precipitation * 10).toInt().coerceIn(0, 100)

            // Rain Detection
            val rainValue = sensorData["rain_detection"]?.toInt() ?: 0
            txtRainDetection.text = if (rainValue == 0) "DRY" else "RAIN"
            txtRainValue.text = "Value: $rainValue"

            // Change color based on rain detection
            if (rainValue == 1) {
                txtRainDetection.setBackgroundColor(resources.getColor(android.R.color.holo_red_light, null))
                txtRainDetection.setTextColor(resources.getColor(android.R.color.white, null))
            } else {
                txtRainDetection.setBackgroundColor(resources.getColor(android.R.color.white, null))
                txtRainDetection.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
            }
        }

        // Function to fetch sensor data from InfluxDB
        fun fetchSensorData() {
            lifecycleScope.launch {
                Toast.makeText(this@MainActivity, "Fetching sensor data...", Toast.LENGTH_SHORT).show()

                // TODO: Replace with actual InfluxDB query
                // For now, using mock data
                sensorData["temperature_c"] = 24.36
                sensorData["humidity_pct"] = 49.59
                sensorData["soil_wet_pct"] = 33.1
                sensorData["lux"] = 106.67
                sensorData["om_cloud_cover"] = 70.0
                sensorData["om_precipitation"] = 0.0
                sensorData["rain_detection"] = 0.0

                updateSensorUI()
                Toast.makeText(this@MainActivity, "Sensor data updated!", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize with current data
        fetchSensorData()

        // Set up listeners
        btnCloseFieldSensors.setOnClickListener { dialog.dismiss() }
        btnCloseFieldSensorsBottom.setOnClickListener { dialog.dismiss() }

        btnRefreshSensors.setOnClickListener {
            fetchSensorData()
        }

        dialog.show()
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun parsePatternInput(input: String): Set<Int> {
        val patterns = mutableSetOf<Int>()

        input.split(",").forEach { part ->
            val trimmed = part.trim()
            if (trimmed.contains("-")) {
                val range = trimmed.split("-")
                if (range.size == 2) {
                    val start = range[0].toInt()
                    val end = range[1].toInt()
                    for (i in start..end) {
                        if (i in 1..14) patterns.add(i)
                    }
                }
            } else {
                val num = trimmed.toIntOrNull()
                if (num != null && num in 1..14) {
                    patterns.add(num)
                }
            }
        }

        return patterns
    }

    private fun testInfluxDB() {
        lifecycleScope.launch {
            Toast.makeText(this@MainActivity, "Testing InfluxDB...", Toast.LENGTH_SHORT).show()

            val success = influxDB.testConnection()

            if (success) {
                Toast.makeText(this@MainActivity, "✓ Connected!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@MainActivity, "✗ Failed - Check Logcat", Toast.LENGTH_LONG).show()
            }
        }
    }
}