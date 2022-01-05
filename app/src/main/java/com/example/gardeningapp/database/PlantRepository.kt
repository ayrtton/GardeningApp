package com.example.gardeningapp.database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class PlantRepository(private val plantDAO: PlantDAO) {
    val allPlants = plantDAO.getAllPlants()

    @WorkerThread
    fun getPlantsByDay(day: Int): Flow<List<Plant>> {
        return plantDAO.getPlantsByDay(day)
    }

    @WorkerThread
    suspend fun getWateringTimesByWeekday(day: Int): List<String> {
        return plantDAO.getWateringTimesByWeekday(day)
    }

    @WorkerThread
    suspend fun countPlantsByDay(day: Int): Int {
        return plantDAO.countPlantsByDay(day)
    }

    @WorkerThread
    suspend fun insert(plant: Plant): Long {
        return plantDAO.insertPlant(plant)
    }

    @WorkerThread
    suspend fun update(plant: Plant): Int {
        return plantDAO.updatePlant(plant)
    }

    @WorkerThread
    suspend fun delete(plant: Plant): Int {
        return plantDAO.deletePlant(plant)
    }

    @WorkerThread
    suspend fun deleteAll(): Int {
        return plantDAO.deleteAllPlants()
    }
}

