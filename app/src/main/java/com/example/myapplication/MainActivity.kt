package com.example.myapplication

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.ImageView
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
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var cardPowerManagement: CardView
    private lateinit var cardMechanicalRoof: CardView
    private lateinit var cardLightingControl: CardView

    private val influxDB = InfluxDBManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find the cards
        cardPowerManagement = findViewById(R.id.cardPowerManagement)
        cardMechanicalRoof = findViewById(R.id.cardMechanicalRoof)
        cardLightingControl = findViewById(R.id.cardLightingControl)

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

        testInfluxDB()
    }

    private fun showPowerManagementDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.power_management_page)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnClose = dialog.findViewById<ImageView>(R.id.btnClose)
        val btnViewCharts = dialog.findViewById<MaterialButton>(R.id.btnViewCharts)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)
        val btnSave = dialog.findViewById<MaterialButton>(R.id.btnSave)

        btnClose.setOnClickListener { dialog.dismiss() }
        btnViewCharts.setOnClickListener {
            Toast.makeText(this, "Opening Charts...", Toast.LENGTH_SHORT).show()
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            Toast.makeText(this, "Changes Saved!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showMechanicalRoofDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.mechanical_roof_page)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCloseRoof = dialog.findViewById<ImageView>(R.id.btnCloseRoof)
        val btnOpenFully = dialog.findViewById<MaterialButton>(R.id.btnOpenFully)
        val btnCloseFully = dialog.findViewById<MaterialButton>(R.id.btnCloseFully)
        val switchAutoMode = dialog.findViewById<SwitchCompat>(R.id.switchAutoMode)
        val btnCancelRoof = dialog.findViewById<MaterialButton>(R.id.btnCancelRoof)
        val btnSaveRoof = dialog.findViewById<MaterialButton>(R.id.btnSaveRoof)

        btnOpenFully.setOnClickListener {
            lifecycleScope.launch {
                val r = Api.openRoof()
                Toast.makeText(this@MainActivity,
                    r.fold(onSuccess = { "Opening roof ✓" },
                        onFailure = { "Failed: ${it.message}" }),
                    Toast.LENGTH_SHORT).show()
            }
        }

        btnCloseFully.setOnClickListener {
            lifecycleScope.launch {
                val r = Api.closeRoof()
                Toast.makeText(this@MainActivity,
                    r.fold(onSuccess = { "Closing roof ✓" },
                        onFailure = { "Failed: ${it.message}" }),
                    Toast.LENGTH_SHORT).show()
            }
        }


        switchAutoMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Auto mode enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Manual mode enabled", Toast.LENGTH_SHORT).show()
            }
        }
        btnCancelRoof.setOnClickListener { dialog.dismiss() }
        btnSaveRoof.setOnClickListener {
            Toast.makeText(this, "Roof settings saved!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showLightingControlDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.lighting_control_page)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Find views
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

        // Store active patterns
        val activePatterns = mutableSetOf<Int>()

        // Helper function to update chips
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

        // Initialize with default patterns (1,3,5,7)
        activePatterns.addAll(listOf(1, 3, 5, 7))
        updateChips()
        editTextPattern.setText("1,3,5,7")

        // Set up listeners
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

                // 🔗 send GET /hook?n=...
                lifecycleScope.launch {
                    val r = Api.applyPattern(activePatterns.toList())
                    Toast.makeText(
                        this@MainActivity,
                        r.fold(
                            onSuccess = { "Pattern applied: ${activePatterns.sorted().joinToString(",")}" },
                            onFailure = { "Failed to apply: ${it.message}" }
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (_: Exception) {
                Toast.makeText(this, "Invalid format. Use: 1,3,5 or 1-5", Toast.LENGTH_SHORT).show()
            }
        }


        // Quick preset buttons
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

    // Parse pattern input like "1,3,5" or "1-5,12,14"
    private fun parsePatternInput(input: String): Set<Int> {
        val patterns = mutableSetOf<Int>()

        input.split(",").forEach { part ->
            val trimmed = part.trim()
            if (trimmed.contains("-")) {
                // Handle range like "1-5"
                val range = trimmed.split("-")
                if (range.size == 2) {
                    val start = range[0].toInt()
                    val end = range[1].toInt()
                    for (i in start..end) {
                        if (i in 1..14) patterns.add(i)
                    }
                }
            } else {
                // Handle single number
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