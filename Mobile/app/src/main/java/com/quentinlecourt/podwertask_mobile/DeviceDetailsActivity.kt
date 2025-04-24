package com.quentinlecourt.podwertask_mobile

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.quentinlecourt.podwertask_mobile.data.model.Machine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.properties.Delegates

class MaterialsDetailsActivity : AppCompatActivity() {

    // Déclaration des vues
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

    private lateinit var machine: Machine

    private lateinit var strStable: String
    private lateinit var strOnMove: String
    private lateinit var strIncoherent: String
    private lateinit var strUnuseable: String
    private lateinit var strUnavailiable: String
    private lateinit var strNoInfo: String
    private lateinit var strProblem: String

    private var colorOk by Delegates.notNull<Int>()
    private var colorProblem by Delegates.notNull<Int>()
    private var colorPanic by Delegates.notNull<Int>()
    private var colorUnknow by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_materials_details)

        // machine = intent.getParcelableExtra("MACHINE") ?: throw IllegalStateException("Machine must be provided")
        machine = intent.getParcelableExtra<Machine>("MACHINE")!!
        val isOnline = intent.getBooleanExtra("IS_ONLINE", false)

        // Initialisation des vues
        initViews()
        initString()

        // TODO : GONE si l'utilisateur est un employé
        // TODO : Afficher si l'utilisateur est un Chef d'équipe et aller chercher la liste des personnes associées dans l'API
        // associateUserListLayout.visibility = View.GONE

        // Configuration du bouton de retour
        returnButton.setOnClickListener {
            finish()
        }

        setupInitialData()
    }

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

    private fun initString() {
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

    private fun setupInitialData() {
        // TODO :Modifier la valeurs des balises si elle a changé uniquement
        deviceNameTextView.text = machine.name

        if (machine.isOnline) {
            machineStatusTextView.text = getString(R.string.online)
            machineStatusTextView.setBackgroundResource(R.drawable.online_status)
        } else {
            machineStatusTextView.text = getString(R.string.offline)
            machineStatusTextView.setBackgroundResource(R.drawable.offline_status)
        }

        // Statut global
        // TODO : Make a function who determine the global status value
        globalStatusTextView.text = "100%"
        globalStatusTextView.setBackgroundResource(R.drawable.online_status)
        if (machine.datetimeDelivery != null) {
            machineDateTimeDetailsTextView.text = machine.datetimeDelivery?.let { seconds ->
                SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    .format(Date(seconds * 1000L))
            }
        }

        // Accéléromètre
        accelerometerStatusTextView.text = when (machine.accelerometerStatus) {
            0 -> strUnavailiable
            1 -> strOnMove
            2 -> strStable
            else -> strNoInfo
        }

        accelerometerStatusTextView.setBackgroundResource (
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
        weightSensorStatusTextView.setBackgroundResource (
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
        stoneDisplayStatusTextView.text = when (machine.weightSensorStatus) {
            0 -> strUnuseable
            1 -> strProblem
            2 -> strStable
            else -> strNoInfo
        }
        stoneDisplayStatusTextView.setBackgroundResource (
            when (machine.weightSensorStatus) {
                0 -> colorPanic
                1 -> colorProblem
                2 -> colorOk
                else -> colorUnknow
            }
        )

        // Liste des utilisateurs associés
        // TODO : Recuperer une liste d'utilisateur par l'API dans le cas ou l'utilisateur actuel de l'app est un admin
        val users = listOf("Jean Dupont", "Marie Martin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, users)
        associateUserListView.adapter = adapter
    }

    // Méthode pour mettre à jour les données réelles (à appeler quand vous avez les vraies données)
//    fun updateDeviceData(deviceData: DeviceData) {
//        // Implémentez cette méthode pour mettre à jour l'UI avec les données réelles
//        // deviceData serait un objet contenant toutes les informations de la machine
//    }
}