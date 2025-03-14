package com.quentinlecourt.podwertask_mobile

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quentinlecourt.podwertask_mobile.data.api.MyAPI
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private val apiService: MyAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MyAPI::class.java)
    }

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
                val response = apiService.getMqttId()
                if (response.isSuccessful && response.code() == 200) {
                    val mqttResponse = response.body()
                    if (mqttResponse != null) {
                        println(mqttResponse.id)
                        println(mqttResponse.password)
                        // Options de connexion MQTT
                        val options = MqttConnectOptions().apply {
                            isCleanSession = true // Démarrer une nouvelle session
                            userName = mqttResponse.id // Identifiant
                            password = mqttResponse.password.toCharArray() // Mot de passe
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
                        // Afficher le message dans un Toast
                        Toast.makeText(this@MainActivity, "Connecté au broker", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Réponse vide", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Gérer les cas d'erreur HTTP
                    Toast.makeText(
                        this@MainActivity,
                        "Échec de la connexion au broker: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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