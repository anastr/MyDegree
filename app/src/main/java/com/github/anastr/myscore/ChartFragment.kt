package com.github.anastr.myscore

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.anastr.myscore.databinding.FragmentChartBinding
import com.github.anastr.myscore.room.view.YearWithSemester
import com.github.anastr.myscore.util.formattedScore
import com.github.anastr.myscore.util.getColorFromAttr
import com.github.anastr.myscore.util.yearsRec
import com.github.anastr.myscore.viewmodel.YearViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class ChartFragment : Fragment() {

    private var _binding: FragmentChartBinding? = null
    private val binding get() = _binding!!

    private val yearViewModel: YearViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_medium).toLong()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.speedometer.speedTextListener = { it.formattedScore() }

        yearViewModel.years.observe(viewLifecycleOwner) {
            fillData(it)
        }

        yearViewModel.finalDegree.observe(viewLifecycleOwner) {
            binding.speedometer.speedTo(speed = it ?: 0f, moveDuration = animationDuration.toLong())
        }
    }

    private fun fillData(yearsList: List<YearWithSemester>) {
        binding.chartYears.clear()

        val entries = ArrayList<RadarEntry>()
        for (year in yearsList) {
            entries.add(RadarEntry(year.score))
        }
        val set = RadarDataSet(entries, "")
        set.color = Color.RED
//        set.valueTextColor = textColor
        set.setDrawFilled(true)
        set.fillColor = requireContext().getColorFromAttr(R.attr.colorSecondary)
        set.fillAlpha = 100

        val formatter = object: ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return getString(yearsRec[value.toInt()])
            }
        }
        val xAxis = binding.chartYears.xAxis
        xAxis.valueFormatter = formatter
        xAxis.textColor = requireContext().getColorFromAttr(R.attr.colorOnSurface)
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = yearsList.size - .1f

        val yAxis = binding.chartYears.yAxis
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = 80f
        yAxis.setDrawLabels(false)
        yAxis.textColor = requireContext().getColorFromAttr(R.attr.colorOnSurface)

        val newData = RadarData(set)
        newData.setValueTextColor(requireContext().getColorFromAttr(R.attr.colorOnSurface))
//        chart_years.description.text = "معدلات السنوات الدراسية"
        binding.chartYears.apply {
            description.text = ""
            description.textColor = requireContext().getColorFromAttr(R.attr.colorOnSurface)
            legend.isEnabled = false
            data = newData
            animateXY(animationDuration, animationDuration, Easing.EaseOutQuad)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val animationDuration = 1200
    }
}