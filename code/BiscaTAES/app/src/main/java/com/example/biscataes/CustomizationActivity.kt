@file:OptIn(InternalSerializationApi::class)

package com.example.biscataes

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json@InternalSerializationApi

@Serializable
data class PurchaseCardFaceRequest(val card_face_id: Int)

@Serializable
data class PurchaseCardFaceResponse(val message: String)

@Serializable
data class CardFacesResponse(val card_faces: List<CardFace>)

@Serializable
data class CoinsBalanceResponse(val coins: Int)

@Serializable
data class SelectCardFaceRequest(val card_face_id: Int)

class CustomizationActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cardFaceAdapter: CardFaceAdapter
    private lateinit var coinsBalanceText: TextView

    private val jsonSerializer = Json { ignoreUnknownKeys = true }
    private val client by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) { json(jsonSerializer) }
            install(Auth) {
                bearer {
                    loadTokens {
                        val token = getAuthToken()
                        if (token != null) BearerTokens(token, "") else null
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)

        recyclerView = findViewById(R.id.recyclerViewCardFaces)
        coinsBalanceText = findViewById(R.id.coinsBalanceText)
        val btnBack = findViewById<Button>(R.id.btnBack)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        cardFaceAdapter = CardFaceAdapter(this::purchaseCardFace, this::selectCardFace)
        recyclerView.adapter = cardFaceAdapter

        fetchCardFaces()
        fetchCoinsBalance()

        btnBack.setOnClickListener { finish() }
    }

    private fun getAuthToken(): String? {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("auth_token", null)
    }

    private fun fetchCoinsBalance() {
        lifecycleScope.launch {
            try {
                val response = client.get("http://10.0.2.2:8000/api/user/coins")
                if (response.status == HttpStatusCode.OK) {
                    val coinsBalanceResponse = response.body<CoinsBalanceResponse>()
                    coinsBalanceText.text = "Coins: ${coinsBalanceResponse.coins}"
                }
            } catch (e: Exception) {
                Toast.makeText(this@CustomizationActivity, "Error fetching coins: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchCardFaces() {
        lifecycleScope.launch {
            try {
                val response = client.get("http://10.0.2.2:8000/api/card-faces")
                if (response.status == HttpStatusCode.OK) {
                    val cardFacesResponse = response.body<CardFacesResponse>()
                    cardFaceAdapter.updateData(cardFacesResponse.card_faces)
                } else {
                    Toast.makeText(this@CustomizationActivity, "Failed to fetch card faces", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CustomizationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun purchaseCardFace(cardFace: CardFace) {
        lifecycleScope.launch {
            try {
                val response = client.post("http://10.0.2.2:8000/api/card-faces/${cardFace.id}/purchase") {
                    contentType(ContentType.Application.Json)
                    setBody(PurchaseCardFaceRequest(cardFace.id))
                }
                if (response.status == HttpStatusCode.OK) {
                    Toast.makeText(this@CustomizationActivity, "Purchase successful!", Toast.LENGTH_SHORT).show()
                    fetchCardFaces() // Refresh the list
                    fetchCoinsBalance() // Refresh the balance
                } else {
                    val error = response.body<PurchaseCardFaceResponse>()
                    Toast.makeText(this@CustomizationActivity, "Purchase failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CustomizationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectCardFace(cardFace: CardFace) {
        lifecycleScope.launch {
            try {
                val response = client.post("http://10.0.2.2:8000/api/user/card-face") {
                    contentType(ContentType.Application.Json)
                    setBody(SelectCardFaceRequest(cardFace.id))
                }
                if (response.status == HttpStatusCode.OK) {
                    Toast.makeText(this@CustomizationActivity, "Card face selected!", Toast.LENGTH_SHORT).show()
                    fetchCardFaces() // Refresh the list to show the new selection
                } else {
                    val error = response.body<PurchaseCardFaceResponse>()
                    Toast.makeText(this@CustomizationActivity, "Failed to select card face: ${error.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CustomizationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        client.close()
    }
}

class CardFaceAdapter(
    private val onPurchaseClick: (CardFace) -> Unit,
    private val onSelectClick: (CardFace) -> Unit
) : RecyclerView.Adapter<CardFaceAdapter.ViewHolder>() {

    private var cardFaces = listOf<CardFace>()
    private var selectedCardFaceId: Int? = null

    fun updateData(newCardFaces: List<CardFace>) {
        cardFaces = newCardFaces
        notifyDataSetChanged()
    }

    fun setSelectedCardFaceId(cardFaceId: Int?) {
        selectedCardFaceId = cardFaceId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_face, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cardFaces[position], onPurchaseClick, onSelectClick, selectedCardFaceId)
    }

    override fun getItemCount() = cardFaces.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.cardFaceName)
        private val costTextView: TextView = itemView.findViewById(R.id.cardFaceCost)
        private val imageView: ImageView = itemView.findViewById(R.id.cardFaceImage)
        private val buyButton: Button = itemView.findViewById(R.id.buyButton)

        fun bind(
            cardFace: CardFace,
            onPurchaseClick: (CardFace) -> Unit,
            onSelectClick: (CardFace) -> Unit,
            selectedCardFaceId: Int?
        ) {
            nameTextView.text = cardFace.name
            costTextView.text = "${cardFace.cost} coins"
            val resourceId = when (cardFace.image_name) {
                "back_card_red" -> R.drawable.back_card_red
                "back_card_green" -> R.drawable.back_card_green
                "back_card_yellow" -> R.drawable.back_card_yellow
                "back_card_silver" -> R.drawable.back_card_silver
                "back_card_black" -> R.drawable.back_card_black
                "back_card_purple" -> R.drawable.back_card_purple
                "back_card_rainbow" -> R.drawable.back_card_rainbow
                "back_card_abandoned" -> R.drawable.back_card_abandoned
                else -> R.drawable.card_back
            }
            imageView.setImageResource(resourceId)

            if (cardFace.is_owned) {
                buyButton.text = "Select"
                buyButton.setOnClickListener { onSelectClick(cardFace) }
            } else {
                buyButton.text = "Buy"
                buyButton.setOnClickListener { onPurchaseClick(cardFace) }
            }

            if (cardFace.id == selectedCardFaceId) {
                itemView.setBackgroundResource(R.drawable.selected_card_border)
            } else {
                itemView.background = null
            }
        }
    }
}

