package com.quentinlecourt.podwertask_mobile.data.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.quentinlecourt.podwertask_mobile.R
import com.quentinlecourt.podwertask_mobile.data.model.Machine

class MachineAdapter(private val machines: List<Machine>) : RecyclerView.Adapter<MachineAdapter.MachineViewHolder>() {

    class MachineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val machineName: TextView = itemView.findViewById(R.id.tv_machine_name)
        val machineStatus: TextView = itemView.findViewById(R.id.tv_machine_status)
        val accelerometerStatus: TextView = itemView.findViewById(R.id.tv_accelerometer_status)
        val weightSensorStatus: TextView = itemView.findViewById(R.id.tv_weight_sensor_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MachineViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.card_view,
            parent,
            false
        )
        return MachineViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MachineViewHolder, position: Int) {
        val currentMachine = machines[position]

        // Définir le nom de la machine
        holder.machineName.text = currentMachine.name

        val componentOk: String = holder.itemView.context.getString(R.string.status_fonctional)
        val componentNotOk: String = holder.itemView.context.getString(R.string.status_problem)
        val componentNoInfo: String = holder.itemView.context.getString(R.string.status_unknow)

        // Définir le statut de la machine et sa couleur d'arrière plan
        if (currentMachine.isOnline) {
            holder.machineStatus.text = holder.itemView.context.getString(R.string.online)
            holder.machineStatus.setBackground(
                AppCompatResources.getDrawable(holder.itemView.context, R.drawable.online_status)
            )

            // Définir le statut des composants
            holder.accelerometerStatus.text = if (currentMachine.isAccelerometerWorking) componentOk else componentNotOk
            holder.accelerometerStatus.setBackground(
                AppCompatResources.getDrawable(
                    holder.itemView.context,
                    if (currentMachine.isAccelerometerWorking) R.drawable.online_status else R.drawable.offline_status
                )
            )

            holder.weightSensorStatus.text = if (currentMachine.isWeightSensorWorking) componentOk else componentNotOk
            holder.weightSensorStatus.setBackground(
                AppCompatResources.getDrawable(
                    holder.itemView.context,
                    if (currentMachine.isWeightSensorWorking) R.drawable.online_status else R.drawable.offline_status
                )
            )
        } else {
            holder.machineStatus.text = holder.itemView.context.getString(R.string.offline)
            holder.machineStatus.setBackground(
                AppCompatResources.getDrawable(holder.itemView.context, R.drawable.offline_status)
            )

            // Définir le statut des composants
            holder.accelerometerStatus.text = componentNoInfo
            holder.accelerometerStatus.setBackground(
                AppCompatResources.getDrawable(holder.itemView.context, R.drawable.none_status)
            )

            holder.weightSensorStatus.text = componentNoInfo
            holder.weightSensorStatus.setBackground(
                AppCompatResources.getDrawable(holder.itemView.context, R.drawable.none_status)
            )
        }
    }

    override fun getItemCount() = machines.size
}