package com.quentinlecourt.podwertask_mobile

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {



    private lateinit var mqttClient: MqttClient
    private val brokerUrl = "ssl://mqtt.powdertask.quentinlecourt.com:8883"
    private val clientId = "AndroidClient"
    private val topic = "test"
    private lateinit var tv_lastmessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv_lastmessage = findViewById(R.id.tv_lastmessage)
        connectToMqttBroker()
    }

    private fun connectToMqttBroker() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Options de connexion MQTT
                val options = MqttConnectOptions().apply {
                    isCleanSession = true // Démarrer une nouvelle session
                    userName = "pwtk-mqtt" // Identifiant
                    password = "esy6z0%FwS0f*X3vtb5X".toCharArray() // Mot de passe
                }

                // Créer le client MQTT
                mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())

                // Définir le callback pour gérer les messages reçus
                mqttClient.setCallback(object : MqttCallback {
                    override fun connectionLost(cause: Throwable?) {
                        // Gérer la perte de connexion
                        runOnUiThread {
                            tv_lastmessage.text = "Connexion perdue : ${cause?.message}"
                        }
                    }

                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        val payload = String(message?.payload ?: byteArrayOf())
                        runOnUiThread { // Utilisez runOnUiThread pour mettre à jour l'UI
                            tv_lastmessage.text = "Dernier message reçu: $payload"
                        }
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                        // Gérer la fin de la livraison
                        println("Message livré")
                    }
                })

                // Se connecter au broker MQTT
                mqttClient.connect(options)

                // S'abonner au topic
                mqttClient.subscribe(topic, 1)
                println("Connecté et abonné au topic : $topic")
            } catch (e: Exception) {
                e.printStackTrace()
                println("Erreur de connexion : ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mqttClient.disconnect()
    }
}