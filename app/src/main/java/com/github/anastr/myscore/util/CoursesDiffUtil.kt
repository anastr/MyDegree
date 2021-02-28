package com.github.anastr.myscore.util

import androidx.recyclerview.widget.DiffUtil
import com.github.anastr.myscore.room.entity.Course

class CoursesDiffUtil(
    private val oldList: List<Course>,
    private val newList: List<Course>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].uid == newList[newItemPosition].uid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}