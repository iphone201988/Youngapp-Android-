package com.tech.young.ui.my_profile_screens.forNormal

import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
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
import com.tech.young.data.model.GetProfileApiResponse.GetProfileApiResponseData
import com.tech.young.data.model.MenuItem
import com.tech.young.data.model.UpdateUserProfileResponse
import com.tech.young.databinding.BotttomSheetTopicsBinding
import com.tech.young.databinding.FragmentFinanceInfoBinding
import com.tech.young.databinding.ItemLayoutDropDownBinding
import com.tech.young.databinding.ItemLayoutRvCategoryBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import com.tech.young.ui.share_screen.ExpandableMenuAdapter
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

    var selectedSalaryRange: DropDownData? = null
    var selectedNetWorth: DropDownData? = null

    private lateinit var commonBottomSheet: BaseCustomBottomSheet<BotttomSheetTopicsBinding>
    private lateinit var commonAdapter: SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    
    // Topic functionality from CommonShareFragment
    private lateinit var topicAdapter: ExpandableMenuAdapter
    private lateinit var menuList: MutableList<MenuItem>
    private var selectedTopicsJson: List<String> = emptyList()
    private val headerToRoleMap = mapOf(
        "Member" to "general_member",
        "Financial Advisor" to "financial_advisor",
        "Startups" to "startup",
        "Small Businesses" to "small_business",
        "VC / Investors" to "investor",
        "Insurance" to "life_insurance",
        "Brokers / Dealers" to "broker",
        "Investment Managers" to "investment_managers",
        "Financial Firm" to "financial_firm",
        "Other" to "Other"
    )

    private fun getHeaderKey(headerTitle: String): String {
        return headerToRoleMap[headerTitle] ?: headerTitle
    }

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
        setupTopicsRecyclerView()
        setupYesNoToggleForInvestment()
        setupYesNoToggleForRetirment()
        setupYesNoToggleForEstate()
        
        if (profileData != null) {
            binding.bean = profileData
            // Pre-select topics if any
            val savedTopics = profileData?.user?.topicsOfInterest?.filterNotNull()

            if (!savedTopics.isNullOrEmpty()) {
                preSelectTopics(savedTopics)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.clFinance) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            // Only adjust padding if keyboard is visible
            view.setPadding(0, 0, 0, imeHeight)
            insets
        }
    }

    private fun setupTopicsRecyclerView() {
        menuList = getMenuList()

        topicAdapter = ExpandableMenuAdapter(menuList) { selectedItems ->

            if (selectedItems.isEmpty()) {
                selectedTopicsJson = emptyList()
                binding.etTopic.setText("")
            } else {
                selectedTopicsJson = selectedItems.map { it.title }
                binding.etTopic.setText(selectedTopicsJson.joinToString(", "))
            }
        }

        binding.rvTopics.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTopics.adapter = topicAdapter
    }

    private fun preSelectTopics(savedTopics: List<String>) {
        val trimmedTopics = savedTopics.map { it.trim() }
        val savedSet = trimmedTopics.map { it.lowercase() }.toSet()

        menuList.forEach { header ->
            header.children.forEach { child ->
                if (savedSet.contains(child.title.trim().lowercase())) {
                    child.isSelected = true
                }
            }
        }
        
        if (::topicAdapter.isInitialized) {
            topicAdapter.notifyDataSetChanged()
        }

        // Use the original saved topics (trimmed) to display in the EditText
        if (trimmedTopics.isNotEmpty()) {
            selectedTopicsJson = trimmedTopics
            binding.etTopic.setText(trimmedTopics.joinToString(", "))
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

                R.id.etSalaryRange ->{
                    type = "6"
                    commonAdapter.list = getSalaryRange()
                    commonBottomSheet.show()
                }
                R.id.etNetWorthEstimate ->{
                    type = "7"
                    commonAdapter.list = getSalaryRange()
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
            if (profileData?.user?.investmentRealEstate == true) {
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
                                "3" -> binding.etFinance.text = m.title
                                "4" -> binding.etLowRisk.text = m.title
                                "6" -> binding.etSalaryRange.setText(m.title)
                                "7" -> {
                                    binding.etNetWorthEstimate.setText(m.title)
                                    selectedNetWorth = m
                                }
                            }
                        }
                        commonBottomSheet.dismiss()
                    }
                }
            }
        commonBottomSheet.binding.rvTopics.adapter = commonAdapter
    }

    private fun apiCall() {

        val data = HashMap<String, RequestBody>()

        val selectedItems = menuList
            .flatMap { it.children }
            .filter { it.isSelected }

        val topicsOfInterestValue = if (selectedTopicsJson.isNotEmpty()) {
            selectedTopicsJson
        } else {
            selectedItems.map { it.title }
        }

        // ✅ Convert List → JSON string
        val topicsJson = Gson().toJson(topicsOfInterestValue)

        data["occupation"] = binding.etOccupation.text.toString().toRequestBody()
        data["yearsEmployed"] = binding.etYears.text.toString().toRequestBody()
        data["salaryRange"] = binding.etSalary.text.toString().toRequestBody()
        data["financialExperience"] = binding.etFinance.text.toString().toRequestBody()
        data["riskTolerance"] = binding.etLowRisk.text.toString().toRequestBody()
        data["investmentAccounts"] = isInvestmentSelected.toString().toRequestBody()
        data["retirement"] = isRetirementSelected.toString().toRequestBody()
        data["investmentRealEstate"] = isEstateSelected.toString().toRequestBody()

        // ✅ FINAL FIX HERE
        data["topicsOfInterest"] = topicsJson.toRequestBody()

        data["goals"] = binding.etGoals.text.toString().toRequestBody()
        data["salaryRange"] = binding.etSalaryRange.text.toString().toRequestBody()

        selectedNetWorth?.actualValue?.let {
            data["netWorthEstimate"] = it.toRequestBody()
        }

        viewModel.updateProfile(Constants.UPDATE_USER, data, null, null)
    }

    private fun getFullMenuList(): List<MenuItem> {
        return listOf(
            MenuItem("Member", MenuItem.HEADER, children = listOf(
                MenuItem("Stocks", MenuItem.CHILD, true),
                MenuItem("Crypto", MenuItem.CHILD, true),
                MenuItem("Insurance", MenuItem.CHILD, true),
                MenuItem("Savings", MenuItem.CHILD, true),
                MenuItem("Investment Management", MenuItem.CHILD, true),
                MenuItem("Child Education", MenuItem.CHILD, true),
                MenuItem("Student Loan Management", MenuItem.CHILD, true),
                MenuItem("Debt Management", MenuItem.CHILD, true),
                MenuItem("Tax Planning", MenuItem.CHILD, true),
                MenuItem("Financial Planning", MenuItem.CHILD, true),
                MenuItem("Wealth Education", MenuItem.CHILD, true),
                MenuItem("Estate Planning", MenuItem.CHILD, true),
                MenuItem("Investor", MenuItem.CHILD, true),
                MenuItem("Venture Capitalist", MenuItem.CHILD, true),
                MenuItem("Small Business", MenuItem.CHILD, true),
                MenuItem("Grants", MenuItem.CHILD, true),
                MenuItem("Loans", MenuItem.CHILD, true),
                MenuItem("Insurance", MenuItem.CHILD, true),
                MenuItem("Annuities", MenuItem.CHILD, true),
            )),

            MenuItem("Financial Advisor", MenuItem.HEADER, children = listOf(
                MenuItem("Retirement Planning", MenuItem.CHILD, true),
                MenuItem("Estate & Legacy", MenuItem.CHILD, true),
                MenuItem("Tax Strategy", MenuItem.CHILD, true),
                MenuItem("Education Funding", MenuItem.CHILD, true),
                MenuItem("Financial Literacy", MenuItem.CHILD, true),
                MenuItem("Risk Management", MenuItem.CHILD, true),
                MenuItem("Behavioral Finance", MenuItem.CHILD, true),
                MenuItem("Holistic Wealth", MenuItem.CHILD, true),
                MenuItem("Charitable Giving", MenuItem.CHILD, true),
                MenuItem("Client Updates", MenuItem.CHILD, true)
            )),

            MenuItem("Startups", MenuItem.HEADER, children = listOf(
                MenuItem("Fundraising", MenuItem.CHILD, true),
                MenuItem("Product Launch", MenuItem.CHILD, true),
                MenuItem("Company Culture", MenuItem.CHILD, true),
                MenuItem("Growth & Scaling", MenuItem.CHILD, true),
                MenuItem("Founder Journey", MenuItem.CHILD, true),
                MenuItem("Industry Disruptor", MenuItem.CHILD, true),
                MenuItem("Operations", MenuItem.CHILD, true),
                MenuItem("Customer Stories", MenuItem.CHILD, true),
                MenuItem("Pitch Deck", MenuItem.CHILD, true),
                MenuItem("Tech Stack", MenuItem.CHILD, true)
            )),

            MenuItem("Small Businesses", MenuItem.HEADER, children = listOf(
                MenuItem("Local Community", MenuItem.CHILD, true),
                MenuItem("Daily Operations", MenuItem.CHILD, true),
                MenuItem("Hiring & Talent", MenuItem.CHILD, true),
                MenuItem("Customer Service", MenuItem.CHILD, true),
                MenuItem("Business Finance", MenuItem.CHILD, true),
                MenuItem("Marketing & SEO", MenuItem.CHILD, true),
                MenuItem("Product/ Service Demo", MenuItem.CHILD, true),
                MenuItem("Sustainability", MenuItem.CHILD, true),
                MenuItem("Work-Life Balance", MenuItem.CHILD, true),
                MenuItem("Tools of the Trade", MenuItem.CHILD, true)
            )),

            MenuItem("VC / Investors", MenuItem.HEADER, children = listOf(
                MenuItem("Deal Flow", MenuItem.CHILD, true),
                MenuItem("Market Trends", MenuItem.CHILD, true),
                MenuItem("Portfolio Update", MenuItem.CHILD, true),
                MenuItem("Investment Strategy", MenuItem.CHILD, true),
                MenuItem("Due Diligence", MenuItem.CHILD, true),
                MenuItem("Exit Events", MenuItem.CHILD, true),
                MenuItem("Limited Partners (LP)", MenuItem.CHILD, true),
                MenuItem("Valuation", MenuItem.CHILD, true),
                MenuItem("Economic Outlook", MenuItem.CHILD, true),
                MenuItem("Mentorship", MenuItem.CHILD, true)
            )),

            MenuItem("Insurance", MenuItem.HEADER, children = listOf(
                MenuItem("Estate Planning", MenuItem.CHILD, true),
                MenuItem("Policy Types", MenuItem.CHILD, true),
                MenuItem("Retirement Income", MenuItem.CHILD, true),
                MenuItem("Claims Stories", MenuItem.CHILD, true),
                MenuItem("Wellness & Longevity", MenuItem.CHILD, true),
                MenuItem("Underwriting", MenuItem.CHILD, true),
                MenuItem("Financial Literacy", MenuItem.CHILD, true),
                MenuItem("Family Security", MenuItem.CHILD, true),
                MenuItem("Tax Advantages", MenuItem.CHILD, true),
                MenuItem("Agent Spotlight", MenuItem.CHILD, true)
            )),

            MenuItem("Brokers / Dealers", MenuItem.HEADER, children = listOf(
                MenuItem("Compliance & Regs", MenuItem.CHILD, true),
                MenuItem("Market Liquidity", MenuItem.CHILD, true),
                MenuItem("Trading Platform", MenuItem.CHILD, true),
                MenuItem("Asset Classes", MenuItem.CHILD, true),
                MenuItem("Wealth Management", MenuItem.CHILD, true),
                MenuItem("Risk Management", MenuItem.CHILD, true),
                MenuItem("Finfluencer Ethics", MenuItem.CHILD, true),
                MenuItem("Clearing & Settlement", MenuItem.CHILD, true),
                MenuItem("Fee Structures", MenuItem.CHILD, true),
                MenuItem("Client Education", MenuItem.CHILD, true)
            )),

            MenuItem("Investment Managers", MenuItem.HEADER, children = listOf(
                MenuItem("Market Outlook", MenuItem.CHILD, true),
                MenuItem("Asset Allocation", MenuItem.CHILD, true),
                MenuItem("Fixed Income", MenuItem.CHILD, true),
                MenuItem("Sustainable Finance", MenuItem.CHILD, true),
                MenuItem("Alternative Assets", MenuItem.CHILD, true),
                MenuItem("Portfolio Rebalancing", MenuItem.CHILD, true),
                MenuItem("Sector Analysis", MenuItem.CHILD, true),
                MenuItem("Dividend Growth", MenuItem.CHILD, true),
                MenuItem("Emerging Markets", MenuItem.CHILD, true),
                MenuItem("Economic Indicators", MenuItem.CHILD, true)
            )),

            MenuItem("Financial Firm", MenuItem.HEADER, children = listOf(
                MenuItem("Retirement Planning", MenuItem.CHILD, true),
                MenuItem("Estate & Legacy", MenuItem.CHILD, true),
                MenuItem("Tax Strategy", MenuItem.CHILD, true),
                MenuItem("Education Funding", MenuItem.CHILD, true),
                MenuItem("Financial Literacy", MenuItem.CHILD, true),
                MenuItem("Risk Management", MenuItem.CHILD, true),
                MenuItem("Behavioral Finance", MenuItem.CHILD, true),
                MenuItem("Holistic Wealth", MenuItem.CHILD, true),
                MenuItem("Charitable Giving", MenuItem.CHILD, true),
                MenuItem("Client Updates", MenuItem.CHILD, true)
            ))
        )
    }

    private fun getMenuList(): MutableList<MenuItem> {
        val role = sharedPrefManager.getLoginData()?.role
        val allMenus = getFullMenuList()

        val roleToHeader = mapOf(
            "general_member" to "Member",
            "financial_advisor" to "Financial Advisor",
            "startup" to "Startups",
            "small_business" to "Small Businesses",
            "investor" to "VC / Investors",
            "life_insurance" to "Insurance",
            "broker" to "Brokers / Dealers",
            "investment_managers" to "Investment Managers",
            "financial_firm" to "Financial Firm"
        )

        val targetHeader = roleToHeader[role]

        val filteredList = if (targetHeader != null) {
            allMenus.filter { it.title == targetHeader }.toMutableList()
        } else {
            allMenus.toMutableList()
        }

        // Always add Other
        filteredList.add(MenuItem("Other", MenuItem.HEADER, children = listOf(
            MenuItem("Other", MenuItem.CHILD, true)
        )))

        return filteredList
    }

}
