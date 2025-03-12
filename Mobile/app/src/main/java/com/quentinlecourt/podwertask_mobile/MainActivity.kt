package com.quentinlecourt.podwertask_mobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

//    private lateinit var mqttClient: MqttAndroidClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//
//        // Initialize MQTT Client
//        mqttClient = MqttAndroidClient(this, "tcp://broker.hivemq.com:1883", "kotlin_client")
//        connectToMqttBroker()
//
//        // Call API to get message
//        fetchMessageFromApi()
//    }
//
//    private fun connectToMqttBroker() {
//        val options = MqttConnectOptions()
//        mqttClient.connect(options, null, object : IMqttActionListener {
//            override fun onSuccess(asyncActionToken: IMqttToken?) {
//                subscribeToMqttTopic("your/topic")
//            }
//
//            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
//                // Handle failure
//            }
//        })
//    }
//
//    private fun subscribeToMqttTopic(topic: String) {
//        mqttClient.subscribe(topic, 0, null, object : IMqttActionListener {
//            override fun onSuccess(asyncActionToken: IMqttToken?) {
//                // Subscription successful
//            }
//
//            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
//                // Handle failure
//            }
//        })
//
//        mqttClient.setCallback(object : MqttCallback {
//            override fun messageArrived(topic: String?, message: MqttMessage?) {
//                runOnUiThread {
//                    findViewById<TextView>(R.id.mqttTextView).text = String(message?.payload ?: byteArrayOf())
//                }
//            }
//
//            override fun connectionLost(cause: Throwable?) {}
//            override fun deliveryComplete(token: IMqttDeliveryToken?) {}
//        })
//    }
//
//    private fun fetchMessageFromApi() {
//        // Implement Retrofit API call here
//        // Update the messageTextView with the response
    }
}