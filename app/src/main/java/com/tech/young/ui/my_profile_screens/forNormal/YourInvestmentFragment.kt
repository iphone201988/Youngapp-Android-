package com.tech.young.ui.my_profile_screens.forNormal

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BaseCustomDialog
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Resource
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.event.SingleRequestEvent
import com.tech.young.base.utils.showToast
import com.tech.young.data.api.Constants
import com.tech.young.data.model.Investment
import com.tech.young.data.model.PerformanceDetailApiResponse
import com.tech.young.databinding.FragmentYourInvestmentBinding
import com.tech.young.databinding.ItemInvestmentsBinding
import com.tech.young.databinding.ItemPopupPerformanceGraphBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import com.google.gson.JsonObject
import com.tech.young.data.model.GetPortfolioData
import com.tech.young.data.model.PerformanceDetailApiResponseData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class YourInvestmentFragment : BaseFragment<FragmentYourInvestmentBinding>() {

    private val viewModel: YourProfileVM by activityViewModels()
    private lateinit var investmentAdapter: SimpleRecyclerViewAdapter<Investment, ItemInvestmentsBinding>
    private lateinit var performanceGraphPopup: BaseCustomDialog<ItemPopupPerformanceGraphBinding>

    override fun getLayoutResource(): Int {
        return R.layout.fragment_your_investment
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        initPopup()
        initAdapter()
        getData()
        setupChart()
        iniObserver()
    }

    private fun setupChart() {
        val chart = performanceGraphPopup.binding.lineChart

        chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            setScaleEnabled(false)
            axisRight.isEnabled = false
        }

        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawAxisLine(false)
            textColor = Color.GRAY
            textSize = 11f
            granularity = 1f
        }

        chart.axisLeft.apply {
            textColor = Color.GRAY
            textSize = 11f
            setDrawGridLines(true)
            enableGridDashedLine(10f, 10f, 0f)
            gridColor = Color.LTGRAY
            setDrawAxisLine(false)

            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value >= 1000) "${(value / 1000).toInt()}K" else "${value.toInt()}"
                }
            }
        }
    }


    private fun iniObserver() {
        viewModel.observeMonthlyAnalytics.observe(viewLifecycleOwner , Observer{
            when(it?.status){
                Status.LOADING -> {
                    showLoading()
                }
                Status.SUCCESS -> {
                    hideLoading()
                    if (it.message == "getMonthlyAnalytics") {
                        val myDataModel : PerformanceDetailApiResponse ? = BindingUtils.parseJson(it.data.toString())
                        if(myDataModel?.data != null){
                            updateChart(myDataModel)
                        }
                    }
                }
                Status.ERROR -> {
                    hideLoading()
                    showToast(it.message.toString())
                }
                else -> {

                }
            }
        })

        // Observe common status for main list loading
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            if (it?.status == Status.LOADING && it.message == "getPerformance") {
                binding.shimmerLayout.visibility = View.VISIBLE
                binding.shimmerLayout.startShimmer()
                binding.rvInvestment.visibility = View.GONE
            } else {
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
                binding.rvInvestment.visibility = View.VISIBLE
            }
        })
    }

    private fun getData() {
        viewModel.performanceData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.investments != null) {
                investmentAdapter.list = data.investments
                investmentAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun initPopup() {
        performanceGraphPopup =
            BaseCustomDialog(requireContext(), R.layout.item_popup_performance_graph) {
                if (it.id == R.id.ivClose) {
                    performanceGraphPopup.dismiss()
                }

            }
    }

    private fun initAdapter() {
        investmentAdapter = SimpleRecyclerViewAdapter(
            R.layout.item_investments,
            BR.bean
        ) { v, m, pos ->
            when (v.id) {
                R.id.consMain -> {
                    performanceGraphPopup.binding.symbol = "${m.name} (${m.symbol})"
                    performanceGraphPopup.binding.currentValue = "$${m.currentValue}"
                    performanceGraphPopup.binding.growth =
                        if (m.percentageGrowth >= 0) "+${m.percentageGrowth}%" else "${m.percentageGrowth}%"

                    // Reset chart and show popup immediately
                    performanceGraphPopup.binding.lineChart.clear()
                    performanceGraphPopup.show()

                    val data = HashMap<String, Any>()
                    data["investmentId"] = m._id
                    viewModel.getMonthlyAnalytics(Constants.MONTHLY_ANALYSIS, data)
                }
            }
        }
        binding.rvInvestment.adapter = investmentAdapter
    }

    private fun updateChart(res: PerformanceDetailApiResponse) {

        val chart = performanceGraphPopup.binding.lineChart

        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        // 1. Reverse the data: current month at start (index 0), oldest at end
        val reversedData = res.data.reversed()

        reversedData.forEachIndexed { index, item ->
            entries.add(Entry(index.toFloat(), item.value.toFloat()))
            
            // 2. Only show labels for start and end
            if (index == 0 || index == reversedData.size - 1) {
                labels.add(item.month)
            } else {
                labels.add("")
            }
        }

        val dataSet = LineDataSet(entries, "").apply {
            color = Color.parseColor("#16A34A")
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = Color.parseColor("#16A34A")
            fillAlpha = 80
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.labelCount = reversedData.size
        
        // 3. Ensure labels on the edges are not cut off
        chart.xAxis.setAvoidFirstLastClipping(true)
        
        chart.invalidate()
    }



    }

