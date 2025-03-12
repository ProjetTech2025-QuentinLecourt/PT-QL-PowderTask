package com.quentinlecourt.podwertask_mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton: Button = findViewById(R.id.btn_login)
        loginButton.setOnClickListener {
            val username = findViewById<EditText>(R.id.edt_username).text.toString()
            val password = findViewById<EditText>(R.id.edt_password).text.toString()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            //loginUser(username, password)
        }
    }

//    // Methode pour soumettre les informations de login à l'API
//    private fun loginUser(username: String, password: String) {
//
//        val intent = Intent(this, MainActivity::class.java)
//        startActivity(intent)
//    }
}