package com.zeeko.mindclash.data.database

import androidx.room.TypeConverter

class Converters {
    
    @TypeConverter
    fun fromBooleanToInt(value: Boolean): Int = if (value) 1 else 0
    
    @TypeConverter
    fun fromIntToBoolean(value: Int): Boolean = value == 1
}
