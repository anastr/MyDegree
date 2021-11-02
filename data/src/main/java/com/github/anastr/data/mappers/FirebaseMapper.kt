package com.github.anastr.myscore.firebase

import com.github.anastr.domain.entities.Semester
import com.github.anastr.domain.entities.db.Course
import com.github.anastr.domain.entities.db.Year
import com.github.anastr.myscore.util.getBoolean
import com.github.anastr.myscore.util.getInt
import com.github.anastr.myscore.util.getLong
import com.github.anastr.myscore.util.getString

fun Year.toHashMap(): HashMap<String, Any> = hashMapOf(
    "uid" to uid,
    "year_order" to order,
)

fun HashMap<String, Any>.toYear(): Year =
    Year(
        uid = getLong("uid"),
        order = getInt("year_order"),
    )

fun Course.toHashMap(): HashMap<String, Any> = hashMapOf(
    "uid" to uid,
    "year_id" to yearId,
    "semester" to semester.ordinal,
    "name" to name,
    "has_practical" to hasPractical,
    "has_theoretical" to hasTheoretical,
    "practical_score" to practicalScore,
    "theoretical_score" to theoreticalScore,
)

fun HashMap<String, Any>.toCourse(): Course = Course(
    uid = getLong("uid"),
    yearId = getLong("year_id"),
    semester = Semester.byOrder(getInt("semester")),
    name = getString("name"),
    hasPractical = getBoolean("has_practical"),
    hasTheoretical = getBoolean("has_theoretical"),
    practicalScore = getInt("practical_score"),
    theoreticalScore = getInt("theoretical_score"),
)