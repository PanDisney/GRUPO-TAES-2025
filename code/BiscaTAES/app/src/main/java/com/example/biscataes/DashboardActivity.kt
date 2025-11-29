@file:OptIn(InternalSerializationApi::class)

package com.example.biscataes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Wrapper class to match the {"data": {...}} structure from the API
@Serializable
data class ApiResponse(
    val data: UserDataResponse
)

@Serializable
data class UserDataResponse(
    val name: String,
    val email: String,
    var coins: Int,
    val nickname: String?,
    val photo_avatar_filename: String?
)

@Serializable
data class PurchaseRequest(val amount: Int)

@Serializable
data class PurchaseResponse(val message: String, val coins: Int)

@Serializable
data class MetadataResponse(
    val name: String,
    val version: String,
    val entry_fee: Int
)

class DashboardActivity : AppCompatActivity() {

    private var entryFee: Int = 50 // Default value

    // Configured Json instance for Ktor
    private val jsonSerializer = Json {
        ignoreUnknownKeys = true
    }

    // UI Elements
    private lateinit var startGameButton: Button
    private lateinit var rankingButton: Button
    private lateinit var customizationButton: Button
    private lateinit var buyCoinsButton: Button
    private lateinit var updateProfileButton: Button
    private lateinit var welcomeText: TextView
    private lateinit var avatarImageView: ImageView
    private lateinit var coinsBalanceText: TextView
    private lateinit var developerModeLayout: LinearLayout
    private lateinit var devNoShuffleButton: Button
    private lateinit var devDebugDealButton: Button

    private var currentUser: UserDataResponse? = null

    // Ktor HTTP Client with Authentication
    private val client by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(jsonSerializer) // Pass the configured Json instance
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        val token = getAuthToken()
                        if (token != null) {
                            BearerTokens(token, "") // refresh token is not used
                        } else {
                            null
                        }
                    }
                }
            }
        }
    }

    private val gameLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            currentUser?.coins = result.data?.getIntExtra("FINAL_COINS", currentUser?.coins ?: 0) ?: (currentUser?.coins ?: 0)
            updateCoinDisplayAndButtonState()
            Toast.makeText(this, "Welcome back! Current balance: ${currentUser?.coins ?: 0} coins.", Toast.LENGTH_LONG).show()
        }
    }

    private val updateProfileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            fetchUserData() // Refresh user data after profile update
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initializeViews()
        setupListeners()

        // Check if the user is anonymous
        val isAnonymous = intent.getBooleanExtra("IS_ANONYMOUS", false)
        if (isAnonymous) {
            setupAnonymousUser()
        } else {
            fetchUserData()
        }
    }

    private fun setupAnonymousUser() {
        currentUser = UserDataResponse(name = "Anonymous", email = "", coins = 500, nickname = null, photo_avatar_filename = null)
        updateUiWithUserData(currentUser!!)
        rankingButton.isEnabled = true
        buyCoinsButton.isEnabled = true

        fetchUserData()
        fetchMetadata()
    }

    private fun initializeViews() {
        startGameButton = findViewById(R.id.buttonStartGame)
        rankingButton = findViewById(R.id.buttonRanking)
        customizationButton = findViewById(R.id.buttonCustomization)
        buyCoinsButton = findViewById(R.id.buttonBuyCoins)
        updateProfileButton = findViewById(R.id.buttonUpdateProfile)
        welcomeText = findViewById(R.id.welcomeText)
        avatarImageView = findViewById(R.id.avatarImageView)
        coinsBalanceText = findViewById(R.id.coinsBalanceText)
        developerModeLayout = findViewById(R.id.developerModeLayout)
        devNoShuffleButton = findViewById(R.id.buttonDevStartPlayerFirst)
        devDebugDealButton = findViewById(R.id.buttonDevStartBotFirst)

        devNoShuffleButton.text = "Dev: No Shuffle"
        devDebugDealButton.text = "Dev: Debug Deal"
    }

    private fun setupListeners() {
        welcomeText.setOnLongClickListener {
            developerModeLayout.visibility = if (developerModeLayout.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            Toast.makeText(this, "Developer Mode Toggled", Toast.LENGTH_SHORT).show()
            true
        }

        startGameButton.setOnClickListener { startGameWithMode(null) }
        devNoShuffleButton.setOnClickListener { startGameWithMode("NO_SHUFFLE") }
        devDebugDealButton.setOnClickListener { startGameWithMode("DEBUG_DEAL") }

        rankingButton.setOnClickListener { startActivity(Intent(this, RankingActivity::class.java)) }
        customizationButton.setOnClickListener { startActivity(Intent(this, CustomizationActivity::class.java)) }

        buyCoinsButton.setOnClickListener {
            purchaseCoins(10) // Purchase 10 euros worth of coins
        }

        updateProfileButton.setOnClickListener {
            val intent = Intent(this, UpdateProfileActivity::class.java).apply {
                putExtra("CURRENT_NICKNAME", currentUser?.nickname)
                putExtra("CURRENT_AVATAR_FILENAME", currentUser?.photo_avatar_filename)
            }
            updateProfileLauncher.launch(intent)
        }
    }

    private fun getAuthToken(): String? {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("auth_token", null)
    }

    private fun fetchUserData() {
        lifecycleScope.launch {
            try {
                val response: HttpResponse = client.get("http://10.0.2.2:8000/api/user")

                when (response.status) {
                    HttpStatusCode.OK -> {
                        // Deserialize the full response and then extract the data object
                        val apiResponse = response.body<ApiResponse>()
                        val userData = apiResponse.data
                        currentUser = userData
                        updateUiWithUserData(userData)
                    }
                    HttpStatusCode.Unauthorized -> {
                        val errorBody = response.body<String>()
                        Log.e("DashboardActivity", "Authentication failed: $errorBody")
                        handleAuthenticationError("Session expired or invalid token.")
                    }
                    else -> handleFetchError("Failed to fetch user data. Status: ${response.status}")
                }
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error fetching user data", e)
                handleAuthenticationError("Could not retrieve user data. Please log in again.")
            }
        }
    }

    private fun fetchMetadata() {
        lifecycleScope.launch {
            try {
                val response: HttpResponse = client.get("http://10.0.2.2:8000/api/metadata")

                if (response.status == HttpStatusCode.OK) {
                    val metadata = response.body<MetadataResponse>()
                    entryFee = metadata.entry_fee
                    updateCoinDisplayAndButtonState()
                } else {
                    Toast.makeText(this@DashboardActivity, "Could not retrieve game settings.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error fetching metadata", e)
                Toast.makeText(this@DashboardActivity, "Could not retrieve game settings.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun purchaseCoins(euros: Int) {
        lifecycleScope.launch {
            try {
                val response: HttpResponse = client.post("http://10.0.2.2:8000/api/coins/purchase") {
                    contentType(io.ktor.http.ContentType.Application.Json)
                    setBody(PurchaseRequest(amount = euros))
                }

                if (response.status == HttpStatusCode.OK) {
                    val purchaseResponse = response.body<PurchaseResponse>()
                    currentUser?.coins = purchaseResponse.coins
                    updateCoinDisplayAndButtonState()
                    Toast.makeText(this@DashboardActivity, "Coins purchased successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@DashboardActivity, "Purchase failed. Status: ${response.status}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Error purchasing coins", e)
                Toast.makeText(this@DashboardActivity, "An error occurred during purchase.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUiWithUserData(userData: UserDataResponse) {
        welcomeText.text = "Welcome, ${userData.nickname ?: userData.name}!"
        coinsBalanceText.text = "Coins: ${userData.coins}"
        avatarImageView.visibility = View.VISIBLE
        coinsBalanceText.visibility = View.VISIBLE
        buyCoinsButton.visibility = View.VISIBLE
        developerModeLayout.visibility = View.GONE
        updateCoinDisplayAndButtonState()
    }

    private fun updateCoinDisplayAndButtonState() {
        val currentCoins = currentUser?.coins ?: 0
        coinsBalanceText.text = "Coins: $currentCoins"
        startGameButton.isEnabled = currentCoins >= entryFee
    }

    private fun startGameWithMode(startMode: String?) {
        currentUser?.let { user ->
            if (user.coins >= entryFee) {
                val intent = Intent(this, GameActivity::class.java).apply {
                    putExtra("CURRENT_COINS", user.coins)
                    putExtra("ENTRY_FEE", entryFee)
                    startMode?.let { putExtra("START_MODE", it) }
                }
                gameLauncher.launch(intent)
            } else {
                Toast.makeText(this, "Insufficient balance to start game! You need $entryFee coins.", Toast.LENGTH_LONG).show()
            }
        } ?: Toast.makeText(this, "Please log in to start a game.", Toast.LENGTH_SHORT).show()
    }

    private fun handleFetchError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        welcomeText.text = "Welcome!"
        avatarImageView.visibility = View.GONE
        coinsBalanceText.visibility = View.GONE
        buyCoinsButton.visibility = View.GONE
        developerModeLayout.visibility = View.GONE
        startGameButton.isEnabled = false
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