package com.example.myapplication
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.myapplication.R
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var cardPowerManagement: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find the Power & Energy Management card
        cardPowerManagement = findViewById(R.id.cardPowerManagement)

        // Set click listener - PUT THE DIALOG CODE HERE
        cardPowerManagement.setOnClickListener {
            showPowerManagementDialog()
        }
    }

    // ADD THIS METHOD
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
        val btnMaxSolar = dialog.findViewById<MaterialButton>(R.id.btnMaxSolar)
        val btnGridPriority = dialog.findViewById<MaterialButton>(R.id.btnGridPriority)
        val btnPowerSave = dialog.findViewById<MaterialButton>(R.id.btnPowerSave)
        val btnResetAuto = dialog.findViewById<MaterialButton>(R.id.btnResetAuto)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)
        val btnSave = dialog.findViewById<MaterialButton>(R.id.btnSave)

        // Set up click listeners
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        btnViewCharts.setOnClickListener {
            // TODO: Open charts activity
            Toast.makeText(this, "Opening Charts...", Toast.LENGTH_SHORT).show()
        }

        btnMaxSolar.setOnClickListener {
            Toast.makeText(this, "Max Solar Mode Activated", Toast.LENGTH_SHORT).show()
        }

        btnGridPriority.setOnClickListener {
            Toast.makeText(this, "Grid Priority Mode Activated", Toast.LENGTH_SHORT).show()
        }

        btnPowerSave.setOnClickListener {
            Toast.makeText(this, "Power Save Mode Activated", Toast.LENGTH_SHORT).show()
        }

        btnResetAuto.setOnClickListener {
            Toast.makeText(this, "Auto Mode Restored", Toast.LENGTH_SHORT).show()
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
}