@file:OptIn(InternalSerializationApi::class)

package com.example.biscataes

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ⭐ NEW DATA CLASSES
@Serializable
data class GlobalResponse(
    val top_10: List<RankingUser>,
    val current_user: CurrentUserData
)

@Serializable
data class RankingUser(
    val rank: Int,
    val name: String,
    val total_wins: Int,
    val coins_earned: Int
)

@Serializable
data class CurrentUserData(
    val rank: Int?,
    val name: String,
    val total_wins: Int,
    val coins_earned: Int
)

@Serializable
data class PersonalResponse(
    val matches_played: Int,
    val wins: Int,
    val win_rate: Double,
    val current_coins: Int,
    val coins_earned: Int
)

class RankingActivity : AppCompatActivity() {

    private lateinit var layoutPersonal: ConstraintLayout
    private lateinit var layoutGlobal: LinearLayout
    private lateinit var btnPersonal: Button
    private lateinit var btnGlobal: Button

    // TextView for User Rank at the bottom
    private var tvUserRank: TextView? = null

    private val client by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        val token = getAuthToken()
                        if (token != null) {
                            BearerTokens(token, "")
                        } else {
                            null
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        layoutPersonal = findViewById(R.id.layoutPersonal)
        layoutGlobal = findViewById(R.id.layoutGlobal)
        btnPersonal = findViewById(R.id.btnPersonal)
        btnGlobal = findViewById(R.id.btnGlobal)

        btnPersonal.setOnClickListener { showPersonalStats() }
        btnGlobal.setOnClickListener { showGlobalRankings() }

        // Load Personal Stats by default
        showPersonalStats()
    }

    private fun showPersonalStats() {
        layoutPersonal.visibility = View.VISIBLE
        layoutGlobal.visibility = View.GONE
        btnPersonal.isEnabled = false
        btnGlobal.isEnabled = true

        fetchPersonalStats()
    }

    // ⭐ UPDATED: SINGLE API CALL
    private fun fetchPersonalStats() {
        lifecycleScope.launch {
            try {
                val response: PersonalResponse = client.get(
                    "http://10.0.2.2:8000/api/rankings/personal"
                ).body()

                updatePersonalStatsUI(
                    response.matches_played,
                    response.wins,
                    response.win_rate,
                    0,
                    0,
                    response.current_coins,
                    response.coins_earned,
                    0
                )

            } catch (e: Exception) {
                Log.e("RankingActivity", "Error fetching personal stats", e)
                Toast.makeText(this@RankingActivity, "Failed to load stats.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePersonalStatsUI(matchesPlayed: Int, wins: Int, winRate: Double, capotes: Int, bandeiras: Int, currentCoins: Int, earnedCoins: Int, purchasedCoins: Int) {
        findViewById<TextView>(R.id.textMatchesPlayed).text = "Jogos Disputados: $matchesPlayed"
        findViewById<TextView>(R.id.textPlayerWins).text = "Vitórias: $wins"

        val winRateFormatted = "%.1f%%".format(winRate)
        findViewById<TextView>(R.id.textWinRate).text = "Taxa de Vitória: $winRateFormatted"

        findViewById<TextView>(R.id.textCapotes).text = "Capotes: $capotes"
        findViewById<TextView>(R.id.textBandeiras).text = "Bandeiras: $bandeiras"

        findViewById<TextView>(R.id.textCurrentCoins).text = "Moedas Atuais: $currentCoins"
        findViewById<TextView>(R.id.textCoinsEarned).text = "Moedas Ganhas (Jogo): $earnedCoins"
        findViewById<TextView>(R.id.textCoinsPurchased).text = "Moedas Compradas: $purchasedCoins"
    }

    private fun showGlobalRankings() {
        layoutPersonal.visibility = View.GONE
        layoutGlobal.visibility = View.VISIBLE
        btnPersonal.isEnabled = true
        btnGlobal.isEnabled = false

        fetchGlobalRankings()
    }

    // ⭐ UPDATED: SINGLE API CALL
    private fun fetchGlobalRankings() {
        lifecycleScope.launch {
            try {
                val response: GlobalResponse = client.get(
                    "http://10.0.2.2:8000/api/rankings/global"
                ).body()

                displayGlobalRankings(response)

            } catch (e: Exception) {
                Log.e("RankingActivity", "Error fetching global rankings", e)
                Toast.makeText(this@RankingActivity, "Erro ao carregar ranking global.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayGlobalRankings(response: GlobalResponse) {
        val globalList = findViewById<ListView>(R.id.listViewGlobal)

        // Set ListView to use weight so it doesn't push the bottom TextView off screen
        val listParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        )
        globalList.layoutParams = listParams

        // Create adapter with top 10 users
        val adapter = ArrayAdapter(this@RankingActivity, android.R.layout.simple_list_item_1, response.top_10.map { user ->
            val displayName = user.name
            val wins = user.total_wins
            val coins = user.coins_earned
            "${user.rank}. $displayName - $wins Vitórias - $coins Moedas Ganhas"
        })
        globalList.adapter = adapter

        // Display Current User Stats at the bottom
        val currentUser = response.current_user

        if (tvUserRank == null) {
            tvUserRank = TextView(this@RankingActivity).apply {
                textSize = 18f
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.DKGRAY)
                gravity = Gravity.CENTER
                setPadding(32, 32, 32, 32)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 0)
                }
                layoutGlobal.addView(this)
            }
        }

        val displayName = currentUser.name
        tvUserRank?.text = """Sua Posição: #${currentUser.rank}
$displayName - ${currentUser.total_wins} Vitórias - ${currentUser.coins_earned} Moedas Ganhas"""
        tvUserRank?.visibility = View.VISIBLE
    }

    private fun getAuthToken(): String? {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("auth_token", null)
    }
}
