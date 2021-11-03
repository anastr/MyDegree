package com.github.anastr.myscore.util

import com.github.anastr.data.utils.formattedScore
import com.github.anastr.data.utils.swap
import com.github.anastr.domain.constant.MAX_YEARS
import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsKtTest {

    @Test
    fun testSwap() {
        val list = listOf("First", "Second")
        val mutableList = list.toMutableList()
        mutableList.swap(0, 1)
        assertEquals(list[0], mutableList[1])
        assertEquals(list[1], mutableList[0])
    }

    @Test
    fun testDegreeFormatter() {
        assertEquals("90", 90f.formattedScore())
        assertEquals("90", 90.0f.formattedScore())
        assertEquals("90.1", 90.1f.formattedScore())
        assertEquals("90.7", 90.7f.formattedScore())
        assertEquals("90.12", 90.12f.formattedScore())
        assertEquals("90.12", 90.125f.formattedScore())
        assertEquals("90.13", 90.127f.formattedScore())
    }

    @Test
    fun testYearsResList() {
        assertEquals(yearsRec.size, MAX_YEARS)
    }
}
