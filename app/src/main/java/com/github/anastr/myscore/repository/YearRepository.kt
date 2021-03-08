package com.github.anastr.myscore.repository

import com.github.anastr.myscore.room.dao.YearDao
import com.github.anastr.myscore.room.entity.Year
import com.github.anastr.myscore.room.view.YearWithSemester
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class YearRepository @Inject constructor (
    private val yearDao: YearDao,
) {

    fun getYearsOrdered(passDegree: Int): Flow<List<YearWithSemester>> = yearDao.getAllOrdered(passDegree)

    fun getFinalDegree(passDegree: Int):Flow<Float?> = yearDao.getFinalDegree(passDegree)

    fun getYearsCount(): Flow<Int> = yearDao.getYearsCount()

    suspend fun updateYears(vararg years: Year) = yearDao.updateAll(*years)
}
