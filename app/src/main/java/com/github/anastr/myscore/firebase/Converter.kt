package com.github.anastr.myscore.firebase

import com.github.anastr.myscore.room.entity.Course
import com.github.anastr.myscore.room.entity.Semester
import com.github.anastr.myscore.room.entity.Year

fun Year.toHashMap(): HashMap<String, Any> = hashMapOf(
    "uid" to uid,
    "year_order" to order,
)

fun HashMap<String, Any>.toYear(): Year = Year(
    uid = get("uid") as Long,
    order = (get("year_order") as Long).toInt(),
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
    uid = get("uid") as Long,
    yearId = get("year_id") as Long,
    semester = Semester.byOrder((get("semester") as Long).toInt()),
    name = get("name") as String,
    hasPractical = get("has_practical") as Boolean,
    hasTheoretical = get("has_theoretical") as Boolean,
    practicalScore = (get("practical_score") as Long).toInt(),
    theoreticalScore = (get("theoretical_score") as Long).toInt(),
)