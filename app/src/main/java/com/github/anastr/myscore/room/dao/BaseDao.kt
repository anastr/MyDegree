package com.github.anastr.myscore.room.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg data: T)

    @Update
    suspend fun updateAll(vararg data: T)

    @Delete
    suspend fun delete(data: T)
}