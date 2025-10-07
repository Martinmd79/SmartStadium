package com.example.myapplication
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var cardPowerManagement: CardView

    private val influxDB = InfluxDBManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find the Power & Energy Management card
        cardPowerManagement = findViewById(R.id.cardPowerManagement)

        // Set click listener - PUT THE DIALOG CODE HERE
        cardPowerManagement.setOnClickListener {
            showPowerManagementDialog()
        }

        testInfluxDB()

    }


    private fun showPowerManagementDialog() {
        // Create dialog
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.power_management_page)

        // Make dialog full width
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Make dialog background transparent (so rounded corners show)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Find views in dialog
        val btnClose = dialog.findViewById<ImageView>(R.id.btnClose)
        val btnViewCharts = dialog.findViewById<MaterialButton>(R.id.btnViewCharts)

        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)
        val btnSave = dialog.findViewById<MaterialButton>(R.id.btnSave)

        // Set up click listeners
        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        btnViewCharts.setOnClickListener {
            Toast.makeText(this, "Opening Charts...", Toast.LENGTH_SHORT).show()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        btnSave.setOnClickListener {
            Toast.makeText(this, "Changes Saved!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        // Show the dialog
        dialog.show()
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

