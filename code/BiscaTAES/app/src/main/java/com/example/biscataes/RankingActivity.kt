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

@Serializable
data class Transaction(
    val id: Int,
    val type: String,
    val euros: Int? = null,
    val amount: Int? = null,
    val transaction_date: String? = null
)

@Serializable
data class TransactionListResponse(
    val data: List<Transaction>
)

@Serializable
data class UserListResponse(
    val data: List<RankingUser>
)

@Serializable
data class RankingUser(
    val id: Int,
    val name: String,
    val nickname: String?,
    val email: String,
    val coins: Int,
    val total_wins: Int? = null,
    val achievements_count: Int? = null
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

    private fun fetchPersonalStats() {
        lifecycleScope.launch {
            try {
                // Fetch user data for coins (Balance)
                val userResponse: ApiResponse = client.get("http://10.0.2.2:8000/api/user").body()
                val currentUser = userResponse.data
                val currentUserEmail = currentUser.email
                val currentCoins = currentUser.coins

                // Fetch matches for game stats
                val matchesResponse: MatchHistoryResponse = client.get("http://10.0.2.2:8000/api/matches").body()
                val matches = matchesResponse.data

                var matchesPlayed = matches.size
                var wins = 0
                var capotes = 0
                var bandeiras = 0
                var coinsEarnedPlaying = 0 // Calculate based on server matches

                matches.forEach { match ->
                    if (match.winner?.email == currentUserEmail) {
                        wins++
                        val myPoints = if (match.player1.email == currentUserEmail) match.player1_marks else match.player2_marks
                        if (myPoints != null) {
                            if (myPoints == 120) capotes++
                            else if (myPoints > 90) bandeiras++
                        }
                        // Calculate coins earned from this match
                        val winnerPoints = if (match.player1.email == currentUserEmail) match.player1_marks else match.player2_marks
                        val points = winnerPoints ?: 0
                        val coins = when {
                            points == 120 -> 80
                            points > 90 -> 40
                            else -> 10
                        }
                        coinsEarnedPlaying += coins
                    }
                }

                // Fetch transactions for Purchased Coins (API + Local)
                var coinsPurchased = 0
                
                try {
                     // 1. Get from API (if any)
                     val response: TransactionListResponse = client.get("http://10.0.2.2:8000/api/user/transactions").body()
                     val transactions = response.data
                     
                     transactions.forEach { transaction ->
                         val amount = transaction.amount ?: 0
                         if (transaction.type.contains("purchase", ignoreCase = true) || 
                             transaction.type.contains("compra", ignoreCase = true)) {
                             coinsPurchased += amount
                         }
                     }
                } catch (e: Exception) {
                    Log.e("RankingActivity", "Error fetching transactions", e)
                }

                // 2. Add local purchased coins (fallback for missing API transaction records)
                val prefs = getSharedPreferences("BiscaPrefs", Context.MODE_PRIVATE)
                val localPurchased = prefs.getInt("local_purchased_coins", 0)
                coinsPurchased += localPurchased


                updatePersonalStatsUI(matchesPlayed, wins, capotes, bandeiras, currentCoins, coinsPurchased, coinsEarnedPlaying)

            } catch (e: Exception) {
                Log.e("RankingActivity", "Error fetching stats", e)
                Toast.makeText(this@RankingActivity, "Failed to load stats.", Toast.LENGTH_SHORT).show()
                val localStats = ScoreManager.getStats(this@RankingActivity)
                
                // Fallback to local stats for display if API fails
                // Note: localStats.coins might be outdated compared to API user.coins, but it's a fallback.
                updatePersonalStatsUI(localStats.matchesPlayed, localStats.playerWins, localStats.capotes, localStats.bandeiras, localStats.coins, 0, localStats.coins)
            }
        }
    }

    private fun updatePersonalStatsUI(matchesPlayed: Int, wins: Int, capotes: Int, bandeiras: Int, currentCoins: Int, purchasedCoins: Int, earnedCoins: Int) {
        findViewById<TextView>(R.id.textMatchesPlayed).text = "Jogos Disputados: $matchesPlayed"
        findViewById<TextView>(R.id.textPlayerWins).text = "Vitórias: $wins"

        val winRate = if (matchesPlayed > 0) {
            (wins.toFloat() / matchesPlayed.toFloat()) * 100
        } else {
            0f
        }
        findViewById<TextView>(R.id.textWinRate).text = "Taxa de Vitória: %.1f%%".format(winRate)

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

    private fun fetchGlobalRankings() {
        lifecycleScope.launch {
            try {
                // 1. Fetch current user to identify "me"
                val userResponse: ApiResponse = client.get("http://10.0.2.2:8000/api/user").body()
                val currentUser = userResponse.data
                
                // 2. Fetch all users from API
                val response: UserListResponse = client.get("http://10.0.2.2:8000/api/users").body()
                val allUsers = response.data

                // 3. Fetch all matches to calculate wins dynamically
                val matchesResponse: MatchHistoryResponse = client.get("http://10.0.2.2:8000/api/matches").body()
                val allMatches = matchesResponse.data

                // Calculate wins and earned coins for each user based on matches
                val winsMap = mutableMapOf<String, Int>()
                val earnedCoinsMap = mutableMapOf<String, Int>()

                allMatches.forEach { match ->
                    val winnerEmail = match.winner?.email
                    if (winnerEmail != null) {
                        winsMap[winnerEmail] = winsMap.getOrDefault(winnerEmail, 0) + 1

                        // Calculate earned coins based on ScoreManager logic
                        val winnerPoints = if (match.player1.email == winnerEmail) match.player1_marks else match.player2_marks
                        val points = winnerPoints ?: 0
                        val coins = when {
                            points == 120 -> 80
                            points > 90 -> 40
                            else -> 10
                        }
                        earnedCoinsMap[winnerEmail] = earnedCoinsMap.getOrDefault(winnerEmail, 0) + coins
                    }
                }
                
                // 4. Filter out Bots and update wins and coins
                val realUsers = allUsers.filter { 
                    !it.name.contains("bot", ignoreCase = true) && 
                    (it.nickname == null || !it.nickname.contains("bot", ignoreCase = true))
                }.map { user ->
                    val calculatedWins = winsMap[user.email] ?: 0
                    val calculatedCoins = earnedCoinsMap[user.email] ?: 0
                    
                    // Use calculatedCoins for all users, including the current user, for global consistency.
                    user.copy(total_wins = calculatedWins, coins = calculatedCoins)
                }

                // 5. Sort by Wins (desc) then Coins (desc)
                val sortedUsers = realUsers.sortedWith(
                    compareByDescending<RankingUser> { it.total_wins ?: 0 }
                        .thenByDescending { it.coins }
                )
                
                // 6. Take Top 10 for the list
                val top10Users = sortedUsers.take(10)

                val globalList = findViewById<ListView>(R.id.listViewGlobal)
                
                // FIX: Set ListView to use weight so it doesn't push the bottom TextView off screen
                val listParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
                globalList.layoutParams = listParams
                
                val adapter = ArrayAdapter(this@RankingActivity, android.R.layout.simple_list_item_1, top10Users.mapIndexed { index, user ->
                    val displayName = user.nickname ?: user.name
                    val wins = user.total_wins ?: 0
                    val coins = user.coins // This now displays Earned Coins
                    val achievements = user.achievements_count ?: 0
                    "${index + 1}. $displayName - $wins Vitórias - $coins Moedas Ganhas - $achievements Conquistas"
                })
                globalList.adapter = adapter
                
                // 7. Display Current User Stats at the bottom
                // Find my rank in the full sorted list of real users
                val myIndex = sortedUsers.indexOfFirst { it.email == currentUser.email }
                
                if (myIndex != -1) {
                    val myRank = myIndex + 1
                    val myEntry = sortedUsers[myIndex]
                    val myDisplayName = myEntry.nickname ?: myEntry.name
                    val myStatsText = """Sua Posição: #$myRank
$myDisplayName - ${myEntry.total_wins ?: 0} Vitórias - ${myEntry.coins} Moedas Ganhas"""
                    
                    // Add or Update TextView
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
                        }
                        layoutGlobal.addView(tvUserRank)
                    }
                    tvUserRank?.text = myStatsText
                    tvUserRank?.visibility = View.VISIBLE
                }

            } catch (e: Exception) {
                Log.e("RankingActivity", "Error fetching global rankings", e)
                Toast.makeText(this@RankingActivity, "Erro ao carregar ranking global.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAuthToken(): String? {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("auth_token", null)
    }
}
