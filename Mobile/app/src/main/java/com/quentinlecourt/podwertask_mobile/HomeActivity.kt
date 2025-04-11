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

class HomeActivity: AppCompatActivity() {
    private lateinit var sessionManager: SessionManager

    private lateinit var tv_h_username: TextView
    private lateinit var tv_h_welcome: TextView
    private lateinit var btn_devicesList: Button
    private lateinit var btn_logout: Button
    private lateinit var background: RelativeLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        sessionManager = SessionManager(this)

        background = findViewById(R.id.rl_backgroundHome)

        tv_h_username = findViewById(R.id.tv_h_username)
        tv_h_welcome = findViewById(R.id.tv_h_welcome)
        btn_devicesList = findViewById(R.id.btn_devicesList)
        btn_logout = findViewById(R.id.btn_logout)

        val token = sessionManager.fetchAuthToken()
        val claims = token?.decodeJwt()
        val userFirstname = claims?.get("firstname")
        println(claims)
        val userJob = claims?.get("job")

        if (userFirstname != null) {
            if (userJob != null) {
                when {
                    userJob.isEmpty()  || userFirstname.isEmpty() -> {
                        tv_h_welcome.text = "Impossible de récupérer le job de l'utilisateur"
                        background.setBackgroundResource(R.color.white)
                    }

                    userJob == "CE" -> {
                        tv_h_welcome.text = getString(R.string.welcome_admin)
                        tv_h_username.text = userFirstname
                        background.setBackgroundResource(R.drawable.admin_background)
                    }

                    userJob == "P" || userJob == "OP" -> {
                        tv_h_welcome.text = getString(R.string.welcome_user)
                        tv_h_username.text = userFirstname
                        background.setBackgroundResource(R.drawable.background)
                    }

                    else -> {
                        tv_h_welcome.text = "Le job de l'utilisateur n'est pas valide"

                    }
                }
            }
        }

        btn_devicesList.setOnClickListener{
            navigateToDevicesList()
        }
        btn_logout.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun navigateToDevicesList() {
        val intent = Intent(this, DevicesListActivity::class.java)
        startActivity(intent)
        finish()
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