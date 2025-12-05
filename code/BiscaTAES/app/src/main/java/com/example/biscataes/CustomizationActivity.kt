package com.example.biscataes

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CustomizationActivity : AppCompatActivity() {

    private lateinit var btnRed: ImageButton
    private lateinit var btnGreen: ImageButton
    private lateinit var btnYellow: ImageButton
    private lateinit var btnAbandoned: ImageButton
    private lateinit var btnSilver: ImageButton
    private lateinit var btnBlack: ImageButton
    private lateinit var btnRainbow: ImageButton
    private lateinit var btnPurple: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)

        btnRed = findViewById(R.id.btnRed)
        btnGreen = findViewById(R.id.btnGreen)
        btnYellow = findViewById(R.id.btnYellow)
        btnAbandoned = findViewById(R.id.btnAbandoned)
        btnSilver = findViewById(R.id.btnSilver)
        btnBlack = findViewById(R.id.btnBlack)
        btnRainbow = findViewById(R.id.btnRainbow)
        btnPurple = findViewById(R.id.btnPurple)
        val btnBack = findViewById<Button>(R.id.btnBack)

        // Carregar a preferência atual para destacar o botão correto
        val sharedPref = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        val currentSelection = sharedPref.getString("card_back", "back_card_red") ?: "back_card_red"
        highlightSelection(currentSelection)

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

        btnSilver.setOnClickListener {
            purchaseAndSavePreference("back_card_silver", 200)
        }

        btnBlack.setOnClickListener {
            purchaseAndSavePreference("back_card_black", 50)
        }

        btnRainbow.setOnClickListener {
            purchaseAndSavePreference("back_card_rainbow", 500)
        }

        btnPurple.setOnClickListener {
            purchaseAndSavePreference("back_card_purple", 25)
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun purchaseAndSavePreference(drawableName: String, price: Int) {
        val sharedPref = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        val ownedCards = sharedPref.getStringSet("owned_cards", setOf("back_card_red", "back_card_green", "back_card_yellow", "back_card_abandoned")) ?: setOf()

        if (ownedCards.contains(drawableName)) {
            savePreference(drawableName)
        } else {
            val coins = sharedPref.getInt("user_coins", 0)
            if (coins >= price) {
                val newCoins = coins - price
                val newOwnedCards = ownedCards.toMutableSet().apply { add(drawableName) }

                with(sharedPref.edit()) {
                    putInt("user_coins", newCoins)
                    putStringSet("owned_cards", newOwnedCards)
                    putString("card_back", drawableName)
                    apply()
                }
                highlightSelection(drawableName)
                Toast.makeText(this, "Compra efetuada com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Moedas insuficientes!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun savePreference(drawableName: String) {
        val sharedPref = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("card_back", drawableName)
            apply()
        }
        highlightSelection(drawableName)
        // Toast removido conforme solicitado
    }

    private fun highlightSelection(selectedDrawableName: String) {
        // Lista de todos os botões
        val buttons = listOf(
            "back_card_red" to btnRed,
            "back_card_green" to btnGreen,
            "back_card_yellow" to btnYellow,
            "back_card_abandoned" to btnAbandoned,
            "back_card_silver" to btnSilver,
            "back_card_black" to btnBlack,
            "back_card_rainbow" to btnRainbow,
            "back_card_purple" to btnPurple
        )

        for ((name, button) in buttons) {
            if (name == selectedDrawableName) {
                // Adicionar borda preta
                val shape = GradientDrawable()
                shape.shape = GradientDrawable.RECTANGLE
                shape.setColor(Color.TRANSPARENT) // Fundo transparente
                shape.setStroke(10, Color.BLACK) // Borda preta de 5px
                shape.cornerRadius = 0f // Ajuste conforme necessário
                button.background = shape
                // Como o background pode sobrepor a imagem dependendo de como o ImageButton funciona (src vs background),
                // e no XML definimos android:background="@android:color/transparent" e src="@drawable/...",
                // definir o background aqui vai desenhar por trás do src. 
                // Se o src não preencher tudo, a borda aparecerá.
                // Para garantir que a borda fica visivel "à volta", o padding pode ser util.
                button.setPadding(10, 10, 10, 10) 
            } else {
                // Remover borda
                button.background = null
                button.setPadding(0, 0, 0, 0)
            }
        }
    }
}
