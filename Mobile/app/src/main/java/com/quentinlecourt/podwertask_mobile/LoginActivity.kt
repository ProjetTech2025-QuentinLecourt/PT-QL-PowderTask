package com.quentinlecourt.podwertask_mobile

import android.content.Intent
import android.os.Build
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

        val loginButton: Button = findViewById(R.id.btn_login)
        loginButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.edt_username).text.toString()
            val password = findViewById<EditText>(R.id.edt_password).text.toString()
            // Exécuter l'appel réseau dans un CoroutineScope
            lifecycleScope.launch {
                try {
                    val loginRequest = LoginRequest(email, password)
                    val response = apiService.login(loginRequest)

                    if (response.isSuccessful && response.code() == 200) {
                        val loginResponse = response.body()
                        if (loginResponse != null) {
                            // Sauvegarder le token
                            saveToken(loginResponse.bearerToken)
                            // Afficher le message dans un Toast
                            Toast.makeText(this@LoginActivity, loginResponse.message, Toast.LENGTH_SHORT).show()
                            navigateToMainActivity()
                        } else {
                            Toast.makeText(this@LoginActivity, "Réponse vide", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Gérer les cas d'erreur HTTP
                        Toast.makeText(
                            this@LoginActivity,
                            "Échec de l'authentification: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    // Gérer les erreurs réseau
                    Toast.makeText(this@LoginActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveToken(token: String) {
        // Sauvegarder le token
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        sharedPreferences.edit().putString("bearerToken", token).apply()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}