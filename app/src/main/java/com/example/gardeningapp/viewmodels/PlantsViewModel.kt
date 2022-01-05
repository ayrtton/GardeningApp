package com.example.gardeningapp.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.*
import com.example.gardeningapp.database.Plant
import com.example.gardeningapp.database.PlantRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

class PlantsViewModel(private val repository: PlantRepository): ViewModel() {
    val allPlants: LiveData<List<Plant>> = repository.allPlants.asLiveData()

    fun getPlantsByDay(day: Int): LiveData<List<Plant>> {
        return if(day == 0)
            repository.allPlants.asLiveData()
        else
            repository.getPlantsByDay(day).asLiveData()
    }

    private suspend fun countPlantsByDay(day: Int): Int {
        return repository.countPlantsByDay(day)
    }

    fun insert(plant: Plant) = viewModelScope.launch {
        repository.insert(plant)
    }

    fun update(plant: Plant) = viewModelScope.launch {
        repository.update(plant)
    }

    fun delete(plant: Plant) = viewModelScope.launch {
        repository.delete(plant)
    }

    @SuppressLint("SimpleDateFormat")
    fun timeStringToMs(timeString: String): Long {
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT+0")
        val formattedTimeString = simpleDateFormat.parse(timeString)
        val timeMs = formattedTimeString?.time!!

        return timeMs
    }

    private fun getWateringTimesByWeekday(weekday: Int): List<String> {
        var wateringTimes = listOf<String>()

        runBlocking {
            launch {
                wateringTimes = repository.getWateringTimesByWeekday(weekday)
            }
        }

        return wateringTimes
    }

    private fun getWateringWeekdays(): CharArray {
        val wateringWeekdays = CharArray(7) {'0'}

        runBlocking {
            launch {
                for(day in 1..7) {
                    if (countPlantsByDay(day) > 0) wateringWeekdays[day-1] = '1'
                }
            }
        }

        return wateringWeekdays
    }

    private fun getWateringWeekTimesMs(): MutableList<Long> {
        val wateringWeekTimesMs = mutableListOf<Long>()
        var wateringTimes: List<String>

        val wateringWeekdays = getWateringWeekdays()
        wateringWeekdays.forEachIndexed { weekday, isSet ->
            if(isSet == '1') {
                wateringTimes = getWateringTimesByWeekday(weekday + 1)
                for(wateringTime in wateringTimes) {
                    wateringWeekTimesMs.add(timeStringToMs(wateringTime) + (weekday * 86400000))
                }
            }
        }

        return wateringWeekTimesMs
    }

    fun getNextReminderTimeMs(): Long {
        val calendar = Calendar.getInstance()
        val currentWeekDay = calendar.get(Calendar.DAY_OF_WEEK)
        val currentTime = String.format("%d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        val currentWeekTimeMs = (currentWeekDay - 1) * 86400000 + timeStringToMs(currentTime)
        val weekTimeMsList = getWateringWeekTimesMs()
        var nextWateringTimeMs = 0L

        var nextWateringWeekTimeMs = 0L
        weekTimeMsList.sort()
        if(weekTimeMsList.size > 0) {
            if (currentWeekTimeMs < weekTimeMsList.first() || currentWeekTimeMs > weekTimeMsList.last()) {
                nextWateringWeekTimeMs = weekTimeMsList.first()
            } else {
                var i = 0
                while (currentWeekTimeMs < weekTimeMsList[i] || currentWeekTimeMs > weekTimeMsList[i + 1]) i++
                nextWateringWeekTimeMs = weekTimeMsList[i + 1]
            }

            val currentTimeMs = System.currentTimeMillis()
            nextWateringTimeMs = if (nextWateringWeekTimeMs < currentWeekTimeMs) {
                currentTimeMs + (7 * 86400000 - currentWeekTimeMs) + nextWateringWeekTimeMs
            } else {
                currentTimeMs + nextWateringWeekTimeMs - currentWeekTimeMs
            }
        }

        return nextWateringTimeMs
    }

}

class PlantsViewModelFactory(private val repository: PlantRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}