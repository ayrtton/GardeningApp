package com.example.gardeningapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Plant::class], version = 2, exportSchema = false)
abstract class PlantDatabase: RoomDatabase() {
    abstract fun plantDAO(): PlantDAO

    companion object {
        @Volatile
        private var INSTANCE: PlantDatabase? = null

        fun getDatabase(context: Context): PlantDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, PlantDatabase::class.java, "plant_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}