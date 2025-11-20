package com.example.biscataes

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.content.Intent
import android.widget.TextView

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val startGameButton = findViewById<Button>(R.id.buttonStartGame)

        startGameButton.setOnClickListener {
            // 3. Criar a Intenção para ir para o GameActivity
            val intent = Intent(this, GameActivity::class.java)

            // 4. Iniciar a nova activity
            startActivity(intent)
        }
        
        val rankingButton = findViewById<Button>(R.id.buttonRanking)
        rankingButton.setOnClickListener {
            val intent = Intent(this, RankingActivity::class.java)
            startActivity(intent)
        }

        val customizationButton = findViewById<Button>(R.id.buttonCustomization)
        customizationButton.setOnClickListener {
            val intent = Intent(this, CustomizationActivity::class.java)
            startActivity(intent)
        }
    }
}
