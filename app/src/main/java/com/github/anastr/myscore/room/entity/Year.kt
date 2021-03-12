package com.github.anastr.myscore.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "year")
data class Year(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid") val uid: Long = 0,
    @ColumnInfo(name = "year_order") var order: Int,
)
