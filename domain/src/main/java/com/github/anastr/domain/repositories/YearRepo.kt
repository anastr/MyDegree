package com.github.anastr.domain.repositories

import com.github.anastr.domain.entities.db.Year
import com.github.anastr.domain.entities.db.YearWithSemester
import kotlinx.coroutines.flow.Flow

interface YearRepo {

    fun getYearsOrdered(passDegree: Int): Flow<List<YearWithSemester>>

    fun getFinalDegree(passDegree: Int): Flow<Float?>

    suspend fun updateYears(vararg years: Year)

    suspend fun deleteYear(year: Year)
}
