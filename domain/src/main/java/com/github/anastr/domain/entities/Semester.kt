package com.github.anastr.domain.entities

enum class Semester {
    FirstSemester, SecondSemester;

    val position get() = if (this == FirstSemester) 0 else 1

    companion object {
        fun byOrder(order: Int): Semester {
            return values()[order]
        }
    }
}
