package com.github.anastr.myscore.util

import androidx.recyclerview.widget.DiffUtil
import com.github.anastr.myscore.room.view.YearWithSemester

class YearsDiffUtil(
    private val oldList: List<YearWithSemester>,
    private val newList: List<YearWithSemester>,
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].year.uid == newList[newItemPosition].year.uid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}