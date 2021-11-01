package com.github.anastr.data.datasource

import androidx.room.Dao
import androidx.room.Query
import com.github.anastr.domain.entities.db.Year
import com.github.anastr.domain.entities.db.YearWithSemester
import kotlinx.coroutines.flow.Flow

@Dao
interface YearDao: BaseDao<Year> {

//    @Query("SELECT * FROM year ORDER BY year_order ASC")
//    fun getAllOrdered(): Flow<List<Year>>

    @Query(
        """
        SELECT year.uid, year.year_order, 
            AVG(CASE WHEN course.semester = 0 AND course.theoretical_score + course.practical_score >= :passDegree
                THEN course.practical_score + course.theoretical_score END) AS semester1Score, 
            AVG(CASE WHEN course.semester = 1 AND course.theoretical_score + course.practical_score >= :passDegree
                THEN course.practical_score + course.theoretical_score END) AS semester2Score 
            FROM year 
            LEFT JOIN course ON year.uid = course.year_id 
            GROUP BY year.uid 
            ORDER BY year.year_order ASC 
    """
    )
    fun getAllOrdered(passDegree: Int): Flow<List<YearWithSemester>>

    @Query(
        """
        SELECT AVG(CASE WHEN course.theoretical_score + course.practical_score >= :passDegree
                THEN course.practical_score + course.theoretical_score END)
        FROM course
    """
    )
    fun getFinalDegree(passDegree: Int): Flow<Float?>

//    @Query("SELECT * FROM course WHERE uid IN (:courseIds)")
//    fun loadAllByIds(courseIds: LongArray): List<Course>

}