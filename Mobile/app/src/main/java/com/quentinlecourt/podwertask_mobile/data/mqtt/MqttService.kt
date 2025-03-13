import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttService : Service() {

    private lateinit var mqttClient: MqttClient

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        connectToMqttBroker()
    }

    private fun connectToMqttBroker() {
        val serverUri = "tcp://broker.hivemq.com:1883" // Remplacez par l'URI de votre broker MQTT
        val clientId = "your_client_id" // Remplacez par un ID client unique

        try {
            mqttClient = MqttClient(serverUri, clientId, MemoryPersistence())
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                connectionTimeout = 10
                keepAliveInterval = 20
            }

            mqttClient.connect(options)
            Log.d("MqttService", "Connected to MQTT broker")

            // Souscrire à un topic
            subscribeToTopic("your/topic")

        } catch (e: MqttException) {
            Log.e("MqttService", "Error connecting to MQTT broker", e)
        }
    }

    private fun subscribeToTopic(topic: String) {
        try {
            mqttClient.subscribe(topic) { _, message ->
                val payload = String(message.payload)
                Log.d("MqttService", "Received message: $payload")
                // Traitez le message reçu ici
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