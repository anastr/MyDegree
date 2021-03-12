package com.github.anastr.myscore.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "course")
data class Course(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid") val uid: Long = 0,
    @ColumnInfo(name = "year_id") val yearId: Long,
    @ColumnInfo(name = "semester") val semester: Semester,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "has_practical") var hasPractical: Boolean,
    @ColumnInfo(name = "has_theoretical") var hasTheoretical: Boolean,
    @ColumnInfo(name = "practical_score") var practicalScore: Int = 0,
    @ColumnInfo(name = "theoretical_score") var theoreticalScore: Int = 0,
) {
    val score: Int
        get() = theoreticalScore + practicalScore
}
