package com.example.gardeningapp.database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plant_table")
data class Plant(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "specie") val specie: String?,
    @ColumnInfo(name = "scientific_name") val scientificName: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "image") val image: String?,
    @ColumnInfo(name = "weekdays") var weekdays: String?,
    @ColumnInfo(name = "time") val time: String?
): Parcelable {
    constructor(parcel: Parcel): this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(specie)
        parcel.writeString(scientificName)
        parcel.writeString(description)
        parcel.writeString(image)
        parcel.writeString(weekdays)
        parcel.writeString(time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR: Parcelable.Creator<Plant> {
        override fun createFromParcel(parcel: Parcel): Plant {
            return Plant(parcel)
        }

        override fun newArray(size: Int): Array<Plant?> {
            return arrayOfNulls(size)
        }
    }
}