package com.quentinlecourt.podwertask_mobile.data.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.quentinlecourt.podwertask_mobile.R
import com.quentinlecourt.podwertask_mobile.data.model.Machine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MachineAdapter(
    private val machines: List<Machine>,
    private val onItemClick: (Machine) -> Unit
) : RecyclerView.Adapter<MachineAdapter.MachineViewHolder>() {

    class MachineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val machineName: TextView = itemView.findViewById(R.id.tv_machine_name)
        val machineStatus: TextView = itemView.findViewById(R.id.tv_machine_status)
        val accelerometerStatus: TextView = itemView.findViewById(R.id.tv_accelerometer_status)
        val weightSensorStatus: TextView = itemView.findViewById(R.id.tv_weight_sensor_status)
        val machineTimeLayout: LinearLayout = itemView.findViewById(R.id.layout_machineTime)
        val machineTimeTextView: TextView = itemView.findViewById(R.id.tv_machineTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MachineViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.scale_card_view,
            parent,
            false
        )
        return MachineViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MachineViewHolder, position: Int) {
        val currentMachine = machines[position]

        // Définir le nom de la machine
        holder.machineName.text = currentMachine.name

        val strStable: String = holder.itemView.context.getString(R.string.stable)
        val strOnMove: String = holder.itemView.context.getString(R.string.on_move)
        val strIncoherent: String = holder.itemView.context.getString(R.string.incoherent)
        val strUnuseable: String = holder.itemView.context.getString(R.string.unusable)
        val strUnavailiable: String = holder.itemView.context.getString(R.string.unavailable)
        val strNoInfo: String = holder.itemView.context.getString(R.string.status_unknow)

        val colorOk = R.drawable.online_status
        val colorProblem = R.drawable.medium_status
        val colorPanic = R.drawable.offline_status
        val colorUnknow = R.drawable.none_status

        // Définir le statut de la machine et sa couleur d'arrière plan
        if (currentMachine.isOnline) {
            holder.machineStatus.text = holder.itemView.context.getString(R.string.online)
            holder.machineStatus.background =
                AppCompatResources.getDrawable(holder.itemView.context, R.drawable.online_status)

        } else {
            holder.machineStatus.text = holder.itemView.context.getString(R.string.offline)
            holder.machineStatus.background =
                AppCompatResources.getDrawable(holder.itemView.context, R.drawable.offline_status)
        }
        // Mise à jour des autres composants
        if (currentMachine.datetimeDelivery == null) {
            holder.machineTimeLayout.visibility = View.GONE
        } else {
            holder.machineTimeLayout.visibility = View.VISIBLE
            holder.machineTimeTextView.text = currentMachine.datetimeDelivery?.let { seconds ->
                SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    .format(Date(seconds * 1000L))
            }
        }

        holder.accelerometerStatus.text = when (currentMachine.accelerometerStatus) {
            0 -> strUnavailiable
            1 -> strOnMove
            2 -> strStable
            else -> strNoInfo
        }
        holder.accelerometerStatus.background = AppCompatResources.getDrawable(
            holder.itemView.context,
            when (currentMachine.accelerometerStatus) {
                0 -> colorPanic
                1 -> colorProblem
                2 -> colorOk
                else -> colorUnknow
            }
        )

        holder.weightSensorStatus.text = when (currentMachine.weightSensorStatus) {
            0 -> strUnuseable
            1 -> strIncoherent
            2 -> strStable
            else -> strNoInfo
        }
        holder.weightSensorStatus.background = AppCompatResources.getDrawable(
            holder.itemView.context,
            when (currentMachine.weightSensorStatus) {
                0 -> colorPanic
                1 -> colorProblem
                2 -> colorOk
                else -> colorUnknow
            }
        )
        // Ajout du clic sur l'item
        holder.itemView.setOnClickListener {
            onItemClick(currentMachine)
        }
    }

    override fun getItemCount() = machines.size
}