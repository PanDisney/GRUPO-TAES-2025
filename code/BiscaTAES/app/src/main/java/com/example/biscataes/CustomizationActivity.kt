package com.example.biscataes

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CustomizationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)

        val btnRed = findViewById<ImageButton>(R.id.btnRed)
        val btnGreen = findViewById<ImageButton>(R.id.btnGreen)
        val btnYellow = findViewById<ImageButton>(R.id.btnYellow)
        val btnAbandoned = findViewById<ImageButton>(R.id.btnAbandoned)
        val btnBack = findViewById<Button>(R.id.btnBack)

        btnRed.setOnClickListener {
            savePreference("back_card_red")
        }

        btnGreen.setOnClickListener {
            savePreference("back_card_green")
        }

        btnYellow.setOnClickListener {
            savePreference("back_card_yellow")
        }

        btnAbandoned.setOnClickListener {
            savePreference("back_card_abandoned")
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun savePreference(drawableName: String) {
        val sharedPref = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("card_back", drawableName)
            apply()
        }
        Toast.makeText(this, "Verso da carta atualizado!", Toast.LENGTH_SHORT).show()
    }
}