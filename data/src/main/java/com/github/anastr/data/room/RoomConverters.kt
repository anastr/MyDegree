package com.github.anastr.data.room

import androidx.room.TypeConverter
import com.github.anastr.domain.entities.Semester

class RoomConverters {
    @TypeConverter
    fun toSemester(value: Int) = enumValues<Semester>()[value]

    @TypeConverter
    fun fromSemester(value: Semester) = value.ordinal
}