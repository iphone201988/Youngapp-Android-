package com.tech.young.ui.my_profile_screens.forNormal

import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
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
import com.tech.young.data.model.DummyLists.getFinancialExpList
import com.tech.young.data.model.DummyLists.getRiskToleranceList
import com.tech.young.data.model.DummyLists.getSalaryRange
import com.tech.young.data.model.DummyLists.getYearsEmployed
import com.tech.young.data.model.DummyLists.topicsOfInterest
import com.tech.young.data.model.GetProfileApiResponse.GetProfileApiResponseData
import com.tech.young.data.model.UpdateUserProfileResponse
import com.tech.young.databinding.BotttomSheetTopicsBinding
import com.tech.young.databinding.FragmentFinanceInfoBinding
import com.tech.young.databinding.ItemLayoutDropDownBinding
import com.tech.young.databinding.ItemLayoutRvCategoryBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class FinanceInfoFragment : BaseFragment<FragmentFinanceInfoBinding>(),
    BaseCustomBottomSheet.Listener {
    private val viewModel: YourProfileVM by viewModels()

    // data
    private var profileData: GetProfileApiResponseData? = null
    private var isInvestmentSelected: Boolean = false
    private var isRetirementSelected: Boolean = false
    private var isEstateSelected: Boolean = false

    // common bottom sheet & adapter
    private var type: String? = null
    private lateinit var commonBottomSheet: BaseCustomBottomSheet<BotttomSheetTopicsBinding>
    private lateinit var commonAdapter: SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    private lateinit var selectedCategoryAdapter: SimpleRecyclerViewAdapter<DropDownData, ItemLayoutRvCategoryBinding>
    private var selectedCategory = ArrayList<DropDownData>()

    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_finance_info
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView() {
        profileData = arguments?.getParcelable("profileData")
        initBottomSheet()
        initAdapter()
        setupYesNoToggleForInvestment()
        setupYesNoToggleForRetirment()
        setupYesNoToggleForEstate()
        if (profileData != null) {
            binding.bean = profileData
            val topicList = profileData?.user?.topicsOfInterest
            selectedCategory = ArrayList(
                topicList?.map { DropDownData(it.toString()) }!!
            )
            selectedCategoryAdapter.list = selectedCategory
            selectedCategoryAdapter.notifyDataSetChanged()
        }
    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                R.id.etYears -> {
                    type = "1"
                    commonAdapter.list = getYearsEmployed()
                    commonBottomSheet.show()
                }

                R.id.etSalary -> {
                    type = "2"
                    commonAdapter.list = getSalaryRange()
                    commonBottomSheet.show()
                }

                R.id.etFinance -> {
                    type = "3"
                    commonAdapter.list = getFinancialExpList()
                    commonBottomSheet.show()
                }

                R.id.etLowRisk -> {
                    type = "4"
                    commonAdapter.list = getRiskToleranceList()
                    commonBottomSheet.show()
                }

                R.id.ivDrop -> {
                    type = "5"
                    val modelList: List<DropDownData> = topicsOfInterest.map { title ->
                        DropDownData(title = title)
                    }
                    commonAdapter.list = modelList
                    commonBottomSheet.show()
                }

                R.id.tvSaveFinance -> {
                    // handle click
                    apiCall()
                }
            }
        }
    }

    /** handle observer **/
    private fun initObserver() {
        viewModel.observeCommon.observe(requireActivity()) {
            when (it?.status) {
                Status.LOADING -> showLoading()
                Status.SUCCESS -> {
                    hideLoading()
                    when (it.message) {
                        "updateProfile" -> {
                            try {
                                val myDataModel: UpdateUserProfileResponse? =
                                    BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null) {
                                    if (myDataModel.data != null) {
                                        showToast(myDataModel.message.toString())
                                        requireActivity().onBackPressedDispatcher.onBackPressed()
                                    } else {
                                        showToast(myDataModel.message.toString())
                                    }
                                } else {
                                    showToast(it.message.toString())
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                    }
                }

                Status.ERROR -> {
                    hideLoading()
                    try {
                        showToast(it.message.toString())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                else -> {

                }
            }
        }

    }

    private fun setupYesNoToggleForInvestment() {
        binding.yesOptionInvestment.label.text = "Yes"
        binding.noOptionInvestment.label.text = "No"
        binding.yesOptionInvestment.box.setOnClickListener {
            isInvestmentSelected = true
            binding.yesOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.noOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
        binding.noOptionInvestment.box.setOnClickListener {
            isInvestmentSelected = false
            binding.noOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.yesOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
        if (profileData != null) {
            if (profileData?.user?.investmentAccounts == true) {
                isInvestmentSelected = true
                binding.yesOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_selected)
                binding.noOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_unselected)
            } else {
                isInvestmentSelected = false
                binding.noOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_selected)
                binding.yesOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_unselected)
            }
        } else {
            isInvestmentSelected = false
            binding.noOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.yesOptionInvestment.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
    }

    private fun setupYesNoToggleForRetirment() {
        binding.yesOptionRetirement.label.text = "Yes"
        binding.noOptionRetirement.label.text = "No"
        binding.yesOptionRetirement.box.setOnClickListener {
            isRetirementSelected = true
            binding.yesOptionRetirement.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.noOptionRetirement.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
        binding.noOptionRetirement.box.setOnClickListener {
            isRetirementSelected = false
            binding.noOptionRetirement.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.yesOptionRetirement.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }

        if (profileData != null) {
            if (profileData?.user?.retirement == true) {
                isRetirementSelected = true
                binding.noOptionRetirement.box.setBackgroundResource(R.drawable.ic_check_unselected)
                binding.yesOptionRetirement.box.setBackgroundResource(R.drawable.ic_check_selected)
            } else {
                isRetirementSelected = false
                binding.noOptionRetirement.box.setBackgroundResource(R.drawable.ic_check_selected)
                binding.yesOptionRetirement.box.setBackgroundResource(R.drawable.ic_check_unselected)
            }
        } else {
            isRetirementSelected = false
            binding.noOptionRetirement.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.yesOptionRetirement.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
    }

    private fun setupYesNoToggleForEstate() {
        binding.yesOptionRealEstate.label.text = "Yes"
        binding.noOptionRealEstate.label.text = "No"
        binding.yesOptionRealEstate.box.setOnClickListener {
            isEstateSelected = true
            binding.yesOptionRealEstate.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.noOptionRealEstate.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
        binding.noOptionRealEstate.box.setOnClickListener {
            isEstateSelected = false
            binding.noOptionRealEstate.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.yesOptionRealEstate.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
        if (profileData != null) {
            if (profileData?.user?.retirement == true) {
                isEstateSelected = true
                binding.noOptionRealEstate.box.setBackgroundResource(R.drawable.ic_check_unselected)
                binding.yesOptionRealEstate.box.setBackgroundResource(R.drawable.ic_check_selected)
            } else {
                isEstateSelected = false
                binding.noOptionRealEstate.box.setBackgroundResource(R.drawable.ic_check_selected)
                binding.yesOptionRealEstate.box.setBackgroundResource(R.drawable.ic_check_unselected)
            }
        } else {
            isEstateSelected = false
            binding.noOptionRealEstate.box.setBackgroundResource(R.drawable.ic_check_selected)
            binding.yesOptionRealEstate.box.setBackgroundResource(R.drawable.ic_check_unselected)
        }
    }

    /** handle bottom sheets **/
    private fun initBottomSheet() {
        // years bottom sheet
        commonBottomSheet =
            BaseCustomBottomSheet(requireContext(), R.layout.botttom_sheet_topics, this)
        commonBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        commonBottomSheet.behavior.isDraggable = true
    }

    override fun onViewClick(view: View?) {

    }

    /** handle adapter **/
    private fun initAdapter() {
        // years adapter
        commonAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.consMain, R.id.title -> {
                        if (type != null) {
                            when (type) {
                                "1" -> binding.etYears.text = m.title
                                "2" -> binding.etSalary.text = m.title
                                "3" -> binding.etFinance.text = m.title
                                "4" -> binding.etLowRisk.text = m.title
                                "5" -> {
                                    if (!selectedCategory.contains(m)) {
                                        selectedCategory.add(m)
                                        selectedCategoryAdapter.list = selectedCategory
                                        selectedCategoryAdapter.notifyDataSetChanged()

                                        Log.i("dasdasdasd", "initAdapter: $selectedCategory")
                                    }
                                }
                            }
                        }
                        commonBottomSheet.dismiss()
                    }
                }
            }
        commonBottomSheet.binding.rvTopics.adapter = commonAdapter

        selectedCategoryAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_rv_category, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.ivCross -> {
                        selectedCategory.removeAt(pos)
                        selectedCategoryAdapter.list = selectedCategory
                        selectedCategoryAdapter.notifyDataSetChanged()
                    }
                }
            }

        binding.rvSelectedCategory.adapter = selectedCategoryAdapter
        selectedCategoryAdapter.list = selectedCategory
        selectedCategoryAdapter.notifyDataSetChanged()
    }

    private fun apiCall() {
        val data = HashMap<String, RequestBody>()
        var selectedActualValues = ""
        if (!selectedCategory.isNullOrEmpty()) {
            selectedActualValues = selectedCategory.joinToString(",") { it.title }
            Log.i("dsadas", "initOnClick: $selectedActualValues")

        }

        data["occupation"] = binding.etOccupation.text.toString().toRequestBody()
        data["yearsEmployed"] = binding.etYears.text.toString().toRequestBody()
        data["salaryRange"] = binding.etSalary.text.toString().toRequestBody()
        data["financialExperience"] = binding.etFinance.text.toString().toRequestBody()
        data["riskTolerance"] = binding.etLowRisk.text.toString().toRequestBody()
        data["investmentAccounts"] = isInvestmentSelected.toString().toRequestBody()
        data["retirement"] = isRetirementSelected.toString().toRequestBody()
        data["investmentRealEstate"] = isEstateSelected.toString().toRequestBody()
        data["topicsOfInterest"] = selectedActualValues.toRequestBody()
        data["goals"] = binding.etGoals.text.toString().toRequestBody()
        viewModel.updateProfile(Constants.UPDATE_USER, data, null)
    }

}