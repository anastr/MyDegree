package com.github.anastr.myscore.repository

import com.github.anastr.domain.entities.db.Year
import com.github.anastr.domain.entities.db.YearWithSemester
import com.github.anastr.myscore.room.dao.YearDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class YearRepository @Inject constructor (
    private val yearDao: YearDao,
) {

    fun getYearsOrdered(passDegree: Int): Flow<List<YearWithSemester>> = yearDao.getAllOrdered(passDegree)

    fun getFinalDegree(passDegree: Int):Flow<Float?> = yearDao.getFinalDegree(passDegree)

    suspend fun updateYears(vararg years: Year) = yearDao.updateAll(*years)
}
