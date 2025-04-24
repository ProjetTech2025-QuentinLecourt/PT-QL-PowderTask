package com.quentinlecourt.podwertask_mobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
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

    private enum class SortMode {
        A_TO_Z, Z_TO_A, LAST_CONNECTION, FIRST_CONNECTION
    }

    private var currentSortMode = SortMode.A_TO_Z

    private lateinit var mqttManager: MqttManager

    // Map pour suivre les machines filtrées actuellement affichées
    private var filteredMachines = mutableListOf<Machine>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_materials_status)

        mqttManager = MqttManager(applicationContext)

        mqttManager.connect { connected ->
            if (connected) {
                subscribeToAllMachines()
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Échec de connexion au broker MQTT", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        setupMqttStatusCallback()
        loadMachines()

        // Configurer la RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_machines)
        recyclerView.layoutManager = LinearLayoutManager(this)
        machineAdapter = MachineAdapter(filteredMachines)
        { selectedMachine ->
            val intent = Intent(this, MaterialsDetailsActivity::class.java).apply {
                putExtra("MACHINE_ID", selectedMachine.id)
                putExtra("MACHINE_NAME", selectedMachine.name)
            }
            startActivity(intent)
        }
        recyclerView.adapter = machineAdapter

        // Bouton de retour (ferme simplement l'activité)
        findViewById<ImageButton>(R.id.return_from_devicesListPage).setOnClickListener {
            finish()
        }
        // Configurer le bouton de tri
        findViewById<ImageButton>(R.id.filterImage).setOnClickListener { view ->
            showSortPopupMenu(view)
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
        mqttManager.setStatusCallback { machineId, isOnline, machineDetails ->
            val machine = machines.find { it.id == machineId }
            machine?.let {
                it.isOnline = isOnline
                it.accelerometerStatus = machineDetails?.accelerometerStatus
                it.accX = machineDetails?.accX
                it.accY = machineDetails?.accY
                it.accZ = machineDetails?.accZ
                it.accNorm = machineDetails?.accNorm
                it.weightSensorStatus = machineDetails?.weightSensorStatus
                it.weightDetected = machineDetails?.weightDetected
                it.calibrationIndex = machineDetails?.calibrationIndex
                it.display = machineDetails?.display
                it.datetimeDelivery = machineDetails?.datetimeDelivery

                runOnUiThread {
                    filterMachines()
                    machineAdapter.notifyItemChanged(filteredMachines.indexOfFirst { m -> m.id == machineId })
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
                        val newMachines = scaleResponse.data.scales.map { scaleDto ->
                            Machine(
                                id = scaleDto.id,
                                name = scaleDto.scale_name,
                                isOnline = false,
                                accelerometerStatus = null,
                                accX = null,
                                accY = null,
                                accZ = null,
                                accNorm = null,
                                weightSensorStatus = null,
                                weightDetected = null,
                                calibrationIndex = null,
                                display = null,
                                datetimeDelivery = null
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

    private fun showSortPopupMenu(anchor: View) {
        val popup = PopupMenu(this, anchor) // Utiliser 'this' si dans une Activity
        popup.menuInflater.inflate(R.menu.scale_sort_menu, popup.menu)

        when (currentSortMode) {
            SortMode.A_TO_Z -> popup.menu.findItem(R.id.sort_az).isChecked = true
            SortMode.Z_TO_A -> popup.menu.findItem(R.id.sort_za).isChecked = true
            SortMode.LAST_CONNECTION -> popup.menu.findItem(R.id.sort_last_connection).isChecked =
                true

            SortMode.FIRST_CONNECTION -> popup.menu.findItem(R.id.sort_first_connection).isChecked =
                true
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.sort_az -> {
                    currentSortMode = SortMode.A_TO_Z
                    true
                }

                R.id.sort_za -> {
                    currentSortMode = SortMode.Z_TO_A
                    true
                }

                R.id.sort_last_connection -> {
                    currentSortMode = SortMode.LAST_CONNECTION
                    true
                }

                R.id.sort_first_connection -> {
                    currentSortMode = SortMode.FIRST_CONNECTION
                    true
                }

                else -> false
            }.also { if (it) sortMachines() }
        }
        popup.show()
    }

    private fun filterMachines() {
        val showOnline = findViewById<CheckBox>(R.id.checkBox_onlineDevice).isChecked
        val showProblem = findViewById<CheckBox>(R.id.checkBox_problemDevice).isChecked

        filteredMachines.clear()
        filteredMachines.addAll(machines.filter { machine ->
            val matchesOnlineFilter = !showOnline || machine.isOnline == true

            val hasAccelerometerIssue = machine.accelerometerStatus?.let { it < 2 } ?: false
            val hasWeightSensorIssue = machine.weightSensorStatus?.let { it < 2 } ?: false
            val hasAnyProblem = hasAccelerometerIssue || hasWeightSensorIssue

            val isProblemMatch = !showProblem || hasAnyProblem

            matchesOnlineFilter && isProblemMatch
        })

        applySorting()
        machineAdapter.notifyDataSetChanged()
    }

    private fun applySorting() {
        filteredMachines.sortWith(when (currentSortMode) {
            SortMode.A_TO_Z -> compareBy { it.name }
            SortMode.Z_TO_A -> compareByDescending { it.name }
            SortMode.LAST_CONNECTION -> compareByDescending { it.datetimeDelivery }
            SortMode.FIRST_CONNECTION -> compareBy { it.datetimeDelivery }
        })
    }

    private fun sortMachines() {
        applySorting()
        machineAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        mqttManager.disconnect()
        super.onDestroy()
    }
}