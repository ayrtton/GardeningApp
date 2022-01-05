package com.example.gardeningapp.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDAO {
    @Insert
    suspend fun insertPlant(plant: Plant): Long

    @Update
    suspend fun updatePlant(plant: Plant): Int

    @Delete
    suspend fun deletePlant(plant: Plant): Int

    @Query("DELETE FROM plant_table")
    suspend fun deleteAllPlants(): Int

    @Query("SELECT * FROM plant_table ORDER BY specie ASC")
    fun getAllPlants(): Flow<List<Plant>>

    @Query("SELECT * FROM plant_table WHERE substr(weekdays, :day, 1) = '1' ORDER BY time ASC")
    fun getPlantsByDay(day: Int): Flow<List<Plant>>

    @Query("SELECT COUNT(*) FROM plant_table WHERE substr(weekdays, :day, 1) = '1'")
    suspend fun countPlantsByDay(day: Int): Int

    @Query("SELECT DISTINCT time FROM plant_table WHERE substr(weekdays, :day, 1) = '1' ORDER BY time ASC")
    suspend fun getWateringTimesByWeekday(day: Int): List<String>
}