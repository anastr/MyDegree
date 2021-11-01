package com.github.anastr.data.repositories

import com.github.anastr.data.datasource.DatabaseDao
import com.github.anastr.data.datasource.YearDao
import com.github.anastr.domain.entities.db.Year
import com.github.anastr.domain.entities.db.YearWithSemester
import com.github.anastr.domain.repositories.YearRepo
import kotlinx.coroutines.flow.Flow

internal class YearRepository(
    private val yearDao: YearDao,
    private val databaseDao: DatabaseDao,
): YearRepo {

    override fun getYearsOrdered(passDegree: Int): Flow<List<YearWithSemester>> = yearDao.getAllOrdered(passDegree)

    override fun getFinalDegree(passDegree: Int):Flow<Float?> = yearDao.getFinalDegree(passDegree)

    override suspend fun updateYears(vararg years: Year) = yearDao.updateAll(*years)

    override suspend fun deleteYear(year: Year) = databaseDao.deleteYear(year)
}
