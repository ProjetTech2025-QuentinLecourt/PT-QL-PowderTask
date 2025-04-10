package com.quentinlecourt.podwertask_mobile

import SessionManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quentinlecourt.podwertask_mobile.data.api.MyAPI
import com.quentinlecourt.podwertask_mobile.data.model.LoginRequest
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.Toast

class LoginActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private val apiService: MyAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MyAPI::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        sessionManager = SessionManager(this)

        // Vérifier si déjà connecté
        if (sessionManager.fetchAuthToken() != null) {
            navigateToHomePageActivity()
            return
        }

        val loginButton: Button = findViewById(R.id.btn_login)
        loginButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.edt_username).text.toString()
            val password = findViewById<EditText>(R.id.edt_password).text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Email/mot de passe requis", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Exécuter l'appel réseau dans un CoroutineScope
            lifecycleScope.launch {
                try {
                    val loginRequest = LoginRequest(email, password)
                    val response = apiService.login(loginRequest)

                    if (response.isSuccessful) {
                        response.body()?.let { loginResponse ->
                            // Sauvegarde des données de session
                            sessionManager.saveAuthToken(loginResponse.bearerToken)
                            sessionManager.saveUserEmail(email)

                            Toast.makeText(
                                this@LoginActivity,
                                "Connecté avec succès",
                                Toast.LENGTH_SHORT
                            ).show()

                            navigateToHomePageActivity()
                        } ?: showError("Réponse vide du serveur")
                    } else {
                        showError("Erreur: ${response.code()} - ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    showError("Erreur réseau: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun saveToken(token: String) {
        // Sauvegarder le token
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        sharedPreferences.edit().putString("bearerToken", token).apply()
    }

    private fun navigateToHomePageActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}