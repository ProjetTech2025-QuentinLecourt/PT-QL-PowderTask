package com.quentinlecourt.podwertask_mobile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.quentinlecourt.podwertask_mobile.data.mqtt.MqttService

class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private val mqttServiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Récupérer le message du broadcast
            val message = intent?.getStringExtra("message") ?: "Aucun message reçu"
            // Mettre à jour le TextView avec le dernier message
            textView.text = message
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialiser le TextView
        textView = findViewById(R.id.tv_lastmessage)

        // Démarrer le service MQTT
        val intent = Intent(this, MqttService::class.java)
        startService(intent)

        // Enregistrer le BroadcastReceiver pour écouter les mises à jour
        registerReceiver(mqttServiceReceiver, IntentFilter("MQTT_MESSAGE_UPDATE"))
    }

    override fun onDestroy() {
        super.onDestroy()
        // Désenregistrer le BroadcastReceiver pour éviter les fuites de mémoire
        unregisterReceiver(mqttServiceReceiver)
    }
}