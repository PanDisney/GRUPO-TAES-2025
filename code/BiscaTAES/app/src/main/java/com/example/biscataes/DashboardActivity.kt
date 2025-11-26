package com.example.biscataes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts

class DashboardActivity : AppCompatActivity() {

    companion object {
        private const val MOCK_ENTRY_FEE = 50
        private var mockCoins = 1000
    }

    private lateinit var startGameButton: Button
    private lateinit var rankingButton: Button
    private lateinit var customizationButton: Button
    private lateinit var buyCoinsButton: Button
    private lateinit var welcomeText: TextView
    private lateinit var avatarImageView: ImageView
    private lateinit var coinsBalanceText: TextView
    private lateinit var developerModeLayout: LinearLayout
    private lateinit var devNoShuffleButton: Button
    private lateinit var devDebugDealButton: Button

    private val gameLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            mockCoins = result.data?.getIntExtra("FINAL_COINS", mockCoins) ?: mockCoins
            updateCoinDisplayAndButtonState()
            Toast.makeText(this, "Bem-vindo de volta! Saldo atual: $mockCoins moedas.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        startGameButton = findViewById(R.id.buttonStartGame)
        rankingButton = findViewById(R.id.buttonRanking)
        customizationButton = findViewById(R.id.buttonCustomization)
        buyCoinsButton = findViewById(R.id.buttonBuyCoins)
        welcomeText = findViewById(R.id.welcomeText)
        avatarImageView = findViewById(R.id.avatarImageView)
        coinsBalanceText = findViewById(R.id.coinsBalanceText)
        developerModeLayout = findViewById(R.id.developerModeLayout)
        devNoShuffleButton = findViewById(R.id.buttonDevStartPlayerFirst)
        devDebugDealButton = findViewById(R.id.buttonDevStartBotFirst)

        devNoShuffleButton.text = "Dev: No Shuffle"
        devDebugDealButton.text = "Dev: Debug Deal"

        val userName = intent.getStringExtra("USER_NAME")

        if (userName != null) {
            welcomeText.text = "Bem-vindo, $userName!"
            avatarImageView.visibility = View.VISIBLE
            avatarImageView.setImageResource(R.drawable.my_avatar)
            coinsBalanceText.visibility = View.VISIBLE
            buyCoinsButton.visibility = View.VISIBLE
            updateCoinDisplayAndButtonState()
        } else {
            welcomeText.text = "Bem-vindo, Anónimo!"
            avatarImageView.visibility = View.GONE
            coinsBalanceText.visibility = View.GONE
            buyCoinsButton.visibility = View.GONE
        }

        welcomeText.setOnLongClickListener {
            developerModeLayout.visibility = if (developerModeLayout.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            Toast.makeText(this, "Developer Mode Toggled", Toast.LENGTH_SHORT).show()
            true
        }

        startGameButton.setOnClickListener {
            startGameWithMode(null)
        }

        devNoShuffleButton.setOnClickListener {
            startGameWithMode("NO_SHUFFLE")
        }

        devDebugDealButton.setOnClickListener {
            startGameWithMode("DEBUG_DEAL")
        }

        rankingButton.setOnClickListener {
            val intent = Intent(this, RankingActivity::class.java)
            startActivity(intent)
        }

        customizationButton.setOnClickListener {
            val intent = Intent(this, CustomizationActivity::class.java)
            startActivity(intent)
        }

        buyCoinsButton.setOnClickListener {
            mockCoins += 100
            updateCoinDisplayAndButtonState()
            Toast.makeText(this, "100 moedas adicionadas!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startGameWithMode(startMode: String?) {
        val userName = intent.getStringExtra("USER_NAME")
        val intent = Intent(this, GameActivity::class.java)

        if (userName != null) {
            if (mockCoins >= MOCK_ENTRY_FEE) {
                intent.putExtra("CURRENT_COINS", mockCoins)
                intent.putExtra("ENTRY_FEE", MOCK_ENTRY_FEE)
                startMode?.let { intent.putExtra("START_MODE", it) }
                gameLauncher.launch(intent)
            } else {
                Toast.makeText(this, "Saldo insuficiente para iniciar o jogo! Necessita de $MOCK_ENTRY_FEE moedas.", Toast.LENGTH_LONG).show()
            }
        } else {
            startMode?.let { intent.putExtra("START_MODE", it) }
            startActivity(intent)
        }
    }

    private fun updateCoinDisplayAndButtonState() {
        coinsBalanceText.visibility = View.VISIBLE
        coinsBalanceText.text = "Moedas: $mockCoins"

        if (mockCoins < MOCK_ENTRY_FEE) {
            startGameButton.isEnabled = false
        } else {
            startGameButton.isEnabled = true
        }
    }
}
