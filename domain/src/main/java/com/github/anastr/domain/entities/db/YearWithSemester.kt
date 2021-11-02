package com.github.anastr.domain.entities.db

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class YearWithSemester(
    @Embedded val year: Year,
    @ColumnInfo(name = "semester1Score") val semester1Score: Float = 0f,
    @ColumnInfo(name = "semester2Score") val semester2Score: Float = 0f,
) {
    val score: Float
        get() {
            val sum = semester1Score + semester2Score
            return if (semester1Score == 0f || semester2Score == 0f)
                sum
            else
                sum / 2f
        }
}
