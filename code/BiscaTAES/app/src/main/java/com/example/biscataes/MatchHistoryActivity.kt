@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

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
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
@Serializable
data class MatchHistoryResponse(
    val data: List<GameMatch>
)

class MatchHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var matchHistoryAdapter: MatchHistoryAdapter
    private var matches = mutableListOf<GameMatch>()

    // Ktor HTTP Client
    private val client by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
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
        setContentView(R.layout.activity_match_history)

        setupRecyclerView()
        fetchMatchHistory()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view_match_history)
        recyclerView.layoutManager = LinearLayoutManager(this)
        matchHistoryAdapter = MatchHistoryAdapter(matches) { match ->
            // Handle click on a match item
            val intent = Intent(this, MatchDetailsActivity::class.java)
            val matchJson = Json.encodeToString(GameMatch.serializer(), match)
            intent.putExtra("MATCH_JSON", matchJson)
            startActivity(intent)
        }
        recyclerView.adapter = matchHistoryAdapter
    }

    private fun fetchMatchHistory() {
        lifecycleScope.launch {
            try {
                // Make the API call and parse the response using the wrapper class
                val response: MatchHistoryResponse = client.get("http://10.0.2.2:8000/api/matches").body()
                val fetchedMatches = response.data

                // Update the list of matches and notify the adapter
                matches.clear()
                matches.addAll(fetchedMatches)
                matchHistoryAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Log.e("MatchHistoryActivity", "Error fetching match history", e)
                Toast.makeText(this@MatchHistoryActivity, "Failed to fetch match history.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getAuthToken(): String? {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("auth_token", null)
    }
}