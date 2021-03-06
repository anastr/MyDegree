package com.github.anastr.myscore.room.view

import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo

data class YearWithSemester(
    @ColumnInfo(name = "uid") val uid: Long = 0,
    @ColumnInfo(name = "year_order") var order: Int,
    @ColumnInfo(name = "semester1Score") val semester1Score: Float = 0f,
    @ColumnInfo(name = "semester2Score") val semester2Score: Float = 0f,
) {
    val score: Float
        get() {
            val sum = semester1Score + semester2Score
            return if (semester1Score == 0f || semester2Score == 0f)
                sum
            else
                sum / 2f
        }
}

object YearWithSemesterDiffCallback : DiffUtil.ItemCallback<YearWithSemester>() {
    override fun areItemsTheSame(oldItem: YearWithSemester, newItem: YearWithSemester) = oldItem.uid == newItem.uid
    override fun areContentsTheSame(oldItem: YearWithSemester, newItem: YearWithSemester) = oldItem == newItem
}
