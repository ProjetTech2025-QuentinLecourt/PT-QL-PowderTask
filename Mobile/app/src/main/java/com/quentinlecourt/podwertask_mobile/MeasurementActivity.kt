package com.quentinlecourt.podwertask_mobile

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quentinlecourt.podwertask_mobile.data.dialog.LoadingDialog
import com.quentinlecourt.podwertask_mobile.data.model.StatusType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class MeasurementActivity : AppCompatActivity() {

    private var calibrationLoadingStatus by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measurement)

        calibrationLoadingStatus = false

        // Initialisation du Spinner
        val spinner: Spinner = findViewById(R.id.spinner)

        // Création d'une liste fictive de balances
        val balanceList = listOf(
            "Balance LAB-100 (Salle A12)",
            "Balance MX-500 (Labo Chimie)",
            "Balance PrecisionPro (Entrepôt)",
            "Balance Portable BT-200",
            "Balance Industrielle HD-3000"
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

        val autoCButton: Button = findViewById(R.id.btn_auto_calibrate)
        autoCButton.setOnClickListener {
            val loadingDialog = LoadingDialog(this, this).apply {
                setMessage("Demande de calibrage envoyée ...")
                show()
                calibrationLoadingStatus = true
            }

            lifecycleScope.launch {
                delay(3000)
                loadingDialog.setMessage("Calibrage...")
                loadingDialog.animateProgressColor(getColor(R.color.color_status_medium))
                delay(3000)
                loadingDialog.setStatus(StatusType.Failed)
                delay(3000)
                loadingDialog.setStatus(
                    status = StatusType.Success,
                    rightNow = true
                )
                calibrationLoadingStatus = false
            }
        }


    }
}