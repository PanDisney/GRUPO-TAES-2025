package com.example.biscataes

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.Toast
import android.content.Intent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Encontrar o botão pelo ID que lhe deu no XML
        val playButton = findViewById<Button>(R.id.buttonPlayAnonymous)

        // 2. Definir o que acontece quando alguém clica
        playButton.setOnClickListener {
            // Ação do clique
            val intent = Intent(this, DashboardActivity::class.java)

            startActivity(intent)

        }

        // --- 2. NOVO BOTÃO: LOGIN ---
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        loginButton.setOnClickListener {
            // Abre o ecrã de Login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}