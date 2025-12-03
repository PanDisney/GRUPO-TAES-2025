package com.example.biscataes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import com.example.biscataes.GameListResponse

class RankingActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var gameAdapter: GameAdapter

    private val client by lazy {
        HttpClient(CIO) {
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

        recyclerView = findViewById(R.id.recyclerViewRanking)
        recyclerView.layoutManager = LinearLayoutManager(this)
        gameAdapter = GameAdapter(emptyList()) // Initialize with an empty list
        recyclerView.adapter = gameAdapter

        fetchBestGames()
    }

    private fun getAuthToken(): String? {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("auth_token", null)
    }

    private fun fetchBestGames() {
        lifecycleScope.launch {
            try {
                val response = client.get("http://10.0.2.2:8000/api/games/best")

                when (response.status) {
                    HttpStatusCode.OK -> {
                        val gameListResponse = response.body<GameListResponse>()
                        gameAdapter.updateGames(gameListResponse.data)
                    }
                    HttpStatusCode.Unauthorized -> {
                        val errorBody = response.body<String>()
                        Log.e("RankingActivity", "Authentication failed: $errorBody")
                        handleAuthenticationError("Session expired or invalid token.")
                    }
                    else -> {
                        Toast.makeText(this@RankingActivity, "Failed to fetch best games. Status: ${response.status}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("RankingActivity", "Error fetching best games", e)
                Toast.makeText(this@RankingActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleAuthenticationError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        getSharedPreferences("auth_prefs", Context.MODE_PRIVATE).edit().remove("auth_token").apply()
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        client.close()
    }
}