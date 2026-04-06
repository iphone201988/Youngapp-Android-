package com.tech.young.ui.my_profile_screens.forNormal

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.tabs.TabLayoutMediator
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BaseCustomDialog
import com.tech.young.databinding.FragmentPerformanceBinding
import com.tech.young.databinding.ItemInvestmentsBinding
import com.tech.young.databinding.ItemPopupPerformanceGraphBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import com.tech.young.ui.my_profile_screens.profile_fragments.CalendarFragment
import com.tech.young.ui.my_profile_screens.profile_fragments.EditProfileFragment
import com.tech.young.ui.my_profile_screens.profile_fragments.ProfileFragment
import com.tech.young.ui.my_profile_screens.profile_fragments.SavedFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class PerformanceFragment : BaseFragment<FragmentPerformanceBinding>() {

    private val viewModel: YourProfileVM by viewModels()

    private lateinit var performanceGraphPopup  : BaseCustomDialog<ItemPopupPerformanceGraphBinding>


    override fun getLayoutResource(): Int {
        return R.layout.fragment_performance
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {

        val fullText = "Quarterly profile updates improve your Match Score accuracy by up to 34%"

        val spannable = SpannableString(fullText)

        val matchScoreStart = fullText.indexOf("Match Score")
        val matchScoreEnd = matchScoreStart + "Match Score".length
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorPrimary)),
            matchScoreStart,
            matchScoreEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val percentStart = fullText.indexOf("34%")
        val percentEnd = percentStart + "34%".length
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.green)),
            percentStart,
            percentEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvInfo.text = spannable


        ViewCompat.setOnApplyWindowInsetsListener(binding.clInvestment) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            // Only adjust padding if keyboard is visible
            view.setPadding(0, 0, 0, imeHeight)
            insets
        }

        initPopup()

        initViewPagerAdapter()
        initAdapter()
        setupChart()
    }

    private fun initPopup() {
        performanceGraphPopup = BaseCustomDialog(requireContext(), R.layout.item_popup_performance_graph){

        }
    }

    private fun initAdapter() {

    }



    private fun setupChart() {

        val entries = listOf(
            Entry(0f, 100f),
            Entry(1f, 450f),
            Entry(2f, 400f),
            Entry(3f, 550f),
            Entry(4f, 1000f)
        )

        val dataSet = LineDataSet(entries, "")

        dataSet.color = Color.parseColor("#16A34A")
        dataSet.lineWidth = 2.5f
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.cubicIntensity = 0.2f

        dataSet.setDrawFilled(false)
//        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.chart_gradient)
//        dataSet.fillDrawable = drawable

        dataSet.isHighlightEnabled = false

        performanceGraphPopup.binding.lineChart.data = LineData(dataSet)

        // 🧹 Clean UI
        performanceGraphPopup.binding.lineChart.description.isEnabled = false
        performanceGraphPopup.binding.lineChart.legend.isEnabled = false
        performanceGraphPopup.binding.lineChart.setTouchEnabled(false)
        performanceGraphPopup.binding.lineChart.setScaleEnabled(false)

        val labels = listOf("jan", "feb", "mar", "apr", "may")

        performanceGraphPopup.binding.lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawAxisLine(false)
            textColor = Color.GRAY
            textSize = 12f
            granularity = 1f
        }

        performanceGraphPopup.binding.lineChart.axisLeft.apply {
            textColor = Color.GRAY
            textSize = 12f
            axisMinimum = 0f

            enableGridDashedLine(10f, 10f, 0f)
            gridColor = Color.LTGRAY
            setDrawAxisLine(false)

            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()}$"
                }
            }
        }

        performanceGraphPopup.binding.lineChart.axisRight.isEnabled = false

        performanceGraphPopup.binding.lineChart.setExtraOffsets(10f, 10f, 10f, 10f)

        performanceGraphPopup.binding.lineChart.animateX(1000)

        performanceGraphPopup.binding.lineChart.invalidate()
    }



    /** view pager & tab layout handling **/
    private fun initViewPagerAdapter() {
        val adapter = ViewPagerAdapter(this)
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Your Investments"
                1 -> "Similar Profiles"
                else -> "Your Investments"
            }
        }.attach()
    }

    inner class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> YourInvestmentFragment()
                1 -> SimilarProfileFragment()
                else -> YourInvestmentFragment()
            }
        }
    }

}