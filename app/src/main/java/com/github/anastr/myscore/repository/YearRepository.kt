package com.github.anastr.myscore.repository

import com.github.anastr.myscore.room.dao.YearDao
import com.github.anastr.myscore.room.entity.Year
import javax.inject.Inject

class YearRepository @Inject constructor (
    private val yearDao: YearDao,
) {

    fun getYearsOrdered(passDegree: Int) = yearDao.getAllOrdered(passDegree)

    fun getFinalDegree(passDegree: Int) = yearDao.getFinalDegree(passDegree)

    fun getYearsCount() = yearDao.getYearsCount()

    suspend fun updateYears(vararg years: Year) = yearDao.updateAll(*years)
}
