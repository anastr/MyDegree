package com.github.anastr.myscore.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.anastr.myscore.CourseListFragmentDirections
import com.github.anastr.myscore.CourseMode
import com.github.anastr.myscore.R
import com.github.anastr.myscore.databinding.ItemCourseBinding
import com.github.anastr.domain.entities.db.Course
import com.github.anastr.myscore.util.rapidClickListener
import java.util.*

class CourseAdapter : ListAdapter<Course, CourseAdapter.CourseViewHolder>(CourseDiffCallback) {

    var passDegree = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        return CourseViewHolder(
            ItemCourseBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CourseViewHolder(
        private val binding: ItemCourseBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(course: Course) {
//            holder.countTextView.text = "${position + 1}"
            binding.courseTextView.text = course.name

            binding.theoreticalTextView.visibility = if (course.hasTheoretical) View.VISIBLE else View.GONE
            binding.theoreticalTextView.text = String.format(
                Locale.ENGLISH,
                binding.root.context.getString(R.string.theoretical),
                course.theoreticalScore,
            )
            binding.practicalTextView.visibility = if (course.hasPractical) View.VISIBLE else View.GONE
            binding.practicalTextView.text = String.format(
                Locale.ENGLISH,
                binding.root.context.getString(R.string.practical),
                course.practicalScore,
            )
            binding.totalTextView.text = "${course.score}"
            val backColor = if (course.score < passDegree)
                R.color.noneDegree
            else when(course.score) {
                in 90 ..100 -> R.color.veryGoodDegree
                in 80 ..89 -> R.color.goodDegree
                in 70 ..79 -> R.color.lowDegree
                else -> R.color.veryLowDegree
            }
            ViewCompat.setBackgroundTintList(binding.totalTextView, ContextCompat.getColorStateList(binding.root.context, backColor))

            binding.root.rapidClickListener {
                val action = CourseListFragmentDirections.actionCourseListFragmentToCourseDialog(
                    CourseMode.Edit(course.uid))
                Navigation.findNavController(binding.root).navigate(action)
            }
        }

    }
}

object CourseDiffCallback : DiffUtil.ItemCallback<Course>() {
    override fun areItemsTheSame(oldItem: Course, newItem: Course) = oldItem.uid == newItem.uid
    override fun areContentsTheSame(oldItem: Course, newItem: Course) = oldItem == newItem
}
