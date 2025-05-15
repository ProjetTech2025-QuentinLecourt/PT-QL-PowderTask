package com.quentinlecourt.podwertask_mobile

/**
 * Activité de mesure d'une balance connectée
 *
 * @todoForUpgrade
 *
 *
 * @version 1.0
 * @versionDate 2025/05/06 23:00
 * @author Quentin Lecourt
 * @project Powder Scale Systems
 */

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quentinlecourt.podwertask_mobile.data.dialog.LoadingDialog
import com.quentinlecourt.podwertask_mobile.data.model.CalibrationReturn
import com.quentinlecourt.podwertask_mobile.data.model.CalibrationType
import com.quentinlecourt.podwertask_mobile.data.model.Machine
import com.quentinlecourt.podwertask_mobile.data.model.MachineAutoCalibration
import com.quentinlecourt.podwertask_mobile.data.model.MachineManualCalibration
import com.quentinlecourt.podwertask_mobile.data.model.MqttError
import com.quentinlecourt.podwertask_mobile.data.model.StatusType
import com.quentinlecourt.podwertask_mobile.data.services.MqttManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.properties.Delegates

class MeasurementActivity : AppCompatActivity() {

    /************** Machines Section **************/
    private lateinit var machine: Machine
    private var machineId by Delegates.notNull<Int>()
    private lateinit var machineName: String

    /************** Balises **************/
    private lateinit var tvMasse: TextView
    private lateinit var tvErrorMessage: TextView
    private lateinit var tvDateTimeCalibration: TextView
    private lateinit var btnAutoCButton: Button
    private lateinit var btnManualCButton: Button

    /************** Others **************/
    private lateinit var mqttManager: MqttManager

    private var calibrationInitialize by Delegates.notNull<Boolean>()

    private var calibrationLoadingStatus by Delegates.notNull<Boolean>()

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measurement)
        calibrationInitialize = false
        calibrationLoadingStatus = false


        tvMasse = findViewById(R.id.tv_weightValue)
        tvErrorMessage = findViewById(R.id.tv_error_message)
        tvDateTimeCalibration = findViewById(R.id.tv_last_calibration_datetime)

        loadingDialog = LoadingDialog(this, this)

        machineId = 1
        machineName = "PowderScale - 01"

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
            setupMqttCallbacks()

        } else {
            Toast.makeText(
                this,
                "Impossible de récupérer les données de la machine. Identifiant inccorect.",
                Toast.LENGTH_LONG
            )
                .show()
        }

        calibrationLoadingStatus = false

        // TODO : Afficher en local l'historique de pesée


        // Initialisation du Spinner
        val spinner: Spinner = findViewById(R.id.spinner)

        // Création d'une liste fictive de balances
        val balanceList = listOf(
            machine.name
        )

        // Création de l'adapteur
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,  // Layout par défaut
            balanceList
        )

        // Layout pour la liste déroulante
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Liaison de l'adapteur au Spinner
        spinner.adapter = adapter

        // Gestion de la sélection (optionnel)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Tu pourras ajouter du code ici plus tard
                val selectedBalance = balanceList[position]
                Log.d("Spinner", "Balance sélectionnée: $selectedBalance")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Ne rien faire
            }
        }

        val returnButton: ImageButton = findViewById(R.id.return_from_measurement)
        returnButton.setOnClickListener {
            finish()
        }

        btnAutoCButton = findViewById(R.id.btn_auto_calibrate)
        btnAutoCButton.setOnClickListener {
            if (calibrationLoadingStatus) return@setOnClickListener

            loadingDialog = LoadingDialog(this, this).apply {
                setMessage("Demande de calibrage envoyée...")
                show()
                calibrationLoadingStatus = true
            }

            // Envoyer la demande de calibration via MQTT
            mqttManager.requestCalibration(machineId, CalibrationType.AUTO)

            lifecycleScope.launch {
                delay(2000)
                loadingDialog.animateProgressColor(getColor(R.color.rouge_20))
                delay(2000)
                loadingDialog.animateProgressColor(getColor(R.color.rouge_40))
                delay(2000)
                loadingDialog.animateProgressColor(getColor(R.color.rouge_60))
                delay(2000)// 2 secondes de timeout
                loadingDialog.animateProgressColor(getColor(R.color.rouge_80))
                delay(2000)
                if (calibrationLoadingStatus) {
                    loadingDialog.setStatus(StatusType.Failed)
                    calibrationLoadingStatus = false
                    Toast.makeText(
                        this@MeasurementActivity,
                        "Pas de réponse de la balance",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        btnManualCButton = findViewById(R.id.btn_manual_calibrate)
        btnManualCButton.visibility = View.GONE
        btnManualCButton.setOnClickListener {
            if (calibrationLoadingStatus) return@setOnClickListener

            loadingDialog = LoadingDialog(this, this).apply {
                setMessage("Demande de calibrage manuelle envoyée...")
                show()
                calibrationLoadingStatus = true
            }

            mqttManager.requestCalibration(machineId, CalibrationType.MANUAL)

            lifecycleScope.launch {
                delay(2000)
                loadingDialog.animateProgressColor(getColor(R.color.rouge_20))
                delay(2000)
                loadingDialog.animateProgressColor(getColor(R.color.rouge_40))
                delay(2000)
                loadingDialog.animateProgressColor(getColor(R.color.rouge_60))
                delay(2000)// 2 secondes de timeout
                loadingDialog.animateProgressColor(getColor(R.color.rouge_80))
                delay(2000)
                if (calibrationLoadingStatus) {
                    loadingDialog.setStatus(StatusType.Failed)
                    calibrationLoadingStatus = false
                    Toast.makeText(
                        this@MeasurementActivity,
                        "Pas de réponse de la balance",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        btnAutoCButton.isEnabled = false
        btnManualCButton.isEnabled = false
        calibrationInitialize = false
        tvMasse.text = "???"
        tvErrorMessage.visibility = View.VISIBLE
        tvErrorMessage.text = "Balance hors ligne"
        tvDateTimeCalibration.text = "???"
    }

    /**
     * Méthode de mise en place du callback du MQTT
     * Ecoute sur le broker et met à jour les détails de la balance et du calibrage
     */
    private fun setupMqttCallbacks() {
        // Callback existant pour les status
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
                    updateData()
                }
            }
        }

        // Nouveau callback pour les calibrations
        mqttManager.setCalibrationCallback { machineId, result ->
            runOnUiThread {
                when (result) {
                    is CalibrationReturn.AutoSuccess -> {
                        handleAutoCalibrationSuccess(result.data)
                    }

                    is CalibrationReturn.ManualSuccess -> {
                        handleManualCalibrationSuccess(result.data)
                    }

                    is CalibrationReturn.Error -> {
                        handleCalibrationError(result.error)
                    }
                }
            }
        }
    }

    private fun handleAutoCalibrationSuccess(data: MachineAutoCalibration) {
        // Mettre à jour l'UI avec le succès de calibration auto
        var message = "Calibration auto réussie"
        if (!calibrationInitialize) {
            message = message + "\n Initialisation terminée"
        }
        if (calibrationLoadingStatus) {
            calibrationLoadingStatus = false
            loadingDialog.setStatus(StatusType.Success, message)
        } else {
            loadingDialog.setStatus(
                StatusType.Success,
                message + "\n Depuis la balance.",
                rightNow = true
            )
        }
        calibrationInitialize = true
        val formattedDate = formatTimestamp(data.datetime)
        tvDateTimeCalibration.text = formattedDate + " (Automatique)"
    }

    private fun handleManualCalibrationSuccess(data: MachineManualCalibration) {
        // Mettre à jour l'UI avec le succès de calibration manuelle
        var message = "Calibration manuelle réussie (Index: ${data.calibrationIndex})"
        if (!calibrationInitialize) {
            message = message + "\n Initialisation terminée"
        }
        if (calibrationLoadingStatus) {
            calibrationLoadingStatus = false
            loadingDialog.setStatus(StatusType.Success, message)
        } else {
            loadingDialog.setStatus(
                StatusType.Success,
                message + "\n Depuis la balance.",
                rightNow = true
            )
        }
        calibrationInitialize = true
        val formattedDate = formatTimestamp(data.datetime)
        tvDateTimeCalibration.text = formattedDate + " (Manuel)"
    }

    private fun handleCalibrationError(error: MqttError) {
        val message = "Erreur de calibration: ${error.error}"
        if (calibrationLoadingStatus) {
            calibrationLoadingStatus = false
            error.error?.let { loadingDialog.setStatus(StatusType.Failed, it) }
            Toast.makeText(this, "Erreur de calibration: ${error.error}", Toast.LENGTH_LONG).show()
            loadingDialog.setStatus(StatusType.Failed, message)
        } else {
            loadingDialog.setStatus(StatusType.Failed, message, rightNow = true)
            Toast.makeText(this, "Erreur de calibration: ${error.error}", Toast.LENGTH_LONG).show()
        }
    }

    private fun formatTimestamp(timestamp: Long?): String {
        if (timestamp == null) return "N/A"

        try {
            val date = Date(timestamp * 1000) // Convertir en millisecondes
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            return format.format(date)
        } catch (e: Exception) {
            Log.e("DateTimeFormat", "Erreur de formatage", e)
            return "Format invalide"
        }
    }

    private fun updateData() {
        runOnUiThread {
            if (machine.isOnline == true) {
                btnAutoCButton.isEnabled = true
                btnManualCButton.isEnabled = true
                if (!calibrationInitialize) {
                    if (calibrationLoadingStatus) return@runOnUiThread
                    loadingDialog = LoadingDialog(this, this).apply {
                        setMessage("Calibrage initial de la balance...")
                        show()
                        calibrationLoadingStatus = true
                    }
                    // Envoyer la demande de calibration via MQTT
                    mqttManager.requestCalibration(machineId, CalibrationType.AUTO)

                    lifecycleScope.launch {
                        delay(2000)
                        loadingDialog.animateProgressColor(getColor(R.color.rouge_20))
                        delay(2000)
                        loadingDialog.animateProgressColor(getColor(R.color.rouge_40))
                        delay(2000)
                        loadingDialog.animateProgressColor(getColor(R.color.rouge_60))
                        delay(2000)// 2 secondes de timeout
                        loadingDialog.animateProgressColor(getColor(R.color.rouge_80))
                        delay(2000)
                        if (calibrationLoadingStatus) {
                            loadingDialog.setStatus(StatusType.Failed)
                            calibrationLoadingStatus = false
                            Toast.makeText(
                                this@MeasurementActivity,
                                "Pas de réponse de la balance",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                tvMasse.text = machine.weightDetected.toString()
                tvErrorMessage.visibility = View.INVISIBLE
            } else {
                btnAutoCButton.isEnabled = false
                btnManualCButton.isEnabled = false
                calibrationInitialize = false
                tvMasse.text = "???"
                tvErrorMessage.visibility = View.VISIBLE
                tvErrorMessage.text = "Balance hors ligne"
                tvDateTimeCalibration.text = "???"
            }
        }
    }
}