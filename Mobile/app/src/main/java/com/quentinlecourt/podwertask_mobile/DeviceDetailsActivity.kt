package com.quentinlecourt.podwertask_mobile

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MaterialsDetailsActivity : AppCompatActivity() {

    // Déclaration des vues
    private lateinit var returnButton: ImageButton
    private lateinit var deviceNameTextView: TextView
    private lateinit var machineStatusTextView: TextView
    private lateinit var globalStatusTextView: TextView
    private lateinit var globalStatusStatusTextView: TextView
    private lateinit var accelerometerStatusTextView: TextView
    private lateinit var accelerometerStatusStatusTextView: TextView
    private lateinit var accelerometerXTextView: TextView
    private lateinit var accelerometerXDetailsTextView: TextView
    private lateinit var accelerometerYTextView: TextView
    private lateinit var accelerometerYDetailsTextView: TextView
    private lateinit var accelerometerZTextView: TextView
    private lateinit var accelerometerZDetailsTextView: TextView
    private lateinit var accelerometerNormTextView: TextView
    private lateinit var accelerometerNormDetailsTextView: TextView
    private lateinit var weightSensorStatusTextView: TextView
    private lateinit var weightSensorStatusStatusTextView: TextView
    private lateinit var weightValueStatusTextView: TextView
    private lateinit var calibrationIndexTextView: TextView
    private lateinit var stoneDisplayStatusTextView: TextView
    private lateinit var stoneDisplayStatusStatusTextView: TextView
    private lateinit var associateUserListView: ListView
    private lateinit var associateUserListLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_materials_details)

        val machineName = intent.getStringExtra("MACHINE_NAME") ?: "Nom inconnu"
        val isOnline = intent.getBooleanExtra("IS_ONLINE", false)

        // Initialisation des vues
        initViews()

        deviceNameTextView.text = machineName

        if (isOnline) {
            machineStatusTextView.text = getString(R.string.online)
            machineStatusTextView.setBackgroundResource(R.drawable.online_status)
        } else {
            machineStatusTextView.text = getString(R.string.offline)
            machineStatusTextView.setBackgroundResource(R.drawable.offline_status)
        }

        // TODO : GONE si l'utilisateur est un employé
        // TODO : Afficher si l'utilisateur est un Chef d'équipe et aller chercher la liste des personnes associées dans l'API
        // associateUserListLayout.visibility = View.GONE

        // Configuration du bouton de retour
        returnButton.setOnClickListener {
            finish()
        }

        // Exemple de données pour démonstration
        //setupSampleData()
    }

    private fun initViews() {
        returnButton = findViewById(R.id.return_from_deviceDetails)
        deviceNameTextView = findViewById(R.id.tv_deviceDetails)
        machineStatusTextView = findViewById(R.id.tv_machine_status)
        globalStatusTextView = findViewById(R.id.tv_globalStatus)
        globalStatusStatusTextView = findViewById(R.id.tv_globalStatus_status)
        accelerometerStatusTextView = findViewById(R.id.tv_accelerometerStatus)
        accelerometerStatusStatusTextView = findViewById(R.id.tv_accelerometerStatus_status)
        accelerometerXTextView = findViewById(R.id.tv_accelerometerStatusX)
        accelerometerXDetailsTextView = findViewById(R.id.tv_accelerometerStatusX_details)
        accelerometerYTextView = findViewById(R.id.tv_accelerometerStatusY)
        accelerometerYDetailsTextView = findViewById(R.id.tv_accelerometerStatusY_details)
        accelerometerZTextView = findViewById(R.id.tv_accelerometerStatusZ)
        accelerometerZDetailsTextView = findViewById(R.id.tv_accelerometerStatusZ_details)
        accelerometerNormTextView = findViewById(R.id.tv_accelerometerStatusNorm)
        accelerometerNormDetailsTextView = findViewById(R.id.tv_accelerometerStatusNorm_details)
        weightSensorStatusTextView = findViewById(R.id.tv_weightSensorStatus)
        weightSensorStatusStatusTextView = findViewById(R.id.tv_weightSensorStatus_status)
        weightValueStatusTextView = findViewById(R.id.tv_weightValueStatus)
        calibrationIndexTextView = findViewById(R.id.tv_calibrationIndex)
        stoneDisplayStatusTextView = findViewById(R.id.tv_stoneDisplayStatus)
        stoneDisplayStatusStatusTextView = findViewById(R.id.tv_stoneDisplayStatus_status)
        associateUserListView = findViewById(R.id.list_associateUserToThisDevice)
        associateUserListLayout = findViewById(R.id.linearLayout_associateUserToThisDevice)
    }

    private fun setupSampleData() {
        // TODO : Récupération des détails par le broker
        // TODO : gérer les déconnexion d'une balance
        deviceNameTextView.text = "Powder Scale - 01"
        machineStatusTextView.text = getString(R.string.online)
        machineStatusTextView.setBackgroundResource(R.drawable.online_status)

        // Statut global
        globalStatusStatusTextView.text = "100%"
        globalStatusStatusTextView.setBackgroundResource(R.drawable.online_status)

        // Accéléromètre
        accelerometerStatusStatusTextView.text = getString(R.string.on_move)
        accelerometerStatusStatusTextView.setBackgroundResource(R.drawable.medium_status)
        accelerometerXDetailsTextView.text = "-0.12"
        accelerometerYDetailsTextView.text = "-0.05"
        accelerometerZDetailsTextView.text = "-1.02"
        accelerometerNormDetailsTextView.text = "-1.03"

        // Capteur de poids
        weightSensorStatusStatusTextView.text = getString(R.string.incoherent)
        weightSensorStatusStatusTextView.setBackgroundResource(R.drawable.medium_status) // Supposons que vous avez ce drawable
        weightValueStatusTextView.text = "2.5 kg"
        calibrationIndexTextView.text = "0.98"

        // Affichage pour l'écran
        stoneDisplayStatusStatusTextView.text = getString(R.string.problem)
        stoneDisplayStatusStatusTextView.setBackgroundResource(R.drawable.offline_status)

        // Liste des utilisateurs associés
        val users = listOf("Jean Dupont", "Marie Martin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, users)
        associateUserListView.adapter = adapter
    }

    // Méthode pour mettre à jour les données réelles (à appeler quand vous avez les vraies données)
    fun updateDeviceData(deviceData: DeviceData) {
        // Implémentez cette méthode pour mettre à jour l'UI avec les données réelles
        // deviceData serait un objet contenant toutes les informations de la machine
    }
}

// Classe exemple pour les données de la machine
data class DeviceData(
    val name: String,
    val isOnline: Boolean,
    val globalStatus: String,
    val accelerometerData: AccelerometerData,
    val weightData: WeightData,
    val stoneDisplayStatus: String,
    val associatedUsers: List<String>
)

data class AccelerometerData(
    val status: String,
    val x: String,
    val y: String,
    val z: String,
    val norm: String
)

data class WeightData(
    val status: String,
    val value: String,
    val calibrationIndex: String
)