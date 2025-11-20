package com.example.biscataes


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// Podes comentar os imports da API por agora
// import com.suaequipa.bisca.api...

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- SIMULAÇÃO DO BACKEND (MOCK) ---
            // Aqui definimos "hardcoded" qual é o utilizador válido para testes
            // Mais tarde, quando o backend de DAD estiver pronto, trocas isto pelo Retrofit

            if (email == "aluno@mail.com" && password == "1234") {
                // 1. Simular Sucesso (200 OK)
                simulateSuccessfulLogin()
            } else {
                // 2. Simular Erro (401 Unauthorized)
                Toast.makeText(this, "Login falhou: Credenciais erradas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun simulateSuccessfulLogin() {
        // Simular um loading (opcional, para parecer real)
        Toast.makeText(this, "A autenticar...", Toast.LENGTH_SHORT).show()

        // Fingir que recebemos dados do servidor
        val fakeUserName = "Utilizador de Teste"

        Toast.makeText(this, "Bem-vindo, $fakeUserName!", Toast.LENGTH_LONG).show()

        // Navegar para o Dashboard
        val intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra("USER_NAME", fakeUserName)
        startActivity(intent)
        finish()
    }
}