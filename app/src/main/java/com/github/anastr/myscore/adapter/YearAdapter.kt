package com.github.anastr.myscore.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.anastr.myscore.databinding.ItemYearBinding
import com.github.anastr.myscore.room.entity.Semester
import com.github.anastr.myscore.room.entity.Year
import com.github.anastr.myscore.room.view.YearWithSemester
import com.github.anastr.myscore.util.*
import com.github.anastr.myscore.util.drag.DragTouchHelper
import com.github.anastr.myscore.util.swipe.SwipeTouchHelper

class YearAdapter(
    private val listener: YearAdapterListener,
) : RecyclerView.Adapter<YearAdapter.YearViewHolder>(), DragTouchHelper, SwipeTouchHelper {

    interface YearAdapterListener {
        fun onClickSemester(yearWithSemester: YearWithSemester, semester: Semester)
        fun onYearItemMoved(newList: List<Year>)
        fun onYearItemSwiped(year: Year)
    }

    private val years = arrayListOf<YearWithSemester>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YearViewHolder {
        return YearViewHolder(
            ItemYearBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: YearViewHolder, position: Int) {
        holder.bind(years[position])
    }

    fun updateData(newData: List<YearWithSemester>) {
        val diff = DiffUtil.calculateDiff(YearsDiffUtil(years, newData))
        years.clear()
        years.addAll(newData)
        diff.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = years.size

    override fun onItemDrag(fromPosition: Int, toPosition: Int) {
        years.swap(fromPosition, toPosition)
        Log.i("adapter", "drag from $fromPosition to $toPosition")
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemMoved() {
        listener.onYearItemMoved(years.mapIndexed { index, item -> Year(uid = item.year.uid, order = index) })
    }

    override fun onItemSwiped(position: Int) {
        listener.onYearItemSwiped(years[position].let { Year(uid = it.year.uid, order = it.year.order) })
        notifyItemChanged(position)
    }

    inner class YearViewHolder(private val binding: ItemYearBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.semester1Button.rapidClickListener {
                listener.onClickSemester(years[adapterPosition], Semester.FirstSemester)
            }
            binding.semester2Button.rapidClickListener {
                listener.onClickSemester(years[adapterPosition], Semester.SecondSemester)
            }
        }

        fun bind(yearItem: YearWithSemester) {
            binding.yearTextView.setText(yearsRec[yearItem.year.order])

            binding.scoreSemester1TextView.text = yearItem.semester1Score.formattedScore()
            binding.scoreSemester2TextView.text = yearItem.semester2Score.formattedScore()
            binding.scoreTextView.text = yearItem.score.formattedScore()
            binding.scoreTextView.visibility =
                if (yearItem.semester1Score > 0 && yearItem.semester2Score > 0) View.VISIBLE else View.GONE
        }
    }

}