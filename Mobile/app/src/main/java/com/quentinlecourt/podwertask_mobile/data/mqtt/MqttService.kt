package com.quentinlecourt.podwertask_mobile.data.mqtt

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.quentinlecourt.podwertask_mobile.R
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

class MqttService : Service() {

    private lateinit var mqttClient: MqttClient

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        connectToMqttBroker()
    }

    private fun createSslSocketFactory(context: Context): javax.net.ssl.SSLSocketFactory {
        // Charger le certificat depuis le fichier raw
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val inputStream: InputStream = context.resources.openRawResource(R.raw.mqtt) // Fichier ca.crt
        val certificate = certificateFactory.generateCertificate(inputStream) as X509Certificate
        inputStream.close()

        // Créer un KeyStore
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        keyStore.setCertificateEntry("ca", certificate)

        // Initialiser un TrustManagerFactory avec le KeyStore
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)

        // Créer un SSLContext avec le TrustManagerFactory
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustManagerFactory.trustManagers, null)

        // Retourner la SSLSocketFactory
        return sslContext.socketFactory
    }

    private fun connectToMqttBroker() {
        val serverUri = "mqtts://mqtt.powdertask.quentinlecourt.com:8883"
        val clientId = "android-client"

        try {
            mqttClient = MqttClient(serverUri, clientId, MemoryPersistence())
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                connectionTimeout = 10
                keepAliveInterval = 20
                socketFactory = createSslSocketFactory(applicationContext)
            }

            mqttClient.connect(options)
            Log.d("MqttService", "Connected to MQTT broker")

            // Souscrire à un topic
            subscribeToTopic("test")

        } catch (e: MqttException) {
            Log.e("MqttService", "Error connecting to MQTT broker", e)
        }
    }

    private fun subscribeToTopic(topic: String) {
        try {
            mqttClient.subscribe(topic) { _, message ->
                val payload = String(message.payload)
                Log.d("MqttService", "Received message: $payload")
            }
        } catch (e: MqttException) {
            Log.e("MqttService", "Error subscribing to topic", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mqttClient.disconnect()
            Log.d("MqttService", "Disconnected from MQTT broker")
        } catch (e: MqttException) {
            Log.e("MqttService", "Error disconnecting from MQTT broker", e)
        }
    }
}