package com.example.gardeningapp

import android.app.Application
import com.example.gardeningapp.database.PlantDatabase
import com.example.gardeningapp.database.PlantRepository

class PlantsApplication: Application() {
    val database by lazy { PlantDatabase.getDatabase(this) }
    val repository by lazy { PlantRepository(database.plantDAO()) }
}