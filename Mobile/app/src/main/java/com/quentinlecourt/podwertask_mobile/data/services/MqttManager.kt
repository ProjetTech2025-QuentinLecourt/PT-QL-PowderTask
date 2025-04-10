package com.quentinlecourt.podwertask_mobile.data.services

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.quentinlecourt.podwertask_mobile.BuildConfig
import com.quentinlecourt.podwertask_mobile.data.api.MyAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

class MqttManager(private val context: Context) {

    private val apiService: MyAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MyAPI::class.java)
    }

    private val TAG = "MqttManager"
    private var mqttClient: MqttClient? = null
    private val brokerUrl = BuildConfig.MQTT_BASE_URL
    private val clientId = "PowderScaleMobile_" + UUID.randomUUID().toString()

    private val subscriptions = mutableMapOf<Int, String>() // Map des IDs de machines aux topics souscrits
    private var statusCallback: ((Int, Boolean, Map<String, Boolean>) -> Unit)? = null

    fun connect(callback: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val responseMqttIdByApi = apiService.getMqttId()
                if (responseMqttIdByApi.isSuccessful && responseMqttIdByApi.code() == 200) {
                    val mqttResponse = responseMqttIdByApi.body()
                    if (mqttResponse != null) {
                        Log.d(TAG, "Credentials obtenus: ${mqttResponse.id}")
                        // Options de connexion MQTT
                        val options = MqttConnectOptions().apply {
                            isCleanSession = true
                            userName = mqttResponse.id
                            password = mqttResponse.password.toCharArray()
                        }

                        // Connexion au broker MQTT
                        mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())
                        mqttClient?.connect(options)

                        // Configuration des callbacks après connexion réussie
                        mqttClient?.setCallback(object : MqttCallback {
                            override fun connectionLost(cause: Throwable?) {
                                Log.w(TAG, "Connexion MQTT perdue", cause)
                                // Tentative de reconnexion
                                connect { success ->
                                    if (success) {
                                        // Réabonnement à tous les topics après reconnexion
                                        resubscribeToAllTopics()
                                    }
                                }
                            }

                            override fun messageArrived(topic: String?, message: MqttMessage?) {
                                message?.let {
                                    val payload = String(it.payload)
                                    Log.d(TAG, "Message reçu sur $topic: $payload")

                                    // Analyser le topic pour extraire l'ID de la machine
                                    if (topic?.contains("/status") == true) {
                                        val parts = topic.split("/")
                                        if (parts.size >= 3) {
                                            val machineId = parts[2].toIntOrNull()
                                            if (machineId != null) {
                                                parseStatusMessage(machineId, payload)
                                            }
                                        }
                                    }
                                }
                            }

                            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                                Log.d(TAG, "Livraison du message terminée")
                            }
                        })

                        Log.d(TAG, "Connexion MQTT réussie")
                        callback(true)
                    } else {
                        Log.e(TAG, "Réponse API vide")
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "Réponse API vide", Toast.LENGTH_SHORT).show()
                        }
                        callback(false)
                    }
                } else {
                    Log.e(TAG, "Erreur API: ${responseMqttIdByApi.code()}")
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            context,
                            "Erreur API: ${responseMqttIdByApi.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    callback(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de la connexion MQTT", e)
                callback(false)
            }
        }
    }

    private fun parseStatusMessage(machineId: Int, statusJson: String) {
        try {
            val isOnline = true
            val sensors = mutableMapOf<String, Boolean>()

            // extraction d'états de capteurs
            if (statusJson.contains("\"accelerometer\": true")) sensors["accelerometer"] = true
            if (statusJson.contains("\"accelerometer\": false")) sensors["accelerometer"] = false
            if (statusJson.contains("\"weightSensor\": true")) sensors["weightSensor"] = true
            if (statusJson.contains("\"weightSensor\": false")) sensors["weightSensor"] = false

            // Notifier le callback avec l'ID et les données
            statusCallback?.invoke(machineId, isOnline, sensors)

        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du parsing du message de statut", e)
        }
    }

    fun subscribeToMachineTopics(machineId: Int) {
        if (mqttClient == null || !mqttClient!!.isConnected) {
            Log.w(TAG, "Client MQTT non connecté, impossible de s'abonner")
            return
        }

        val statusTopic = "/scale/$machineId/status"
        try {
            mqttClient?.subscribe(statusTopic, 1) // Simplifié avec juste le topic et QoS
            Log.d(TAG, "Abonnement réussi à $statusTopic")
            subscriptions[machineId] = statusTopic
            // Envoyer immédiatement une demande de statut
            requestMachineStatus(machineId)
        } catch (e: MqttException) {
            Log.e(TAG, "Erreur lors de l'abonnement à $statusTopic", e)
        }
    }

    fun requestMachineStatus(machineId: Int) {
        if (mqttClient == null || !mqttClient!!.isConnected) {
            Log.w(TAG, "Client MQTT non connecté, impossible d'envoyer la commande")
            return
        }

        val commandTopic = "/scale/$machineId/commands"
        val message = "{\"command\":\"get_status\"}"

        try {
            mqttClient?.publish(commandTopic, message.toByteArray(), 1, false)
            Log.d(TAG, "Commande de statut envoyée à $commandTopic")
        } catch (e: MqttException) {
            Log.e(TAG, "Erreur lors de l'envoi de la commande à $commandTopic", e)
        }
    }

    fun setStatusCallback(callback: (Int, Boolean, Map<String, Boolean>) -> Unit) {
        this.statusCallback = callback
    }

    private fun resubscribeToAllTopics() {
        subscriptions.forEach { (machineId, topic) ->
            try {
                mqttClient?.subscribe(topic, 1)
                Log.d(TAG, "Réabonnement réussi à $topic")
            } catch (e: MqttException) {
                Log.e(TAG, "Erreur lors du réabonnement à $topic", e)
            }
        }
    }

    fun disconnect() {
        try {
            mqttClient?.let {
                if (it.isConnected) {
                    it.disconnect()
                    Log.d(TAG, "Déconnexion MQTT réussie")
                }
            }
        } catch (e: MqttException) {
            Log.e(TAG, "Erreur lors de la déconnexion MQTT", e)
        }
        mqttClient = null
    }
}