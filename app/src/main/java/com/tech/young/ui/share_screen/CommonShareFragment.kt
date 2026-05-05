package com.tech.young.ui.share_screen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.showToast
import com.tech.young.data.api.Constants
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentCommonShareBinding
import com.github.dhaval2404.imagepicker.util.FileUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tech.young.base.permission.PermissionHandler
import com.tech.young.base.permission.Permissions
import com.tech.young.base.utils.BaseCustomBottomSheet
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.data.DropDownData
import com.tech.young.data.model.CreatePostApiResponse
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.data.model.MenuItem
import com.tech.young.data.model.ShareData
import com.tech.young.databinding.BottomSheetCameraGalleryBinding
import com.tech.young.databinding.BotttomSheetTopicsBinding
import com.tech.young.databinding.ItemLayoutDropDownBinding
import com.tech.young.ui.ecosystem.EcosystemFragment
import com.tech.young.ui.exchange.ExchangeFragment
import com.tech.young.ui.share_screen.share_confirmation.ShareConfirmationFragment
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@AndroidEntryPoint
class CommonShareFragment : BaseFragment<FragmentCommonShareBinding>() ,BaseCustomBottomSheet.Listener{
    private val viewModel: ShareVM by viewModels()


    private var isSymbolRequired = false
    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>
    private lateinit var topicBottomSheet : BaseCustomBottomSheet<BotttomSheetTopicsBinding>
    private lateinit var topicAdapter : SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    private var topicList = ArrayList<DropDownData>()
    private lateinit var cameraGalleryBottomSheet: BaseCustomBottomSheet<BottomSheetCameraGalleryBinding>

    private var selectedOption: String ? = null  // default

    private lateinit var adapter: ExpandableMenuAdapter
    private lateinit var menuList: MutableList<MenuItem>

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    //// camera
    private var photoFile: File? = null
    private var imageUri : Uri? = null

    private var selectedTopicsJson: String = ""

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

        viewModel.getAds(Constants.GET_ADS)
        // click
        initOnClick()
        // observer
        initObserver()


        binding.tabLayoutBottom.tabExchange.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, ExchangeFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.tabLayoutBottom.tabEcosystem.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, EcosystemFragment())
                .addToBackStack(null)
                .commit()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.nestedScrollView) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            // Only adjust padding if keyboard is visible
            view.setPadding(0, 0, 0, imeHeight)
            insets
        }

    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_common_share
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }



    private fun setupRecycler() {

        menuList = getMenuList()

        adapter = ExpandableMenuAdapter(menuList) { selectedItems  ->
            // 👉 Group topics by header
            val groupedTopics = mutableMapOf<String, MutableList<String>>()
            val fullList = getFullMenuList()

            selectedItems.forEach { child ->
                val parentHeader = fullList.find { header ->
                    header.type == MenuItem.HEADER && header.children.any { it.title == child.title }
                }?.title ?: "Other"

                val roleKey = getHeaderKey(parentHeader)

                if (!groupedTopics.containsKey(roleKey)) {
                    groupedTopics[roleKey] = mutableListOf()
                }
                groupedTopics[roleKey]?.add(child.title)
            }

            if (groupedTopics.isEmpty()) {
                selectedTopicsJson = ""
                binding.etTopic.setText("")
            } else {
                // Build JSON string manually
                val jsonEntries = groupedTopics.map { (header, children) ->
                    "\"$header\": [${children.joinToString(", ") { "\"$it\"" }}]"
                }
                selectedTopicsJson = "{ ${jsonEntries.joinToString(", ")} }"

                val displayValues = selectedItems.joinToString(", ") { it.title }
                binding.etTopic.setText(displayValues)
            }
        }

        binding.rvTopics.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTopics.adapter = adapter
    }

    private fun initBottomsheet() {
        topicBottomSheet = BaseCustomBottomSheet(requireContext(),R.layout.botttom_sheet_topics,this)

        topicBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        topicBottomSheet.behavior.isDraggable = true



        cameraGalleryBottomSheet =
            BaseCustomBottomSheet(requireContext(), R.layout.bottom_sheet_camera_gallery) {
                when (it.id) {
                    R.id.openCamara, R.id.openCamaraImage -> {
                        openCamera()
                        cameraGalleryBottomSheet.dismiss()
                    }

                    R.id.icon_emoji_new, R.id.tvChooseFromGallery -> {
                        if (!BindingUtils.hasPermissions(
                                requireContext(),
                                BindingUtils.permissions
                            )
                        ) {
                            permissionResultLauncher.launch(BindingUtils.permissions)
                        } else {
                            selectImage()
                        }
                        cameraGalleryBottomSheet.dismiss()
                    }
                }

            }
        cameraGalleryBottomSheet.behavior.isDraggable = true
        cameraGalleryBottomSheet.setCancelable(true)

    }

    /** handle view **/
    private fun  initView() {
        initBottomsheet()
        galleryLauncher()

        getTopicsList()
        setupRecycler()
        initAdapter()
        setupToggle()

        binding.etDescription.setOnTouchListener { v, event ->
            val parent = v.parent ?: return@setOnTouchListener false  // Safely accessing the parent
            parent.requestDisallowInterceptTouchEvent(true)
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_UP -> parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }


    private fun galleryLauncher() {
        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    imageUri = data?.data
                    if (imageUri != null){
                        binding.ivUploadImage.setImageURI(imageUri)
                    }
                }
            }
    }



    private var allGranted = false
    private val permissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            for (it in permissions.entries) {
                val isGranted = it.value
                allGranted = isGranted
            }
            when {
                allGranted -> {
                    selectImage()
                }

            }
        }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(Intent.createChooser(intent, "Select Picture"))
    }


    private fun openCamera() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Permissions.check(requireContext(), Manifest.permission.CAMERA, 0, object : PermissionHandler() {
                override fun onGranted() {
                    openCameraIntent()
                }
            })
        } else {
            openCameraIntent()
        }
    }


    private fun openCameraIntent() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = BindingUtils.createImageFile(requireContext())
        val authority = "${requireContext().packageName}.provider"
        val photoURI: Uri = FileProvider.getUriForFile(
            requireContext(), authority, photoFile!!
        )
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        cameraLauncher.launch(cameraIntent)

    }

    private var cameraLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    imageUri = photoFile!!.absoluteFile.toUri()
                    binding.ivUploadImage.setImageURI(imageUri)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }



    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                R.id.tvShare -> {
                    // handle click
                    if (isEmptyField()){


                        val shareData = ShareData(
                          title = binding.etTitle.text.toString().trim(),
                            topic =  selectedTopicsJson,
                            description = binding.etDescription.text.toString().trim(),
                            symbol = selectedOption,
                            symbolValue =  binding.etSymbol.text.toString().trim(),
                            image = imageUri

                        )

                        Log.i("dssaa", "initOnClick: $shareData")

                        val fragment = ShareConfirmationFragment().apply {
                            arguments = Bundle().apply {
                                putParcelable("share_data", shareData)
                            }
                        }

                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.frameLayout, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }
                R.id.ivUploadImage ->{
                    cameraGalleryBottomSheet.show()
                }
                R.id.etTopic ->{
                }
            }
        }

    }

    /** handle observer **/
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                         hideLoading()
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "sharePost" ->{
                            val myDataModel : CreatePostApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    showToast(myDataModel.message.toString())
                                    requireActivity().onBackPressedDispatcher.onBackPressed()
                                }
                            }
                        }
                        "getAds" ->{
                            val myDataModel : GetAdsAPiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    adsAdapter.list = myDataModel.data?.ads
                                }
                            }
                        }
                    }
                }
                Status.ERROR ->{
                    hideLoading()
                    showToast(it.message.toString())
                }
                else ->{

                }
            }
        })
    }

    /** handle adapter **/
    private fun initAdapter() {
        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { _, _, _ ->
        }
        binding.rvAds.adapter = adsAdapter

        topicAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down,BR.bean){v,m,_ ->
            when(v.id){
                R.id.consMain , R.id.title->{
                    binding.etTopic.setText(m.title)
                    
                    // Update JSON for single selection
                    val fullList = getFullMenuList()
                    val parentHeader = fullList.find { header ->
                        header.children.any { it.title.equals(m.title, ignoreCase = true) }
                    }?.title ?: "Other"
                    
                    val roleKey = getHeaderKey(parentHeader)
                    selectedTopicsJson = "{ \"$roleKey\": [\"${m.title}\"] }"

                    topicBottomSheet.dismiss()
                }
            }
        }
        topicBottomSheet.binding.rvTopics.adapter = topicAdapter
        topicAdapter.list = topicList
        topicAdapter.notifyDataSetChanged()
    }


    private fun setupToggle() {
        binding.yesOption.label.text = "Stock"
        binding.noOption.label.text = "Crypto"

        selectedOption = null
        binding.etSymbol.isEnabled = false
        binding.etSymbol.alpha = 0.5f

        fun resetSelection() {
            binding.yesOption.box.setBackgroundResource(R.drawable.ic_check_unselected)
            binding.noOption.box.setBackgroundResource(R.drawable.ic_check_unselected)
            binding.etSymbol.isEnabled = false
            binding.etSymbol.alpha = 0.5f
            binding.etSymbol.clearFocus()
            selectedOption = null
        }

        binding.yesOption.box.setOnClickListener {
            if (selectedOption == "Stock") {
                isSymbolRequired = false
                // Deselect if same selected
                resetSelection()
            } else {
                isSymbolRequired = true
                selectedOption = "Stock"
                binding.etSymbol.isEnabled = true
                binding.etSymbol.alpha = 1f
                binding.yesOption.box.setBackgroundResource(R.drawable.ic_check_selected)
                binding.noOption.box.setBackgroundResource(R.drawable.ic_check_unselected)
            }
        }

        binding.noOption.box.setOnClickListener {
            if (selectedOption == "Crypto") {
                isSymbolRequired = false
                // Deselect if same selected
                resetSelection()
            } else {
                isSymbolRequired = true
                selectedOption = "Crypto"
                binding.etSymbol.isEnabled = true
                binding.etSymbol.alpha = 1f
                binding.noOption.box.setBackgroundResource(R.drawable.ic_check_selected)
                binding.yesOption.box.setBackgroundResource(R.drawable.ic_check_unselected)
            }
        }
    }



    private fun isEmptyField() : Boolean {
        if (TextUtils.isEmpty(binding.etTitle.text.toString().trim())){
            showToast("Please enter title")
            return false
        }
        if (TextUtils.isEmpty(binding.etTopic.text.toString().trim())){
            showToast("Please select topic")
            return false
        }
        if (TextUtils.isEmpty(binding.etDescription.text.toString().trim())){
            showToast("Please enter description")
            return false
        }
        if (isSymbolRequired && TextUtils.isEmpty(binding.etSymbol.text.toString().trim())) {
            showToast("Please enter symbol")
            return false
        }
        return true
    }

    override fun onViewClick(view: View?) {

    }


    private fun getTopicsList() {
        topicList.add(DropDownData("Stocks"))
        topicList.add(DropDownData("Crypto"))
        topicList.add(DropDownData("Insurance"))
        topicList.add(DropDownData("Retirement"))
        topicList.add(DropDownData("Savings"))
        topicList.add(DropDownData("Investment Management"))
        topicList.add(DropDownData("Child Education"))
        topicList.add(DropDownData("Student Loan Management"))
        topicList.add(DropDownData("Debt Management"))
        topicList.add(DropDownData("Tax Planning"))
        topicList.add(DropDownData("Financial Planning"))
        topicList.add(DropDownData("Wealth Education"))
        topicList.add(DropDownData("Estate Planning"))
        topicList.add(DropDownData("Investor"))
        topicList.add(DropDownData("Venture Capitalist"))
        topicList.add(DropDownData("Small Business"))
        topicList.add(DropDownData("Grants"))
        topicList.add(DropDownData("Loans"))
        topicList.add(DropDownData("Insurance"))
        topicList.add(DropDownData("Annuities"))
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