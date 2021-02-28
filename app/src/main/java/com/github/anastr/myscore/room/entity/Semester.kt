package com.github.anastr.myscore.room.entity

enum class Semester {
    FirstSemester,
    SecondSemester;

    val position get() = if (this == FirstSemester) 0 else 1

    companion object {
        fun byOrder(order: Int): Semester {
            return Semester.values()[order]
        }
    }
}