package com.github.anastr.domain.repositories

import com.github.anastr.domain.enums.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemeRepo {
    fun getTheme(): Flow<ThemeMode>
}
