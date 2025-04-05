package com.quentinlecourt.podwertask_mobile

import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quentinlecourt.podwertask_mobile.data.adapter.MachineAdapter
import com.quentinlecourt.podwertask_mobile.data.model.Machine

class DevicesListActivity : AppCompatActivity() {

    private lateinit var machineAdapter: MachineAdapter
    private var machines = mutableListOf<Machine>()
    private var sortByName = true // true pour tri alphabétique, false pour tri par dernière connexion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_materials_status)

        // Initialiser les données (à remplacer par vos données réelles)
        loadMachines()

        // Configurer la RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_machines)
        recyclerView.layoutManager = LinearLayoutManager(this)
        machineAdapter = MachineAdapter(machines)
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

    private fun loadMachines() {
        // Exemple de données
        machines.clear()
        machines.addAll(listOf(
            Machine(1, "Machine A", true, true, false, System.currentTimeMillis() - 3600000),
            Machine(2, "Machine B", false, false, true, System.currentTimeMillis() - 7200000),
            Machine(3, "Machine C", true, true, true, System.currentTimeMillis() - 1800000),
            Machine(4, "Machine A", true, true, false, System.currentTimeMillis() - 3600000),
            Machine(5, "Machine B", false, false, true, System.currentTimeMillis() - 7200000),
            Machine(6, "Machine C", true, true, true, System.currentTimeMillis() - 1800000)
        ))
    }

    private fun sortMachines() {
        if (sortByName) {
            machines.sortBy { it.name }
        } else {
            machines.sortByDescending { it.lastConnectionTime }
        }
        machineAdapter.notifyDataSetChanged()
    }

    private fun filterMachines() {
        val showOnline = findViewById<CheckBox>(R.id.checkBox_onlineDevice).isChecked
        val showProblem = findViewById<CheckBox>(R.id.checkBox_problemDevice).isChecked

        // Implémenter la logique de filtrage selon vos besoins
        // ...

        machineAdapter.notifyDataSetChanged()
    }
}