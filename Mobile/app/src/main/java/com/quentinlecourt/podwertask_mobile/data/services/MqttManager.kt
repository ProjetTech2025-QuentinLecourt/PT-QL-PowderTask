package com.quentinlecourt.podwertask_mobile.data.services

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.quentinlecourt.podwertask_mobile.BuildConfig
import com.quentinlecourt.podwertask_mobile.data.api.MyAPI
import com.quentinlecourt.podwertask_mobile.data.api.RetrofitInstance
import com.quentinlecourt.podwertask_mobile.data.model.MachineDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

class MqttManager(private val context: Context) {

    private val apiService: MyAPI by lazy { RetrofitInstance.apiService }

    private val TAG = "MqttManager"
    private var mqttClient: MqttClient? = null
    private val brokerUrl = BuildConfig.MQTT_BASE_URL
    private val clientId = "PowderScaleMobile_" + UUID.randomUUID().toString()

    private val subscriptions =
        mutableMapOf<Int, String>() // Map des IDs de machines aux topics souscrits

    private val lastMessages = mutableMapOf<Int, MachineDetails?>()
    private val lastOnlineStatus = mutableMapOf<Int, Boolean>()

    private var statusCallback: ((Int, Boolean, MachineDetails?) -> Unit)? = null

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
                                        resubscribeToAllTopics()
                                    }
                                }
                            }

                            override fun messageArrived(topic: String?, message: MqttMessage?) {
                                message?.let {
                                    val payload = String(it.payload)
                                    Log.d(TAG, "Message reçu sur $topic: $payload")

                                    // Analyser le topic pour extraire l'ID de la machine
                                    if (topic?.contains("/status/details") == true) {
                                        val parts = topic.split("/")
                                        if (parts.size >= 3) {
                                            val machineId = parts[2].toIntOrNull()
                                            if (machineId != null) {
                                                parseDetailsStatusMessage(machineId, payload)
                                            }
                                        }
                                    }
                                    // Analyser le topic pour extraire l'ID de la machine
                                    if (topic?.contains("/status/online") == true) {
                                        val parts = topic.split("/")
                                        if (parts.size >= 3) {
                                            val machineId = parts[2].toIntOrNull()
                                            if (machineId != null) {
                                                parseOnlineStatusMessage(machineId, payload)
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

    fun setStatusCallback(callback: (Int, Boolean, MachineDetails?) -> Unit) {
        this.statusCallback = callback
    }

    private fun parseDetailsStatusMessage(machineId: Int, statusJson: String) {
        try {
            val gson = Gson()
            val details = gson.fromJson(statusJson, MachineDetails::class.java)

            // Stockage du dernier message
            lastMessages[machineId] = details

            val lastOnline = lastOnlineStatus[machineId] ?: false

            // Notifier avec les dernières données
            statusCallback?.invoke(machineId, lastOnline, details)

        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du parsing du message de détails", e)
        }
    }

    private fun parseOnlineStatusMessage(machineId: Int, statusJson: String) {
        try {
            val jsonObject = JSONObject(statusJson)
            val isOnline = jsonObject.optBoolean("online", false)

            // Stockage du dernier message
            lastOnlineStatus[machineId] = isOnline

            val lastDetails = lastMessages[machineId]

            // Notifier avec les dernières données
            statusCallback?.invoke(machineId, isOnline, lastDetails)

        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du parsing du message online", e)
        }
    }

    fun subscribeToMachineTopics(machineId: Int) {
        if (mqttClient == null || !mqttClient!!.isConnected) {
            Log.w(TAG, "Client MQTT non connecté, impossible de s'abonner")
            return
        }

        val statusTopic = "/scale/$machineId/status"
        val onlineStatusTopic = "$statusTopic/online"
        val detailsStatusTopic = "$statusTopic/details"

        try {
            mqttClient?.subscribe(onlineStatusTopic, 1)
            Log.d(TAG, "Abonnement réussi à $onlineStatusTopic")
            subscriptions[machineId] = onlineStatusTopic
            mqttClient?.subscribe(detailsStatusTopic, 1)

            Log.d(TAG, "Abonnement réussi à $detailsStatusTopic")
            subscriptions[machineId] = detailsStatusTopic
        } catch (e: MqttException) {
            Log.e(TAG, "Erreur lors de l'abonnement à $statusTopic", e)
        }
    }

    fun getLastKnownState(machineId: Int): Pair<Boolean, MachineDetails?>? {
        val onlineState = lastOnlineStatus[machineId]
        val detailsState = lastMessages[machineId]

        return if (onlineState != null || detailsState != null) {
            val isOnline = onlineState ?: false
            val details = detailsState
            Pair(isOnline, details)
        } else {
            null
        }
    }

//      Va etre mise a jour plus tard
//    fun requestMachineStatus(machineId: Int) {
//        if (mqttClient == null || !mqttClient!!.isConnected) {
//            Log.w(TAG, "Client MQTT non connecté, impossible d'envoyer la commande")
//            return
//        }
//
//        val commandTopic = "/scale/$machineId/commands"
//        val message = "{\"command\":\"get_status\"}"
//
//        try {
//            mqttClient?.publish(commandTopic, message.toByteArray(), 1, false)
//            Log.d(TAG, "Commande de statut envoyée à $commandTopic")
//        } catch (e: MqttException) {
//            Log.e(TAG, "Erreur lors de l'envoi de la commande à $commandTopic", e)
//        }
//    }

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