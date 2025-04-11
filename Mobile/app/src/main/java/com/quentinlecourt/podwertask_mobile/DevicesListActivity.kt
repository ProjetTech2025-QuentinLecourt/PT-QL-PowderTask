package com.quentinlecourt.podwertask_mobile

import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quentinlecourt.podwertask_mobile.data.adapter.MachineAdapter
import com.quentinlecourt.podwertask_mobile.data.api.MyAPI
import com.quentinlecourt.podwertask_mobile.data.api.RetrofitInstance
import com.quentinlecourt.podwertask_mobile.data.model.Machine
import com.quentinlecourt.podwertask_mobile.data.services.MqttManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DevicesListActivity : AppCompatActivity() {

    private val apiService: MyAPI by lazy { RetrofitInstance.apiService }

    private lateinit var machineAdapter: MachineAdapter
    private var machines = mutableListOf<Machine>()
    private var sortByName = true // true pour tri alphabétique, false pour tri par dernière connexion
    private lateinit var mqttManager: MqttManager

    // Map pour suivre les machines filtrées actuellement affichées
    private var filteredMachines = mutableListOf<Machine>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_materials_status)

        // Initialiser MQTT Manager
        mqttManager = MqttManager(applicationContext)

        // Connecter au broker MQTT
        mqttManager.connect { connected ->
            if (connected) {
                // Abonner aux topics MQTT pour chaque machine
                subscribeToAllMachines()
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Échec de connexion au broker MQTT", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Configurer le callback de statut MQTT
        setupMqttStatusCallback()

        // Initialiser les données
        loadMachines()

        // Configurer la RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_machines)
        recyclerView.layoutManager = LinearLayoutManager(this)
        machineAdapter = MachineAdapter(filteredMachines)
        recyclerView.adapter = machineAdapter

        // Configurer le bouton de tri
        findViewById<ImageButton>(R.id.filterImage).setOnClickListener {
            sortByName = !sortByName
            sortMachines()
        }

        // Configurer les checkboxes de filtrage
        findViewById<CheckBox>(R.id.checkBox_onlineDevice).setOnCheckedChangeListener { _, _ ->
            filterMachines()
        }

        findViewById<CheckBox>(R.id.checkBox_problemDevice).setOnCheckedChangeListener { _, _ ->
            filterMachines()
        }
    }

    private fun setupMqttStatusCallback() {
        mqttManager.setStatusCallback { machineId, isOnline, sensors ->
            // Trouver la machine correspondante
            val machine = machines.find { it.id == machineId }
            machine?.let {
                // Mettre à jour les données de la machine
                it.isOnline = isOnline
                it.lastConnectionTime = System.currentTimeMillis()

                // Mise à jour des capteurs
                if (sensors.containsKey("accelerometer")) {
                    it.isSensorAccelerometerCorrect = sensors["accelerometer"]!!
                }
                if (sensors.containsKey("weightSensor")) {
                    it.isSensorWeightCorrect = sensors["weightSensor"]!!
                }

                // Mettre à jour l'interface utilisateur
                runOnUiThread {
                    filterMachines() // Ré-appliquer les filtres et mettre à jour la liste
                }
            }
        }
    }

    private fun subscribeToAllMachines() {
        for (machine in machines) {
            mqttManager.subscribeToMachineTopics(machine.id)
        }
    }

    private fun loadMachines() {
        lifecycleScope.launch {
            try {
                val response = apiService.getMachines()
                if (response.isSuccessful) {
                    val scaleResponse = response.body()

                    if (scaleResponse?.data?.scales != null) {  // Vérification null explicite
                        // Convertir les ScaleDto en Machine
                        val newMachines = scaleResponse.data.scales.mapNotNull { scaleDto ->
                            Machine(
                                id = scaleDto.id,
                                name = scaleDto.scale_name,
                                isOnline = false,
                                isSensorAccelerometerCorrect = null,
                                isSensorWeightCorrect = null,
                                lastConnectionTime = System.currentTimeMillis()
                            )
                        }

                        // Mettre à jour sur le thread UI
                        withContext(Dispatchers.Main) {
                            machines.clear()
                            machines.addAll(newMachines)
                            filteredMachines.clear()
                            filteredMachines.addAll(machines)
                            sortMachines()
                            subscribeToAllMachines()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@DevicesListActivity,
                                "Aucune machine disponible",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@DevicesListActivity,
                            "Erreur serveur: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DevicesListActivity,
                        "Erreur: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun sortMachines() {
        if (sortByName) {
            filteredMachines.sortBy { it.name }
        } else {
            filteredMachines.sortByDescending { it.lastConnectionTime }
        }
        machineAdapter.notifyDataSetChanged()
    }

    private fun filterMachines() {
        val showOnline = findViewById<CheckBox>(R.id.checkBox_onlineDevice).isChecked
        val showProblem = findViewById<CheckBox>(R.id.checkBox_problemDevice).isChecked

        // Filtrer les machines selon les critères
        filteredMachines.clear()
        filteredMachines.addAll(machines.filter { machine ->
            val matchesOnlineFilter = !showOnline || machine.isOnline
            val matchesProblemFilter = !showProblem || machine.isSensorAccelerometerCorrect == false || machine.isSensorWeightCorrect == false
            matchesOnlineFilter && matchesProblemFilter
        })

        // Appliquer le tri actuel
        if (sortByName) {
            filteredMachines.sortBy { it.name }
        } else {
            filteredMachines.sortByDescending { it.lastConnectionTime }
        }

        machineAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Déconnecter proprement le client MQTT
        mqttManager.disconnect()
    }
}