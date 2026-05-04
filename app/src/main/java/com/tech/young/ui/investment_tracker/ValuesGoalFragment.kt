package com.tech.young.ui.investment_tracker

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BaseCustomBottomSheet
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.DropDownData
import com.tech.young.data.api.Constants
import com.tech.young.data.model.AddInvestmentApiResponse
import com.tech.young.data.model.DummyLists.getInvestmentPerformance
import com.tech.young.data.model.DummyLists.getInvestments
import com.tech.young.data.model.DummyLists.getYearsEmployed
import com.tech.young.databinding.BotttomSheetTopicsBinding
import com.tech.young.databinding.FragmentValuesGoalBinding
import com.tech.young.databinding.ItemLayoutDropDownBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class ValuesGoalFragment : BaseFragment<FragmentValuesGoalBinding>() {

    private val viewModel: InvestmentTrackingVm by activityViewModels()
    private lateinit var commonBottomSheet: BaseCustomBottomSheet<BotttomSheetTopicsBinding>
    private lateinit var commonAdapter: SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    private lateinit var topicAdapter : SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    private lateinit var topicBottomSheet : BaseCustomBottomSheet<BotttomSheetTopicsBinding>

    private var topicList = ArrayList<DropDownData>()
    private var type: String? = null

    // Store selected actual values
    private var selectedTypeActualValue: String? = null
    private var selectedInvestmentActualValue: String? = null
    private var selectedSavingActualValue: String? = null

    override fun getLayoutResource(): Int {
      return  R.layout.fragment_values_goal
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        initOnClick()
        initPopup()
        getTopicsList()
        initAdapter()
        initObserver()
        populateSelectedData()
    }

    private fun populateSelectedData() {
        viewModel.selectedInvestment.observe(viewLifecycleOwner, Observer { item ->
            if (item != null) {
                binding.tvAddToPortfolio.text = "Update Portfolio"
                binding.etName.setText(item.name)
                binding.etSymbol.setText(item.symbol)
                
                // Set Type
                val typeData = topicList.find { topic -> topic.actualValue == item.type }
                binding.etType.setText(typeData?.title ?: item.type.replace("_", " ").capitalize())
                selectedTypeActualValue = item.type

                binding.etCurrentValue.setText(item.currentValue.toString())
                binding.etShare.setText(item.units.toString())

                // Set Investment Performance
                val performanceList = getInvestmentPerformance()
                val performanceData = performanceList.find { p -> p.actualValue == item.investmentPerformingWell }
                binding.etInvestment.setText(performanceData?.title ?: item.investmentPerformingWell.replace("_", " ").capitalize())
                selectedInvestmentActualValue = item.investmentPerformingWell

                // Set General Investment (Saving)
                val savingList = getInvestments()
                val savingData = savingList.find { s -> s.actualValue == item.generalInvestment }
                binding.etSaving.setText(savingData?.title ?: item.generalInvestment.replace("_", " ").capitalize())
                selectedSavingActualValue = item.generalInvestment
            } else {
                binding.tvAddToPortfolio.text = "Add to Portfolio"
                // Clear fields if no item is selected (for fresh add)
                binding.etName.setText("")
                binding.etSymbol.setText("")
                binding.etType.setText("")
                binding.etCurrentValue.setText("")
                binding.etShare.setText("")
                binding.etInvestment.setText("")
                binding.etSaving.setText("")
                selectedTypeActualValue = null
                selectedInvestmentActualValue = null
                selectedSavingActualValue = null
            }
        })
    }

    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer{
            when(it?.status){
                Status.LOADING -> {
                    showLoading()
                }
                Status.SUCCESS -> {
                    hideLoading()
                    when(it.message){
                        "investmentTracking" ->{
                            val myDataModel : AddInvestmentApiResponse?= BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    val successMsg = if (viewModel.selectedInvestment.value != null) "Investment updated successfully" else "Investment added successfully"
                                    showToast(successMsg)
                                    // Clear selection after success
                                    viewModel.selectedInvestment.value = null
                                }
                            }

                        }
                    }

                }
                Status.ERROR -> {
                    hideLoading()
                }
                else -> {

                }
            }
        })
    }

    private fun initPopup() {
        topicBottomSheet = BaseCustomBottomSheet(requireContext(), R.layout.botttom_sheet_topics){

        }

        commonBottomSheet =
            BaseCustomBottomSheet(requireContext(), R.layout.botttom_sheet_topics){}
        commonBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        commonBottomSheet.behavior.isDraggable = true
    }

    private fun initAdapter() {
        topicAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down,BR.bean){v,m,pos ->
            when(v.id){
                R.id.consMain , R.id.title->{
                    binding.etType.setText(m.title)
                    selectedTypeActualValue = m.actualValue
                    topicBottomSheet.dismiss()
                }
            }
        }
        topicBottomSheet.binding.rvTopics.adapter = topicAdapter
        topicAdapter.list = topicList
        topicAdapter.notifyDataSetChanged()

        commonAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.consMain, R.id.title -> {
                        if (type != null) {
                            when (type) {
                                "2" -> {
                                    binding.etInvestment.setText(m.title)
                                    selectedInvestmentActualValue = if (m.actualValue.isNullOrEmpty()) m.title.lowercase().replace(" ", "_") else m.actualValue
                                }
                                "3" -> {
                                    binding.etSaving.setText(m.title)
                                    selectedSavingActualValue = if (m.actualValue.isNullOrEmpty()) m.title.lowercase().replace(" ", "_") else m.actualValue
                                }
                            }
                        }
                        commonBottomSheet.dismiss()
                    }
                }
            }
        commonBottomSheet.binding.rvTopics.adapter = commonAdapter
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer{
            when(it?.id){
                R.id.tvAddToPortfolio ->{
                    if (isEmptyField()){
                        val data  = HashMap<String, Any>()
                        data["name"] = binding.etName.text.toString().trim()
                        data["symbol"] = binding.etSymbol.text.toString().trim()
                        data["type"] = selectedTypeActualValue ?: ""
                        data["currentValue"] = binding.etCurrentValue.text.toString().trim()
                        data["units"] = binding.etShare.text.toString().trim()
                        data["investmentPerformingWell"]  = selectedInvestmentActualValue ?: ""
                        data["generalInvestment"] = selectedSavingActualValue ?: ""
                        
                        val selectedItem = viewModel.selectedInvestment.value
                        if (selectedItem != null) {
                            // Update mode
                            data["investmentId"] = selectedItem._id
                            data["name"] = binding.etName.text.toString().trim()
                            data["symbol"] = binding.etSymbol.text.toString().trim()
                            data["type"] = selectedTypeActualValue ?: ""
                            data["currentValue"] = binding.etCurrentValue.text.toString().trim()
                            data["units"] = binding.etShare.text.toString().trim()
                            data["investmentPerformingWell"]  = selectedInvestmentActualValue ?: ""
                            data["generalInvestment"] = selectedSavingActualValue ?: ""
                            viewModel.investmentTracking(Constants.UPDATE_INVESTMENT , data)
                        } else {
                            // Add mode
                            viewModel.investmentTracking(Constants.ADD_INVESTMENT , data)
                        }
                    }



                }
                R.id.etType ->{
                    topicBottomSheet.show()
                }
                R.id.etInvestment ->{
                    type = "2"
                    commonAdapter.list = getInvestmentPerformance()
                    commonBottomSheet.show()

                }
                R.id.etSaving ->{
                    type = "3"
                    commonAdapter.list = getInvestments()
                    commonBottomSheet.show()

                }
            }
        })
    }

    private fun isEmptyField() : Boolean {
        if (TextUtils.isEmpty(binding.etName.text.toString().trim())){
            showToast("Please enter name")
            return false
        }
        if (TextUtils.isEmpty(binding.etSymbol.text.toString().trim())){
            showToast("Please enter symbol")
            return false
        }
        if (TextUtils.isEmpty(binding.etType.text.toString().trim())){
            showToast("Please select type")
            return false
        }
        if (TextUtils.isEmpty(binding.etCurrentValue.text.toString().trim())){
            showToast("Please enter current value")
            return false
        }
        if (TextUtils.isEmpty(binding.etShare.text.toString().trim())){
            showToast("Please enter shares")
            return false
        }
        if (TextUtils.isEmpty(binding.etInvestment.text.toString().trim())){
            showToast("Please select investment")
            return false
        }
        if (TextUtils.isEmpty(binding.etSaving.text.toString().trim())){
            showToast("Please select general investments")
            return false
        }
        return true
    }


    private fun getTopicsList() {
        topicList.clear()
        topicList.add(DropDownData("Stock", "stock"))
        topicList.add(DropDownData("ETF", "etf"))
        topicList.add(DropDownData("Mutual Fund", "mutual_fund"))
        topicList.add(DropDownData("Index Fund", "index_fund"))
        topicList.add(DropDownData("Cryptocurrency", "cryptocurrency"))
        topicList.add(DropDownData("Bond", "bond"))
        topicList.add(DropDownData("Real Estate", "real_estate"))
        topicList.add(DropDownData("REIT", "reit"))
        topicList.add(DropDownData("Commodity", "commodity"))
        topicList.add(DropDownData("Options", "options"))
        topicList.add(DropDownData("Futures", "futures"))
        topicList.add(DropDownData("Private Equity", "private_equity"))
        topicList.add(DropDownData("Venture Capital", "venture_capital"))
        topicList.add(DropDownData("Savings / Cash", "savings_cash"))
        topicList.add(DropDownData("Fixed Deposit", "fixed_deposit"))
        topicList.add(DropDownData("Pension Fund", "pension_fund"))
        topicList.add(DropDownData("Other", "other"))
    }

}
