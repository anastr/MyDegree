package com.github.anastr.domain.repositories

import kotlinx.coroutines.flow.Flow

interface PassDegreeRepo {
    fun getPassDegree(): Flow<Int>
}
