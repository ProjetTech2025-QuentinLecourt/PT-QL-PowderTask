package com.quentinlecourt.podwertask_mobile

/**
 * Activité des détails de la machine/balance
 *
 * @todoForUpgrade
 * @todo Mettre en place en MVVM + Data Biding
 * @todo Ne mettr à jour que lorsqu'un attribut est modifié
 *
 * @version 1.0
 * @versionDate 2025/04/21 16:00
 * @author Quentin Lecourt
 * @project Powder Scale Systems
 */

import SessionManager
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quentinlecourt.podwertask_mobile.data.services.MqttManager
import com.quentinlecourt.podwertask_mobile.data.model.Machine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.properties.Delegates
import com.google.gson.Gson
import com.quentinlecourt.podwertask_mobile.data.api.MyAPI
import com.quentinlecourt.podwertask_mobile.data.api.RetrofitInstance
import kotlinx.coroutines.launch

class MaterialsDetailsActivity : AppCompatActivity() {

    /************** Views Section **************/
    private lateinit var returnButton: ImageButton
    private lateinit var deviceNameTextView: TextView
    private lateinit var machineStatusTextView: TextView
    private lateinit var globalStatusTextView: TextView
    private lateinit var machineDateTimeDetailsTextView: TextView
    private lateinit var accelerometerStatusTextView: TextView
    private lateinit var accelerometerXDetailsTextView: TextView
    private lateinit var accelerometerYDetailsTextView: TextView
    private lateinit var accelerometerZDetailsTextView: TextView
    private lateinit var accelerometerNormDetailsTextView: TextView
    private lateinit var weightSensorStatusTextView: TextView
    private lateinit var weightValueStatusTextView: TextView
    private lateinit var calibrationIndexTextView: TextView
    private lateinit var stoneDisplayStatusTextView: TextView
    private lateinit var associateUserListView: ListView
    private lateinit var associateUserListLayout: LinearLayout

    /************** Machines Section **************/
    private lateinit var machine: Machine
    private var machineId by Delegates.notNull<Int>()
    private lateinit var machineName: String

    /************** API et session **************/
    private val apiService: MyAPI by lazy { RetrofitInstance.apiService }
    private lateinit var sessionManager: SessionManager
    private lateinit var userJob: String

    /************** String **************/
    private lateinit var strStable: String
    private lateinit var strOnMove: String
    private lateinit var strIncoherent: String
    private lateinit var strUnuseable: String
    private lateinit var strUnavailiable: String
    private lateinit var strNoInfo: String
    private lateinit var strProblem: String

    /************** Colors **************/
    private var colorOk by Delegates.notNull<Int>()
    private var colorProblem by Delegates.notNull<Int>()
    private var colorPanic by Delegates.notNull<Int>()
    private var colorUnknow by Delegates.notNull<Int>()

    /************** Others **************/
    private var userListDownloaded by Delegates.notNull<Boolean>()
    private lateinit var mqttManager: MqttManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_materials_details)

        // Sessions
        sessionManager = SessionManager(this)
        userJob = sessionManager.fetchAuthToken()?.decodeJwt()?.get("job").toString()

        // Récupération de l'id et du nom de la balance
        machineId = intent.getIntExtra("MACHINE_ID", -1)
        machineName = intent.getStringExtra("MACHINE_NAME").toString()

        machine = Machine(
            id = machineId,
            name = machineName,
            isOnline = null,
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
        userListDownloaded = false

        mqttManager = MqttManager(applicationContext)
        if (machineId != -1) {
            mqttManager.connect { connected ->
                if (connected) {
                    mqttManager.subscribeToMachineTopics(machineId)
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Échec de connexion au broker MQTT",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
            setupMqttStatusCallback()

            initViews()
            initConst()

            setupData()

        } else {
            Toast.makeText(
                this,
                "Impossible de récupérer les données de la machine. Identifiant inccorect.",
                Toast.LENGTH_LONG
            )
                .show()
        }
        // Configuration du bouton de retour
        returnButton.setOnClickListener {
            finish()
        }
    }

    /**
     * Méthode de mise en place du callback du MQTT
     * Ecoute sur le broker et met à jour les détails de la balance
     */
    private fun setupMqttStatusCallback() {
        mqttManager.setStatusCallback { machineId, isOnline, machineDetails ->
            machine.let {
                machine.isOnline = isOnline
                machine.accelerometerStatus = machineDetails?.accelerometerStatus
                machine.accX = machineDetails?.accX
                machine.accY = machineDetails?.accY
                machine.accZ = machineDetails?.accZ
                machine.accNorm = machineDetails?.accNorm
                machine.weightSensorStatus = machineDetails?.weightSensorStatus
                machine.weightDetected = machineDetails?.weightDetected
                machine.calibrationIndex = machineDetails?.calibrationIndex
                machine.display = machineDetails?.display
                machine.datetimeDelivery = machineDetails?.datetimeDelivery

                runOnUiThread {
                    // Mise à jour des éléments affichés
                    setupData()
                }
            }
        }
    }

    // Initialisation des éléments
    private fun initViews() {
        returnButton = findViewById(R.id.return_from_deviceDetails)
        deviceNameTextView = findViewById(R.id.tv_deviceDetails)
        machineStatusTextView = findViewById(R.id.tv_machine_status)
        globalStatusTextView = findViewById(R.id.tv_globalStatus_status)
        machineDateTimeDetailsTextView = findViewById(R.id.tv_machineDateTime_details)
        accelerometerStatusTextView = findViewById(R.id.tv_accelerometerStatus_status)
        accelerometerXDetailsTextView = findViewById(R.id.tv_accelerometerStatusX_details)
        accelerometerYDetailsTextView = findViewById(R.id.tv_accelerometerStatusY_details)
        accelerometerZDetailsTextView = findViewById(R.id.tv_accelerometerStatusZ_details)
        accelerometerNormDetailsTextView = findViewById(R.id.tv_accelerometerStatusNorm_details)
        weightSensorStatusTextView = findViewById(R.id.tv_weightSensorStatus_status)
        weightValueStatusTextView = findViewById(R.id.tv_weightValueStatus)
        calibrationIndexTextView = findViewById(R.id.tv_calibrationIndex)
        stoneDisplayStatusTextView = findViewById(R.id.tv_stoneDisplayStatus_status)
        associateUserListView = findViewById(R.id.list_associateUserToThisDevice)
        associateUserListLayout = findViewById(R.id.linearLayout_associateUserToThisDevice)
    }

    // Initialisation des constantes string et color
    private fun initConst() {
        strStable = getString(R.string.stable)
        strOnMove = getString(R.string.on_move)
        strIncoherent = getString(R.string.incoherent)
        strUnuseable = getString(R.string.unusable)
        strUnavailiable = getString(R.string.unavailable)
        strNoInfo = getString(R.string.status_unknow)
        strProblem = getString(R.string.problem)

        colorOk = R.drawable.online_status
        colorProblem = R.drawable.medium_status
        colorPanic = R.drawable.offline_status
        colorUnknow = R.drawable.none_status
    }

    // Affiche les attributs de la balance dans les éléments de la vue
    private fun setupData() {
        // TODO :Modifier la valeurs des balises si elle a changé uniquement
        deviceNameTextView.text = machine.name

        if (machine.isOnline == true) {
            machineStatusTextView.text = getString(R.string.online)
            machineStatusTextView.setBackgroundResource(R.drawable.online_status)
        } else {
            machineStatusTextView.text = getString(R.string.offline)
            machineStatusTextView.setBackgroundResource(R.drawable.offline_status)
        }

        // Statut global
        // TODO : Faire une fonction qui détermine la valeur du status global (systemes de points)
        globalStatusTextView.text = "100%"
        globalStatusTextView.setBackgroundResource(R.drawable.online_status)
        if (machine.datetimeDelivery != null) {
            machineDateTimeDetailsTextView.text = machine.datetimeDelivery?.let { seconds ->
                SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    .format(Date(seconds * 1000L))
            }
        }

        // Accéléromètre
        // TODO: Voir une pour version améliorer, code qui se répete
        accelerometerStatusTextView.text = when (machine.accelerometerStatus) {
            0 -> strUnavailiable
            1 -> strOnMove
            2 -> strStable
            else -> strNoInfo
        }
        accelerometerStatusTextView.setBackgroundResource(
            when (machine.accelerometerStatus) {
                0 -> colorPanic
                1 -> colorProblem
                2 -> colorOk
                else -> colorUnknow
            }
        )
        accelerometerXDetailsTextView.text = machine.accX.toString()
        accelerometerYDetailsTextView.text = machine.accY.toString()
        accelerometerZDetailsTextView.text = machine.accZ.toString()
        accelerometerNormDetailsTextView.text = machine.accNorm.toString()

        // Capteur de poids
        weightSensorStatusTextView.text = when (machine.weightSensorStatus) {
            0 -> strUnuseable
            1 -> strIncoherent
            2 -> strStable
            else -> strNoInfo
        }
        weightSensorStatusTextView.setBackgroundResource(
            when (machine.weightSensorStatus) {
                0 -> colorPanic
                1 -> colorProblem
                2 -> colorOk
                else -> colorUnknow
            }
        )
        weightValueStatusTextView.text = machine.weightDetected.toString()
        calibrationIndexTextView.text = machine.calibrationIndex.toString()

        // Affichage pour l'écran
        stoneDisplayStatusTextView.text = when (machine.display) {
            0 -> strUnuseable
            1 -> strProblem
            2 -> strStable
            else -> strNoInfo
        }
        stoneDisplayStatusTextView.setBackgroundResource(
            when (machine.display) {
                0 -> colorPanic
                1 -> colorProblem
                2 -> colorOk
                else -> colorUnknow
            }
        )

        // Liste des utilisateurs
        if (!userListDownloaded) {
            if (userJob == "CE") {
                associateUserListLayout.visibility = View.VISIBLE
                fetchUsersForMachine()
            } else {
                associateUserListLayout.visibility = View.GONE
            }
        }

    }

    // Appel à l'API pour la liste des utilisateurs liés à la balance
    private fun fetchUsersForMachine() {
        lifecycleScope.launch {
            try {
                val response = apiService.getUsersByMachineId(machineId)
                if (response.isSuccessful) {
                    val users = response.body()?.users ?: emptyList()
                    val adapter = ArrayAdapter(
                        this@MaterialsDetailsActivity,
                        android.R.layout.simple_list_item_1,
                        users.map { "${it.first_name} ${it.last_name}" }
                    )
                    associateUserListView.adapter = adapter
                    userListDownloaded = true
                } else {
                    Toast.makeText(
                        this@MaterialsDetailsActivity,
                        "Erreur lors de la récupération des utilisateurs: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MaterialsDetailsActivity,
                    "Erreur réseau: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Todo : Mettre cette fonction dans un useful ou ailleurs
    private fun String.decodeJwt(): Map<String, String>? {
        return try {
            val payload = this.split(".")[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val decodedString = String(decodedBytes, Charsets.UTF_8)
            Gson().fromJson(decodedString, Map::class.java) as? Map<String, String>
        } catch (e: Exception) {
            null
        }
    }
}