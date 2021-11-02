package com.github.anastr.domain.entities.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.anastr.domain.entities.Semester

@Entity(tableName = "course")
data class Course(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid") val uid: Long = 0,
    @ColumnInfo(name = "year_id") val yearId: Long,
    @ColumnInfo(name = "semester") val semester: Semester,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "has_practical") val hasPractical: Boolean,
    @ColumnInfo(name = "has_theoretical") val hasTheoretical: Boolean,
    @ColumnInfo(name = "practical_score") val practicalScore: Int = 0,
    @ColumnInfo(name = "theoretical_score") val theoreticalScore: Int = 0,
) {
    val score: Int
        get() = theoreticalScore + practicalScore
}
