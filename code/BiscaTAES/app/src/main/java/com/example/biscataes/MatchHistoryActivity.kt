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
    private var allMatches = mutableListOf<GameMatch>()
    private var displayedMatches = mutableListOf<GameMatch>()

    private lateinit var spinnerFilterResult: android.widget.Spinner
    private lateinit var spinnerSort: android.widget.Spinner
    private lateinit var buttonApplyFilters: android.widget.Button

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

        spinnerFilterResult = findViewById(R.id.spinner_filter_result)
        spinnerSort = findViewById(R.id.spinner_sort)
        buttonApplyFilters = findViewById(R.id.button_apply_filters)

        setupSpinners()
        setupRecyclerView()
        fetchMatchHistory()

        buttonApplyFilters.setOnClickListener {
            applyFiltersAndSorting()
        }
    }

    private fun setupSpinners() {
        // Filter by Result Spinner
        val resultOptions = arrayOf("Todos", "Vitórias", "Derrotas")
        val resultAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, resultOptions)
        resultAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilterResult.adapter = resultAdapter

        // Sort by Spinner
        val sortOptions = arrayOf("Data (Mais Recente)", "Data (Mais Antigo)", "Duração (Menor)", "Duração (Maior)")
        val sortAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSort.adapter = sortAdapter
    }


    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view_match_history)
        recyclerView.layoutManager = LinearLayoutManager(this)
        matchHistoryAdapter = MatchHistoryAdapter(displayedMatches) { match ->
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
                val response: MatchHistoryResponse = client.get("http://10.0.2.2:8000/api/matches").body()
                allMatches.clear()
                allMatches.addAll(response.data)
                applyFiltersAndSorting()

            } catch (e: Exception) {
                Log.e("MatchHistoryActivity", "Error fetching match history", e)
                Toast.makeText(this@MatchHistoryActivity, "Failed to fetch match history.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun applyFiltersAndSorting() {
        var filteredList = allMatches.toMutableList()

        // Filter by Result
        val selectedResult = spinnerFilterResult.selectedItem.toString()
        filteredList = when (selectedResult) {
            "Vitórias" -> filteredList.filter { it.winner?.id == getMyUserId() }.toMutableList()
            "Derrotas" -> filteredList.filter { it.winner?.id != getMyUserId() && it.winner != null }.toMutableList()
            else -> filteredList // "Todos"
        }

        // Sort
        val selectedSort = spinnerSort.selectedItem.toString()
        when (selectedSort) {
            "Data (Mais Recente)" -> filteredList.sortByDescending { it.began_at }
            "Data (Mais Antigo)" -> filteredList.sortBy { it.began_at }
            "Duração (Menor)" -> filteredList.sortBy { it.total_time }
            "Duração (Maior)" -> filteredList.sortByDescending { it.total_time }
        }

        displayedMatches.clear()
        displayedMatches.addAll(filteredList)
        matchHistoryAdapter.notifyDataSetChanged()
    }


    private fun getAuthToken(): String? {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("auth_token", null)
    }

    private fun getMyUserId(): Int? {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("user_id", -1).let { if (it == -1) null else it }
    }
}