package com.quentinlecourt.podwertask_mobile.data.services

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.quentinlecourt.podwertask_mobile.BuildConfig
import com.quentinlecourt.podwertask_mobile.data.api.MyAPI
import com.quentinlecourt.podwertask_mobile.data.api.RetrofitInstance
import com.quentinlecourt.podwertask_mobile.data.model.CalibrationReturn
import com.quentinlecourt.podwertask_mobile.data.model.CalibrationType
import com.quentinlecourt.podwertask_mobile.data.model.Machine
import com.quentinlecourt.podwertask_mobile.data.model.MachineAutoCalibration
import com.quentinlecourt.podwertask_mobile.data.model.MachineManualCalibration
import com.quentinlecourt.podwertask_mobile.data.model.MqttError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import java.util.UUID

class MqttManager(private val context: Context) {

    private val apiService: MyAPI by lazy { RetrofitInstance.apiService }

    private val TAG = "MqttManager"
    private var mqttClient: MqttClient? = null
    private val brokerUrl = BuildConfig.MQTT_BASE_URL
    private val clientId = "PowderScaleMobile_" + UUID.randomUUID().toString()

    private val subscriptions =
        mutableMapOf<Int, String>() // Map des IDs de machines aux topics souscrits

    private val lastMessages = mutableMapOf<Int, Machine?>()
    private val lastOnlineStatus = mutableMapOf<Int, Boolean>()
    private val lastAutoCalibrationStatus = mutableMapOf<Int, MachineAutoCalibration?>()
    private val lastManualCalibrationStatus = mutableMapOf<Int, MachineManualCalibration?>()

    private var statusCallback: ((Int, Boolean, Machine?) -> Unit)? = null
    private var calibrationCallback: ((Int, CalibrationReturn) -> Unit)? = null

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
                                    if (topic != null) {
                                        val parts = topic.split("/")
                                        if (parts.size >= 3) {
                                            val machineId = parts[2].toIntOrNull()
                                            if (machineId != null) {
                                                when {
                                                    topic.contains("/status/details") -> parseDetailsStatusMessage(
                                                        machineId,
                                                        payload
                                                    )

                                                    topic.contains("/status/online") -> parseOnlineStatusMessage(
                                                        machineId,
                                                        payload
                                                    )

                                                    topic.contains("/status/calibration/") -> parseCalibrationStatusMessage(
                                                        machineId,
                                                        topic,
                                                        payload
                                                    )
                                                }
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

    fun setStatusCallback(callback: (Int, Boolean, Machine?) -> Unit) {
        this.statusCallback = callback
    }

    fun setCalibrationCallback(callback: (Int, CalibrationReturn) -> Unit) {
        this.calibrationCallback = callback
    }

    private fun parseDetailsStatusMessage(machineId: Int, statusJson: String) {
        try {
            val gson = Gson()
            val details = gson.fromJson(statusJson, Machine::class.java)

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

    private fun parseCalibrationStatusMessage(machineId: Int, topic: String, payload: String) {
        try {
            val gson = Gson()

            when {
                topic.contains("/calibration/auto") -> {
                    try {
                        val calibration = gson.fromJson(payload, MachineAutoCalibration::class.java)
                        lastAutoCalibrationStatus[machineId] = calibration
                        calibrationCallback?.invoke(
                            machineId,
                            CalibrationReturn.AutoSuccess(calibration)
                        )
                    } catch (e: Exception) {
                        handleCalibrationError(machineId, payload)
                    }
                }

                topic.contains("/calibration/manual") -> {
                    try {
                        val calibration =
                            gson.fromJson(payload, MachineManualCalibration::class.java)
                        lastManualCalibrationStatus[machineId] = calibration
                        calibrationCallback?.invoke(
                            machineId,
                            CalibrationReturn.ManualSuccess(calibration)
                        )
                    } catch (e: Exception) {
                        handleCalibrationError(machineId, payload)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du parsing du message de calibration", e)
            calibrationCallback?.invoke(
                machineId,
                CalibrationReturn.Error(MqttError("Erreur de parsing: ${e.message}"))
            )
        }
    }

    private fun handleCalibrationError(machineId: Int, payload: String) {
        try {
            val error = Gson().fromJson(payload, MqttError::class.java)
            calibrationCallback?.invoke(
                machineId,
                CalibrationReturn.Error(error)
            )
            Log.e(TAG, "Erreur de calibration pour machine $machineId: ${error.error}")
        } catch (e: Exception) {
            calibrationCallback?.invoke(
                machineId,
                CalibrationReturn.Error(MqttError("Format de réponse inconnu"))
            )
        }
    }

    fun requestCalibration(
        machineId: Int,
        type: CalibrationType
//        ,calibrationIndex: Float? = null // Optionnel, nécessaire seulement pour MANUAL
    ) {
        if (mqttClient == null || !mqttClient!!.isConnected) {
            Log.w(TAG, "Client MQTT non connecté, impossible d'envoyer la commande")
            calibrationCallback?.invoke(
                machineId,
                CalibrationReturn.Error(MqttError("Client MQTT non connecté"))
            )
            return
        }

        val commandTopic = "/scale/$machineId/commands"
        val message = buildJsonString(type, null)

        try {
            mqttClient?.publish(commandTopic, message.toByteArray(), 1, false)
            Log.d(TAG, "Commande de calibration ${type.name} envoyée à $commandTopic")
        } catch (e: MqttException) {
            Log.e(TAG, "Erreur lors de l'envoi de la commande à $commandTopic", e)
            calibrationCallback?.invoke(
                machineId,
                CalibrationReturn.Error(MqttError("Erreur MQTT: ${e.message}"))
            )
        }
    }

    private fun buildJsonString(type: CalibrationType, calibrationIndex: Float?): String {
        return when (type) {
            CalibrationType.AUTO -> "{\"command\":\"calibrate\",\"type\":\"auto\"}"
            CalibrationType.MANUAL -> "{\"command\":\"calibrate\",\"type\":\"manual\"}"
        }
    }

    // Méthodes pour récupérer le dernier état connu
    fun getLastAutoCalibrationStatus(machineId: Int): MachineAutoCalibration? {
        return lastAutoCalibrationStatus[machineId]
    }

    fun getLastManualCalibrationStatus(machineId: Int): MachineManualCalibration? {
        return lastManualCalibrationStatus[machineId]
    }

    fun subscribeToMachineTopics(machineId: Int) {
        if (mqttClient == null || !mqttClient!!.isConnected) {
            Log.w(TAG, "Client MQTT non connecté, impossible de s'abonner")
            return
        }

        val statusTopic = "/scale/$machineId/status"
        val onlineStatusTopic = "$statusTopic/online"
        val detailsStatusTopic = "$statusTopic/details"
        val calibrationAuto = "$statusTopic/calibration/auto"
        val calibrationManuel = "$statusTopic/calibration/manual"

        try {
            mqttClient?.subscribe(onlineStatusTopic, 1)
            Log.d(TAG, "Abonnement réussi à $onlineStatusTopic")
            subscriptions[machineId] = onlineStatusTopic

            mqttClient?.subscribe(detailsStatusTopic, 1)
            Log.d(TAG, "Abonnement réussi à $detailsStatusTopic")
            subscriptions[machineId] = detailsStatusTopic

            mqttClient?.subscribe(calibrationAuto, 1)
            Log.d(TAG, "Abonnement réussi à $calibrationAuto")
            subscriptions[machineId] = calibrationAuto

            mqttClient?.subscribe(calibrationManuel, 1)
            Log.d(TAG, "Abonnement réussi à $calibrationManuel")
            subscriptions[machineId] = calibrationManuel
        } catch (e: MqttException) {
            Log.e(TAG, "Erreur lors de l'abonnement à $statusTopic", e)
        }
    }

    fun getLastKnownState(machineId: Int): Pair<Boolean, Machine?>? {
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

    // TODO : requestCalibration(calibrationType -> Auto || Manu) envoie une command à la machine pour demander une calibation

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