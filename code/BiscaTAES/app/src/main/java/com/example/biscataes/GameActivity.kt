@file:OptIn(InternalSerializationApi::class)

package com.example.biscataes

import android.content.Context
import android.content.Intent
import android.graphics.Color // <-- IMPORTAR CORES
import android.graphics.drawable.GradientDrawable // <-- Importar GradientDrawable para design melhor
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity // <-- IMPORTAR GRAVIDADE (para centrar o texto)
import android.view.View // <-- IMPORTAR VIEW para VISIBLE/INVISIBLE
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.lifecycleScope // <-- Importar lifecycleScope
import io.ktor.client.HttpClient // <-- Importar HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO // <-- Importar CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post // <-- Importar post
import io.ktor.client.request.put // <-- Importar put
import io.ktor.client.request.setBody // <-- Importar setBody
import io.ktor.client.statement.HttpResponse // <-- Importar HttpResponse
import io.ktor.http.ContentType // <-- Importar ContentType
import io.ktor.http.HttpStatusCode // <-- Importar HttpStatusCode
import io.ktor.http.contentType // <-- Importar contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch // <-- Importar launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable // <-- Importar Serializable
import kotlinx.serialization.SerialName // <-- Importar SerialName
import kotlinx.serialization.json.Json // <-- Importar Json

// Data class to represent the request body for deducting coins
@Serializable
data class DeductCoinsRequest(val amount: Int)

@Serializable
data class StartGameRequest(val type: String, val status: String)

// Data class to represent the API response
@Serializable
data class CoinApiResponse(val message: String, val coins: Int? = null)

@Serializable
data class User(
    val id: Int,
    val name: String,
    val nickname: String,
    val email: String,
    val photo_avatar_filename: String? = null,
    @SerialName("selected_card_back_image_name")
    val selected_card_back_image_name: String? = null
)





@Serializable

data class CreateGameRequest(
    val match_id: Int,
    val type: String, // Add type field
    val status: String,
    @SerialName("winner_user_id")
    val winnerId: Int?,
    val is_draw: Boolean, // Add is_draw field
    val player1_points: Int?,
    val player2_points: Int?,
    val player1_moves: List<MoveData>,
    val player2_moves: List<MoveData>,
    val total_time: Int?,
    val trump_card: String?,
    val first_trick_leader_id: Int?
)



@Serializable
data class UpdateMatchRequest(
    val status: String,
    @SerialName("winner_user_id")
    val winnerId: Int?,
    @SerialName("loser_user_id")
    val loserId: Int?,
    val player1_marks: Int,
    val player2_marks: Int,
    val total_time: Int?,
    val win_type: String? = null,
    val give_up: Boolean? = null
)



class GameActivity : AppCompatActivity() {
    private lateinit var gameEngine: GameEngine

    private var matchStartTime: Long = 0
    private var gameStartTime: Long = 0


    private var isUiLocked = false

    private var currentCoins: Int = 0

    private var entryFee: Int = 0

    private var isAnonymous: Boolean = false

    private var matchId: Int? = null

    private var player1: User? = null

    private var player2: User? = null

    // Configured Json instance for Ktor (from DashboardActivity)
    private val jsonSerializer = Json {
        ignoreUnknownKeys = true
    }

    // Ktor HTTP Client with Authentication (from DashboardActivity)
    private val client by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(jsonSerializer)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)

        currentCoins = intent.getIntExtra("CURRENT_COINS", 0)
        entryFee = intent.getIntExtra("ENTRY_FEE", 0)
        isAnonymous = intent.getBooleanExtra("IS_ANONYMOUS", false)

        if (isAnonymous) {
            // For anonymous users, just start the local game simulation
            initializeLocalGame()
        } else {
            // For logged-in users, create the match on the backend first
            startNewMatch()
        }

        val giveUpButton = findViewById<Button>(R.id.giveUpButton)

        giveUpButton.setOnClickListener {
            showGiveUpConfirmationDialog()
        }
    }



    



    private fun initializeLocalGame() {
        val startMode = intent.getStringExtra("START_MODE")
        val deckSize = intent.getIntExtra("DECK_SIZE", -1)
        val fastMode = intent.getBooleanExtra("FAST_MODE", false)

        gameStartTime = System.currentTimeMillis()
        gameEngine = GameEngine(player1, player2, startMode, deckSize = if(deckSize == -1) null else deckSize, fastMode = fastMode)

        // Draw the initial UI based on the new GameEngine state
        drawPlayerHand()
        drawBotHand()
        displayTrumpCard()
        updateScoreboardView()
        updateDeckView()
        checkIfBotPlaysFirst()
    }

    private fun getAuthToken(): String? {
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

        return sharedPref.getString("auth_token", null)
    }

    private fun startNewMatch() {
        lifecycleScope.launch {
            try {
                matchStartTime = System.currentTimeMillis()
                val matchResponse: GameMatch = client.post("http://10.0.2.2:8000/api/matches").body()

                matchId = matchResponse.id
                player1 = matchResponse.player1
                player2 = matchResponse.player2

                // Save player1's chosen card back to SharedPreferences
                player1?.selected_card_back_image_name?.let { cardBackName ->
                    val sharedPref = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("card_back", cardBackName)
                        apply()
                    }
                }

                initializeLocalGame()

            } catch (e: Exception) {

                Log.e("GameActivity", "Error starting new match on backend", e)
                Toast.makeText(this@GameActivity, "Failed to start match. Please try again.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private suspend fun createNewGameInBackend(status: String, winnerId: Int?) {
        val currentMatchId = matchId ?: return // Ensure matchId is available
        val isDraw = winnerId == null && status == "E"

        try {

            val requestBody = CreateGameRequest(
                match_id = currentMatchId,
                type = "S", // Set type to Single Player
                status = status,
                winnerId = winnerId,
                is_draw = isDraw,
                player1_points = gameEngine.playerPoints,
                player2_points = gameEngine.botPoints,
                player1_moves = gameEngine.playerMovesHistory,
                player2_moves = gameEngine.botMovesHistory,
                total_time = ((System.currentTimeMillis() - gameStartTime) / 1000).toInt(),
                trump_card = gameEngine.trumpCard?.toString(),
                first_trick_leader_id = gameEngine.firstTrickLeaderId
            )

            Log.d("SAVE_GAME_DEBUG", "Sending POST to /api/games")
            Log.d("SAVE_GAME_DEBUG", "Request Body: ${jsonSerializer.encodeToString(CreateGameRequest.serializer(), requestBody)}")

            val response: HttpResponse = client.post("http://10.0.2.2:8000/api/games") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (response.status == HttpStatusCode.Created) { // Expect 201 Created for POST
                Log.d("GameActivity", "Game record created successfully.")
            } else {
                Log.e("GameActivity", "Failed to create game record. Status: ${response.status.value}")
            }

        } catch (e: Exception) {
            Log.e("GameActivity", "Error creating game record", e)
        }
    }

    private suspend fun updateMatchOnBackend(winnerId: Int?, loserId: Int?, win_type: String? = null, give_up: Boolean? = null) {
        val currentMatchId = matchId ?: return

        try {
            val requestBody = UpdateMatchRequest(
                status = "E", // Ended
                winnerId = winnerId,
                loserId = loserId,
                player1_marks = gameEngine.playerGamesWon,
                player2_marks = gameEngine.botGamesWon,
                total_time = ((System.currentTimeMillis() - matchStartTime) / 1000).toInt(),
                win_type = win_type,
                give_up = give_up
            )

            Log.d("UPDATE_MATCH_DEBUG", "Sending PUT to /api/matches/$currentMatchId")
            Log.d("UPDATE_MATCH_DEBUG", "Request Body: ${jsonSerializer.encodeToString(UpdateMatchRequest.serializer(), requestBody)}")

            val response: HttpResponse = client.put("http://10.0.2.2:8000/api/matches/$currentMatchId") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (response.status == HttpStatusCode.OK) {
                Log.d("GameActivity", "Match updated successfully.")
            } else {
                Log.e("GameActivity", "Failed to update match. Status: ${response.status.value}")
            }

        } catch (e: Exception) {
            Log.e("GameActivity", "Error updating match", e)
        }
    }

    private fun showGiveUpConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Desistir do Jogo")
            .setMessage("Tem a certeza que quer desistir? A sua aposta será perdida.")
            .setPositiveButton("Sim, desistir") { _, _ ->
                if (isAnonymous) {
                    handleAnonymousGiveUp()
                } else {
                    lifecycleScope.launch {
                        val winnerId = player2?.id
                        val loserId = player1?.id
                        createNewGameInBackend(status = "I", winnerId = winnerId)
                        updateMatchOnBackend(winnerId = winnerId, loserId = loserId, give_up = true)

                        // Get updated user data
                        val response: HttpResponse = client.get("http://10.0.2.2:8000/api/user")
                        if (response.status == HttpStatusCode.OK) {
                            val apiResponse = response.body<ApiResponse>()
                            val userData = apiResponse.data
                            currentCoins = userData.coins
                        }

                        val resultIntent = Intent().apply {
                            putExtra("FINAL_COINS", currentCoins)
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                }
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun handleAnonymousGiveUp() {
        val resultIntent = Intent().apply {
            putExtra("FINAL_COINS", currentCoins)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun deductCoinsApiCall(amount: Int) {
        lifecycleScope.launch {
            try {
                val response: HttpResponse = client.post("http://10.0.2.2:8000/api/coins/deduct") {
                    contentType(ContentType.Application.Json)
                    setBody(DeductCoinsRequest(amount))
                }

                when (response.status) {
                    HttpStatusCode.OK -> {
                        val apiResponse = response.body<CoinApiResponse>()
                        Toast.makeText(this@GameActivity, apiResponse.message, Toast.LENGTH_SHORT).show()
                        val resultIntent = Intent().apply {
                            putExtra("FINAL_COINS", apiResponse.coins)
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                    HttpStatusCode.BadRequest -> {
                        val apiResponse = response.body<CoinApiResponse>()
                        Toast.makeText(this@GameActivity, "Erro: ${apiResponse.message}", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    HttpStatusCode.Unauthorized -> {
                        Toast.makeText(this@GameActivity, "Sessão expirada. Por favor, faça login novamente.", Toast.LENGTH_LONG).show()
                        handleAuthenticationError("Session expired or invalid token.")
                    }
                    else -> {
                        Toast.makeText(this@GameActivity, "Erro ao deduzir moedas: ${response.status.value}", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                Log.e("GameActivity", "Error deducting coins", e)
                Toast.makeText(this@GameActivity, "Erro de rede. Por favor, tente novamente.", Toast.LENGTH_LONG).show()
                finish()
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

    //
    // --- FUNÇÃO drawPlayerHand ATUALIZADA ---
    //
    private fun drawPlayerHand() {
        val hand = gameEngine.getPlayerHand()
        val handLayout = findViewById<LinearLayout>(R.id.playerHandLayout)

        handLayout.removeAllViews()

        for ((index, card) in hand.withIndex()) {
            val cardTextView = TextView(this)

            // --- MUDANÇA AQUI ---
            // 1. Obter os símbolos
            val rankSymbol = getRankSymbol(card.rank) // "A", "K", "7", "2"
            val suitSymbol = getSuitSymbol(card.suit) // "♥", "♦", "♣", "♠"

            // 2. Definir o texto formatado
            cardTextView.text = "$rankSymbol\n$suitSymbol" // "A" em cima, "♥" em baixo

            // --- Melhorar o Estilo ---
            cardTextView.textSize = 20f // Texto maior
            cardTextView.gravity = Gravity.CENTER // Centrar o texto (vertical e horizontal)
            cardTextView.setPadding(16, 16, 16, 16)

            // Definir o tamanho da "carta"
            val cardWidth = 120 // Largura em pixels
            val cardHeight = 180 // Altura em pixels
            val params = LinearLayout.LayoutParams(cardWidth, cardHeight)
            params.setMargins(8, 0, 8, 0)
            cardTextView.layoutParams = params

            // Design das cartas do Jogador (Frente)
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.setColor(Color.WHITE)
            shape.setStroke(2, Color.BLACK)
            shape.cornerRadius = 15f // Cantos arredondados
            cardTextView.background = shape

            // 3. Definir a cor do texto (Vermelho ou Preto)
            if (card.suit == Suit.HEARTS || card.suit == Suit.DIAMONDS) {
                cardTextView.setTextColor(Color.RED)
            } else {
                cardTextView.setTextColor(Color.BLACK)
            }
            // --- Fim do estilo ---

            // --- Listener de clique (igual a antes) ---

            cardTextView.setOnClickListener {
                // Se a UI estiver bloqueada (à espera do delay), não faz nada
                if (isUiLocked) return@setOnClickListener

                // 1. Bloquear a UI para evitar cliques duplos
                isUiLocked = true

                Log.d("GameActivity", "Clicou na carta: $card (índice: $index)")

                // 2. Avisar o motor
                if (gameEngine.isPlayerTurn()) {
                    // --- O JOGADOR LIDERA A VAZA ---
                    gameEngine.playerLeadsTrick(index)
                    updateTableView() // Mostra AMBAS as cartas na mesa

                    // Redesenhar a mão e resolver a vaza
                    drawPlayerHand()
                    drawBotHand() // Atualizar mão do bot (jogou carta)
                    startTrickResolutionDelay()
                } else {
                    // --- O JOGADOR RESPONDE AO BOT ---
                    val moveWasValid = gameEngine.playerResponds(index)
                    if (moveWasValid) {
                        updateTableView() // Mostra a carta do jogador
                        drawPlayerHand()
                        drawBotHand() // Atualizar mão do bot (sincronizar)
                        startTrickResolutionDelay()
                    } else {
                        // Jogada inválida!
                        Toast.makeText(this, "Jogada inválida! Deve assistir (jogar o mesmo naipe).", Toast.LENGTH_SHORT).show()
                        isUiLocked = false // Desbloquear a UI para o jogador tentar de novo
                    }
                }
            }

            handLayout.addView(cardTextView)
        }
    }

    // --- NOVA FUNÇÃO: Desenhar a mão do Bot (Costas da carta) ---
    private fun drawBotHand() {
        val handSize = gameEngine.getBotHandSize()
        val handLayout = findViewById<LinearLayout>(R.id.botHandLayout)

        handLayout.removeAllViews()

        // Obter a preferência do verso da carta
        val sharedPref = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        val cardBackName = sharedPref.getString("card_back", "back_card_red") // Padrão: vermelho

        val cardBackResId = resources.getIdentifier(cardBackName, "drawable", packageName)


        for (i in 0 until handSize) {
            val cardView = TextView(this)

            // Estilo "Verso da Carta" - USANDO IMAGEM
            if (cardBackResId != 0) {
                cardView.setBackgroundResource(cardBackResId)
            } else {
                cardView.setBackgroundResource(R.drawable.back_card_red) // Fallback
            }
            cardView.text = "" // Sem texto

            // Definir o tamanho da "carta" (EXATAMENTE igual ao do player)
            val cardWidth = 120
            val cardHeight = 180
            val params = LinearLayout.LayoutParams(cardWidth, cardHeight)
            params.setMargins(8, 0, 8, 0)
            cardView.layoutParams = params

            handLayout.addView(cardView)
        }
    }

    //
    // --- FUNÇÕES AJUDANTES (NOVAS) ---
    //
    // --- NOVA FUNÇÃO ---
    // Atualiza as cartas na "mesa"
    private fun updateTableView() {
        // Encontrar os TextViews da mesa
        val playerCardView = findViewById<TextView>(R.id.playerPlayedCardView)
        val botCardView = findViewById<TextView>(R.id.botPlayedCardView)

        // Apanhar as cartas da mesa a partir do motor
        val playerCard = gameEngine.playerCardOnTable
        val botCard = gameEngine.botCardOnTable

        // Helper para criar o shape das cartas jogadas
        fun setupPlayedCardStyle(view: TextView) {
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.setColor(Color.WHITE)
            shape.setStroke(2, Color.BLACK)
            shape.cornerRadius = 15f
            view.background = shape
        }

        // Atualizar a carta do jogador
        if (playerCard != null) {
            playerCardView.text = "${getRankSymbol(playerCard.rank)}\n${getSuitSymbol(playerCard.suit)}"
            setupPlayedCardStyle(playerCardView)
            if (playerCard.suit == Suit.HEARTS || playerCard.suit == Suit.DIAMONDS) {
                playerCardView.setTextColor(Color.RED)
            } else {
                playerCardView.setTextColor(Color.BLACK)
            }
            playerCardView.visibility = View.VISIBLE // Tornar visível
        } else {
            playerCardView.visibility = View.INVISIBLE // Esconder
        }

        // Atualizar a carta do Bot
        if (botCard != null) {
            botCardView.text = "${getRankSymbol(botCard.rank)}\n${getSuitSymbol(botCard.suit)}"
            setupPlayedCardStyle(botCardView)
            if (botCard.suit == Suit.HEARTS || botCard.suit == Suit.DIAMONDS) {
                botCardView.setTextColor(Color.RED)
            } else {
                botCardView.setTextColor(Color.BLACK)
            }
            botCardView.visibility = View.VISIBLE // Tornar visível
        } else {
            botCardView.visibility = View.INVISIBLE // Esconder
        }
    }
    // --- NOVA FUNÇÃO ---
// Mostra a carta de trunfo no ecrã
    private fun displayTrumpCard() {
        // 1. Ir buscar a carta de trunfo ao motor
        val trumpCard = gameEngine.trumpCard

        // 2. Encontrar o TextView no layout
        val trumpView = findViewById<TextView>(R.id.trumpCardView)

        if (trumpCard != null) {
            // 3. Usar as funções helper que já temos
            trumpView.text = getSuitSymbol(trumpCard.suit)
            trumpView.gravity = Gravity.CENTER
            trumpView.textSize = 32f


            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.setColor(Color.WHITE)
            shape.setStroke(4, Color.BLUE) // Borda azul para destacar
            shape.cornerRadius = 15f
            trumpView.background = shape

            // 4. Definir a cor
            if (trumpCard.suit == Suit.HEARTS || trumpCard.suit == Suit.DIAMONDS) {
                trumpView.setTextColor(Color.RED)
            } else {
                trumpView.setTextColor(Color.BLACK)
            }
            // GARANTIR QUE ESTÁ VISÍVEL
            trumpView.visibility = View.VISIBLE
        } else {
            // Segurança: se por algum motivo não houver trunfo, esconde
            trumpView.visibility = View.GONE
        }
    }

    // --- US-10: ATUALIZADA ---
    private fun updateScoreboardView() {
        // 1. Encontrar os TextViews para os pontos
        val playerScoreView = findViewById<TextView>(R.id.playerScoreView)
        val botScoreView = findViewById<TextView>(R.id.botScoreView)

        // 2. Encontrar os TextViews para os jogos ganhos (NOVOS)
        val playerGamesWonView = findViewById<TextView>(R.id.playerGamesWonView)
        val botGamesWonView = findViewById<TextView>(R.id.botGamesWonView)

        // 3. Ir buscar os pontos e jogos ganhos
        val playerPoints = gameEngine.playerPoints
        val botPoints = gameEngine.botPoints
        val playerGames = gameEngine.playerGamesWon
        val botGames = gameEngine.botGamesWon

        // Get player names from backend data, with fallback to default
        val p1Name = player1?.nickname ?: "Tu"
        val p2Name = player2?.nickname ?: "Bot"

        // 4. Atualizar o texto dos pontos
        playerScoreView.text = "$p1Name: $playerPoints pts"
        botScoreView.text = "$p2Name: $botPoints pts"

        // 5. Atualizar o texto dos jogos ganhos, com destaque para 4 vitórias
        playerGamesWonView.text = "$p1Name: $playerGames/4"
        botGamesWonView.text = "$p2Name: $botGames/4"
    }


    // --- NOVA FUNÇÃO: Atualiza o Deck ---
    private fun updateDeckView() {
        val deckView = findViewById<TextView>(R.id.deckView)
        val remaining = gameEngine.getCardsRemaining()

        // Obter a preferência do verso da carta
        val sharedPref = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        val cardBackName = sharedPref.getString("card_back", "back_card_red") // Padrão: vermelho
        val cardBackResId = resources.getIdentifier(cardBackName, "drawable", packageName)

        if (cardBackResId != 0) {
            deckView.setBackgroundResource(cardBackResId)
        } else {
            deckView.setBackgroundResource(R.drawable.back_card_red)
        }


        if (remaining > 0) {
            deckView.text = remaining.toString()
            deckView.visibility = View.VISIBLE
        } else {
            deckView.visibility = View.INVISIBLE
        }
    }

    // Converte um Rank num símbolo de texto
    private fun getRankSymbol(rank: Rank): String {
        return when (rank) {
            Rank.ACE -> "A"
            Rank.KING -> "K"
            Rank.JACK -> "J"
            Rank.QUEEN -> "Q"
            Rank.SEVEN -> "7"
            Rank.SIX -> "6"
            Rank.FIVE -> "5"
            Rank.FOUR -> "4"
            Rank.THREE -> "3"
            Rank.TWO -> "2"
        }
    }

    // Converte um Suit num emoji/símbolo
    private fun getSuitSymbol(suit: Suit): String {
        return when (suit) {
            Suit.HEARTS -> "♥"
            Suit.DIAMONDS -> "♦"
            Suit.CLUBS -> "♣"
            Suit.SPADES -> "♠"
        }
    }

    // --- NOVA FUNÇÃO (extraída) ---
    private fun startTrickResolutionDelay() {
        // Inicia o delay para resolver a vaza
        Handler(Looper.getMainLooper()).postDelayed({

            gameEngine.resolveCurrentTrick() // Resolve a vaza

            // Atualiza toda a UI
            updateTableView()      // Limpa a mesa
            drawPlayerHand()       // Mostra a mão com a nova carta
            drawBotHand()          // <-- Atualiza mão do Bot (compra de cartas)
            updateScoreboardView() // Atualiza os pontos
            updateDeckView()       // <-- Atualiza o deck (cartas compradas)

            // --- A GRANDE MUDANÇA ---
            // Verifica se o Bot joga a seguir
            checkIfBotPlaysFirst()

        }, 1500) // 1.5s de delay
    }

    // --- NOVA FUNÇÃO ---
    // Verifica se é a vez do Bot jogar primeiro
    private fun checkIfBotPlaysFirst() {
        // Se o jogo acabou, não faz nada
        if (!gameEngine.isGameRunning) {
            isUiLocked = false
            showEndGameDialog() // US-10: A lógica vai para aqui
            return
        }

        if (!gameEngine.isPlayerTurn()) {
            // É a vez do Bot liderar!
            Log.d("GameActivity", "É a vez do Bot liderar a vaza.")
            isUiLocked = true // Manter a UI bloqueada

            // Simular o "pensamento" do Bot
            Handler(Looper.getMainLooper()).postDelayed({
                gameEngine.botLeadsTrick() // Bot joga a sua carta
                updateTableView()          // Mostrar a carta do Bot na mesa
                drawBotHand()              // <-- Atualiza mão do Bot
                isUiLocked = false         // Desbloquear a UI para o jogador responder
            }, 1000) // Bot "pensa" por 1 segundo

        } else {
            // É a vez do jogador, desbloqueia a UI
            isUiLocked = false
        }
    }

    // --- US-10: LÓGICA DE FIM DE PARTIDA ---
    private fun showEndGameDialog() {
        // Primeiro, verificar se a partida acabou (alguém chegou a 4)
        if (gameEngine.playerGamesWon >= 4 || gameEngine.botGamesWon >= 4) {
            showEndMatchDialog()
            return // Para não mostrar o diálogo de fim de jogo
        }

        // --- Se a partida não acabou, mostra o resultado do JOGO atual ---
        val result = gameEngine.gameResult
        val title: String
        val message: String
        val gameWinnerId: Int?

        when (result) {
            GameEngine.GameResult.PLAYER_WINS -> {
                title = "Jogo ganho!"
                message = "Você ganhou este jogo. \nPontos: ${gameEngine.playerPoints} - ${gameEngine.botPoints}"
                gameWinnerId = player1?.id
            }
            GameEngine.GameResult.BOT_WINS -> {
                title = "Jogo perdido"
                message = "O Bot ganhou este jogo. \nPontos: ${gameEngine.playerPoints} - ${gameEngine.botPoints}"
                gameWinnerId = player2?.id
            }
            GameEngine.GameResult.DRAW -> {
                title = "Empate"
                message = "Este jogo terminou em empate."
                gameWinnerId = null // No winner on draw
            }
            else -> return
        }

        // Create a GameLog object from the current game state
        val gameLog = GameLog(
            playerFinalPoints = gameEngine.playerPoints,
            botFinalPoints = gameEngine.botPoints,
            playerMoves = gameEngine.playerMovesHistory,
            botMoves = gameEngine.botMovesHistory,
            trumpCard = gameEngine.trumpCard?.toString(),
            gameResult = result
        )
        // Save the game result and detailed log to ScoreManager
        ScoreManager.saveGameResult(this, gameLog)

        // Create a new game record in the backend for this finished game
        // if the user is not anonymous
        if (!isAnonymous) {
            lifecycleScope.launch {
                createNewGameInBackend("E", gameWinnerId)
            }
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(false)

        builder.setPositiveButton("Próximo Jogo") { _, _ ->
            gameEngine.startNewGame()
            resetUiForNewGame()
        }

        val dialog = builder.create()
        dialog.show()
    }

    // --- US-10: NOVA FUNÇÃO para o fim da PARTIDA ---
    private fun showEndMatchDialog() {
        val title: String
        val message: String
        val winnerId: Int?
        val loserId: Int?
        var win_type: String? = null

        if (gameEngine.playerGamesWon >= 4) {
            title = "VITÓRIA NA PARTIDA!"
            message = "Parabéns, você venceu a partida por ${gameEngine.playerGamesWon} a ${gameEngine.botGamesWon}!"
            winnerId = player1?.id
            loserId = player2?.id
            // Determine win_type based on last game points
            if (gameEngine.playerPoints == 120) {
                win_type = "BANDEIRA"
            } else if (gameEngine.playerPoints >= 91) {
                win_type = "CAPOTE"
            } else {
                win_type = "NORMAL"
            }
        } else {
            title = "DERROTA NA PARTIDA"
            message = "O Bot venceu a partida por ${gameEngine.botGamesWon} a ${gameEngine.playerGamesWon}!"
            winnerId = player2?.id
            loserId = player1?.id
        }

        lifecycleScope.launch {
            createNewGameInBackend("E", winnerId)
            updateMatchOnBackend(winnerId, loserId, win_type)
            
            // Get updated user data
            val response: HttpResponse = client.get("http://10.0.2.2:8000/api/user")
            if (response.status == HttpStatusCode.OK) {
                val apiResponse = response.body<ApiResponse>()
                val userData = apiResponse.data
                currentCoins = userData.coins
            }
            
            val resultIntent = Intent().apply {
                putExtra("FINAL_COINS", currentCoins)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage("$message\n\nSaldo será atualizado.")
        builder.setCancelable(false)

        builder.setPositiveButton("Voltar ao Menu") { _, _ ->
            // The finish() is now inside the coroutine
        }

        val dialog = builder.create()
        dialog.show()
    }

    // --- US-10: NOVA FUNÇÃO para reiniciar a UI
    private fun resetUiForNewGame() {
        drawPlayerHand()
        drawBotHand()
        displayTrumpCard()
        updateScoreboardView()
        updateDeckView()
        updateTableView() // Limpar as cartas da mesa
        isUiLocked = false

        // A decisão de quem joga a seguir é determinada pelo motor
        checkIfBotPlaysFirst()
    }

}
