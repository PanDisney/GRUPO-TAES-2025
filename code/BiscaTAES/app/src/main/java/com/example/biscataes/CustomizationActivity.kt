package com.example.biscataes

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class CustomizationActivity : AppCompatActivity() {

    private lateinit var btnRed: ImageButton
    private lateinit var btnGreen: ImageButton
    private lateinit var btnYellow: ImageButton
    private lateinit var btnAbandoned: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)

        btnRed = findViewById(R.id.btnRed)
        btnGreen = findViewById(R.id.btnGreen)
        btnYellow = findViewById(R.id.btnYellow)
        btnAbandoned = findViewById(R.id.btnAbandoned)
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
        highlightSelection(drawableName)
        // Toast removido conforme solicitado
    }

    private fun highlightSelection(selectedDrawableName: String) {
        // Lista de todos os botões
        val buttons = listOf(
            "back_card_red" to btnRed,
            "back_card_green" to btnGreen,
            "back_card_yellow" to btnYellow,
            "back_card_abandoned" to btnAbandoned
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