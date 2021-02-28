package com.github.anastr.myscore.room

import androidx.room.TypeConverter
import com.github.anastr.myscore.room.entity.Semester

class RoomConverters {
    @TypeConverter
    fun toSemester(value: Int) = enumValues<Semester>()[value]

    @TypeConverter
    fun fromSemester(value: Semester) = value.ordinal
}