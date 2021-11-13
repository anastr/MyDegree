package com.github.anastr.data.repositories

import com.github.anastr.data.datasource.SharedPrefDataSource
import com.github.anastr.data.utils.PreferenceKeys
import com.github.anastr.data.utils.ThemeModeKeys
import com.github.anastr.domain.enums.ThemeMode
import com.github.anastr.domain.repositories.PassDegreeRepo
import com.github.anastr.domain.repositories.ThemeRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
internal class SharedPrefRepository constructor(
    private val sharedPrefDataSource: SharedPrefDataSource,
): ThemeRepo, PassDegreeRepo {

    override fun getPassDegree(): Flow<Int> =
        sharedPrefDataSource.intFlow(PreferenceKeys.PASS_DEGREE, 60)

    override fun getTheme(): Flow<ThemeMode> =
        sharedPrefDataSource.stringFlow(PreferenceKeys.THEME, ThemeModeKeys.FOLLOW_SYSTEM)
            .map {
                when (it) {
                    ThemeModeKeys.FOLLOW_SYSTEM -> ThemeMode.FOLLOW_SYSTEM
                    ThemeModeKeys.LIGHT -> ThemeMode.LIGHT
                    ThemeModeKeys.DARK -> ThemeMode.DARK
                    else -> throw IllegalArgumentException("Wrong value for theme preference.")
                }
            }
}
