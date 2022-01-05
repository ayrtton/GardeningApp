package com.example.gardeningapp.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.gardeningapp.R
import com.example.gardeningapp.activities.AddPlantActivity
import com.example.gardeningapp.activities.MainActivity
import com.example.gardeningapp.databinding.ItemPlantBinding
import com.example.gardeningapp.database.Plant
import com.example.gardeningapp.viewmodels.PlantsViewModel
import java.io.File

class PlantsAdapter(
    private val context: Context, private var list: MutableList<Plant>
): RecyclerView.Adapter<PlantsAdapter.PlantsViewHolder>() {
    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantsViewHolder {
        return PlantsViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_plant, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PlantsViewHolder, position: Int) {
        val plant = list[position]

        holder.binding.apply {
            ivPlant.load(File(plant.image!!)) {
                allowHardware(false)
            }
            tvSpecie.text = plant.specie
            if(plant.scientificName?.isNotEmpty() == true) {
                tvScientificName.text = plant.scientificName
                tvScientificName.visibility = View.VISIBLE
            }
        }

        holder.itemView.setOnClickListener {
            if(onClickListener != null) {
                onClickListener!!.onClick(position, plant)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddPlantActivity::class.java)
        intent.putExtra(MainActivity.PLANT_DETAILS, list[position])
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    fun removeAt(position: Int, plantViewModel: PlantsViewModel) {
        plantViewModel.delete(list[position])
        list.removeAt(position)
        notifyItemRemoved(position)
    }

    class PlantsViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val binding: ItemPlantBinding = ItemPlantBinding.bind(view)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: Plant)
    }
}