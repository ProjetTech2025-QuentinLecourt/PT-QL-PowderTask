package com.quentinlecourt.podwertask_mobile

import SessionManager
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

class HomeActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager

    private lateinit var tvUsername: TextView
    private lateinit var tvWelcome: TextView
    private lateinit var btnDevicesList: Button
    private lateinit var btnLogout: Button
    private lateinit var background: RelativeLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        sessionManager = SessionManager(this)

        background = findViewById(R.id.rl_backgroundHome)

        tvUsername = findViewById(R.id.tv_h_username)
        tvWelcome = findViewById(R.id.tv_h_welcome)
        btnLogout = findViewById(R.id.btn_logout)
        btnDevicesList = findViewById(R.id.btn_devicesList)

        val token = sessionManager.fetchAuthToken()
        val claims = token?.decodeJwt()
        val userFirstname = claims?.get("firstname")
        val userJob = claims?.get("job")

        if (userFirstname != null) {
            if (userJob != null) {
                when {
                    userJob.isEmpty() || userFirstname.isEmpty() -> {
                        tvWelcome.text = "Impossible de récupérer le job de l'utilisateur"
                        background.setBackgroundResource(R.color.white)
                    }

                    userJob == "CE" -> {
                        tvWelcome.text = getString(R.string.welcome_admin)
                        tvUsername.text = userFirstname
                        background.setBackgroundResource(R.drawable.admin_background)
                    }

                    userJob == "P" || userJob == "OP" -> {
                        tvWelcome.text = getString(R.string.welcome_user)
                        tvUsername.text = userFirstname
                        background.setBackgroundResource(R.drawable.background)
                    }

                    else -> {
                        tvWelcome.text = "Le job de l'utilisateur n'est pas valide"

                    }
                }
            }
        }

        btnLogout.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnDevicesList.setOnClickListener {
            navigateToDevicesList()
        }
    }

    private fun navigateToDevicesList() {
        val intent = Intent(this, DevicesListActivity::class.java)
        startActivity(intent)
    }

    private fun String.decodeJwt(): Map<String, String>? {
        return try {
            val payload = this.split(".")[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val decodedString = String(decodedBytes, Charsets.UTF_8)
            Gson().fromJson(decodedString, Map::class.java) as? Map<String, String>
        } catch (e: Exception) {
            null
        }
    }
}