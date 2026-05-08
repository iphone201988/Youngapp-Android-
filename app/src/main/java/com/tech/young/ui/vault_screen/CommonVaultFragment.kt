package com.tech.young.ui.vault_screen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
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
import com.tech.young.base.utils.BaseCustomBottomSheet
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.DropDownData
import com.tech.young.data.api.Constants
import com.tech.young.data.model.GetUserApiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.BottomSheetCategoryBinding
import com.tech.young.databinding.BotttomSheetTopicsBinding
import com.tech.young.databinding.FragmentCommonVaultBinding
import com.tech.young.databinding.ItemLayoutDropDownBinding
import com.tech.young.databinding.ItemLayoutRvCategoryBinding
import com.tech.young.ui.common.CommonActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.util.FileUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tech.young.base.permission.PermissionHandler
import com.tech.young.base.permission.Permissions
import com.tech.young.data.model.CreateVaultApiResponse
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.data.model.MenuItem
import com.tech.young.databinding.BottomSheetCameraGalleryBinding
import com.tech.young.ui.ecosystem.EcosystemFragment
import com.tech.young.ui.exchange.ExchangeFragment
import com.tech.young.ui.my_profile_screens.common_ui.EditProfileDetailFragment
import com.tech.young.ui.share_screen.ExpandableMenuAdapter
import com.tech.young.ui.vault_screen.people_screen.PeopleFragment
import com.tech.young.ui.vault_screen.vault_room.VaultRoomFragment
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@AndroidEntryPoint
class CommonVaultFragment : BaseFragment<FragmentCommonVaultBinding>()  , BaseCustomBottomSheet.Listener{
    private val viewModel: VaultVM by viewModels()

    private lateinit var adapter: ExpandableMenuAdapter
    private lateinit var menuList: MutableList<MenuItem>

    private var pdfUri: Uri? = null


    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>
    private lateinit var topicBottomSheet : BaseCustomBottomSheet<BotttomSheetTopicsBinding>
    private lateinit var categoryBottomSheet : BaseCustomBottomSheet<BottomSheetCategoryBinding>
    private lateinit var cameraGalleryBottomSheet: BaseCustomBottomSheet<BottomSheetCameraGalleryBinding>

    private lateinit var topicAdapter : SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    private lateinit var categoryAdapter : SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    private lateinit var selectedCategoryAdapter : SimpleRecyclerViewAdapter<DropDownData, ItemLayoutRvCategoryBinding>
    private var topicList = ArrayList<DropDownData>()
    private var categoryList = ArrayList<DropDownData>()
    private var selectedCategory = ArrayList<DropDownData>()
    private var selectedPlayer = ArrayList<String>()

    private var visibilityMode : String = "private"
    private var isFoundersRoom : Boolean = false

    private var selectedTopicsJson: String = ""
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var selectedImagePart: MultipartBody.Part? = null

    //// camera
    private var photoFile: File? = null
    private var photoURI: Uri? = null
    private var imageUri : Uri? = null

    private val roleToHeaderMap = mapOf(
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

    private fun getHeaderKey(headerTitle: String): String {
        return roleToHeaderMap.entries.find { it.value == headerTitle }?.key ?: headerTitle
    }


    companion object{
        var selectedUserId: String = ""
        var reload   = false
    }

    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()

        val role =  sharedPrefManager.getLoginData()?.role
        val isFounderRole = role in setOf("small_business", "startup", "investor")

        binding.setFounderDefault.visibility =
            if (isFounderRole) View.VISIBLE else View.GONE

        binding.etFounderMode.visibility =
            if (isFounderRole) View.VISIBLE else View.GONE

        binding.setFounderDefault.setOnCheckedChangeListener { _, isChecked ->
            isFoundersRoom = isChecked
        }
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_common_vault
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView() {
        initBottomsheet()
        galleryLauncher()
        viewModel.getAds(Constants.GET_ADS)
        getTopicsList()

        Log.i("reload", "initView: $reload")
        if (!reload){
            selectedCateGoryList()
            reload = false
        }

        getCategoryList()
        initAdapter()
        setupRecycler()

        binding.setDefault.setOnCheckedChangeListener { _, isChecked ->
             visibilityMode = if (isChecked) "public" else "private"
            Log.d("SwitchValue", "Visibility mode: $visibilityMode")

            // Optionally store it somewhere
            // myViewModel.visibilityMode.value = visibilityMode
        }

        binding.etDescription.setOnTouchListener { v, event ->
            val parent = v.parent ?: return@setOnTouchListener false  // Safely accessing the parent
            parent.requestDisallowInterceptTouchEvent(true)
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_UP -> parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }


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

    private fun selectedCateGoryList() {
        selectedCategory.add(DropDownData("Members","general_member"))
    }


    private fun getCategoryList() {
        categoryList.add(DropDownData("Members", "general_member"))
        categoryList.add(DropDownData("Advisors", "financial_advisor"))
        categoryList.add(DropDownData("Startups", "startup"))
        categoryList.add(DropDownData("Small Businesses", "small_business"))
        categoryList.add(DropDownData("Insurance", "investor"))
        categoryList.add(DropDownData("Firm", "financial_firm"))

    }

    private fun initBottomsheet() {
        topicBottomSheet = BaseCustomBottomSheet(requireContext(),R.layout.botttom_sheet_topics,this)

        topicBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        topicBottomSheet.behavior.isDraggable = true


        categoryBottomSheet = BaseCustomBottomSheet(requireContext(),R.layout.bottom_sheet_category,this)

        categoryBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        categoryBottomSheet.behavior.isDraggable = true


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

    private fun galleryLauncher() {
        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    imageUri = data?.data
                    if (imageUri != null){
                        binding.ivUploadImage.setImageURI(imageUri)
                    }

//                    if (imageUri != null) {
//                        selectedImagePart = BindingUtils.createImageMultipartFromUri(
//                            requireContext(),
//                            imageUri!!,
//                            "file" // <-- or any string key like "profile", "photo"
//                        )
//                        Log.i("ImageUpload", "galleryLauncher: $imageUri")
//                    }
                }
            }
    }

    private var allGranted = false
    private val permissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            for (it in permissions.entries) {
                it.key
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
                    if (imageUri != null){
                        binding.ivUploadImage.setImageURI(imageUri)
                    }
//                    if (photoURI != null) {
//                        selectedImagePart = BindingUtils.createImageMultipartFromUri(
//                            requireContext(),
//                            photoURI!!,
//                            "file" // or appropriate key
//                        )
//                        Log.i("ImageUpload", "cameraLauncher: $photoURI")
//                    }
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
                R.id.ivUploadImage ->{
//                    ImagePicker.with(this)
//                        .compress(1024)
//                        .maxResultSize(1080, 1080)
//                        .createIntent { intent ->
//                            startForImageResult.launch(intent)
//                        }

                    cameraGalleryBottomSheet.show()
                }

                R.id.ivUploadDeck ->{
                    pickPdfLauncher.launch(arrayOf("application/pdf"))

                }

                R.id.tvCreate -> {

                    if (isEmptyField()){
                        val selectedActualValues = selectedCategory.joinToString(",") { it.actualValue }

                        val parts = mutableListOf<MultipartBody.Part>()

                        // Image
                        imageUri?.let {
                            convertImageToMultipart(it)?.let { part ->
                                parts.add(part)
                            }
                        }

                        // PDF
                        pdfUri?.let {
                            convertPdfToMultipart(it)?.let { part ->
                                parts.add(part)
                            }
                        }


                        Log.i("dsadas", "initOnClick: $selectedActualValues")
//                        val multipartImage = imageUri?.let { convertImageToMultipart(it) }
//                        val multipartPdf = pdfUri?.let { convertPdfToMultipart(it) }

                        val data = HashMap<String, RequestBody>()
                        data["title"] = binding.etTitle.text.toString().trim().toRequestBody()
                        data["topic"] = selectedTopicsJson.toRequestBody()
                        data["description"] = binding.etDescription.text.toString().trim().toRequestBody()
                        data["access"] = visibilityMode.toRequestBody()
                        data["members"] = selectedUserId.toRequestBody()
                        data["category"] = selectedActualValues.toRequestBody()
                        data["isFoundersRoom"] = isFoundersRoom.toString().toRequestBody()

                        viewModel.createVault(
                            data,
                            Constants.CREATE_VAULT,
                            parts
                        )                    }


                }
                R.id.etUsers -> {
                    selectedUserId = ""
//                    val intent = Intent(requireContext(), CommonActivity::class.java).apply {
//                        putExtra("from", "people")
//                        putParcelableArrayListExtra("selectedCategory", ArrayList(selectedCategory))
//                    }
//                    startActivity(intent)

                    val fragment = PeopleFragment().apply {
                        arguments = Bundle().apply {
                            putParcelableArrayList("selectedCategory", ArrayList(selectedCategory))
                        }
                    }


                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
                }

                R.id.etTopic -> {

                }
                R.id.etCategory ->{
                    categoryBottomSheet.show()
                }
            }
        }

    }


    private val pickPdfLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                pdfUri = it
                showPdfThumbnail(it)
            }
        }


    private fun showPdfThumbnail(uri: Uri) {
        try {
            val fileDescriptor = requireContext().contentResolver.openFileDescriptor(uri, "r")
            val pdfRenderer = PdfRenderer(fileDescriptor!!)

            val page = pdfRenderer.openPage(0)

            val bitmap = Bitmap.createBitmap(
                page.width,
                page.height,
                Bitmap.Config.ARGB_8888
            )

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            binding.ivUploadDeck.setImageBitmap(bitmap)

            page.close()
            pdfRenderer.close()
            fileDescriptor.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun convertPdfToMultipart(uri: Uri): MultipartBody.Part? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().cacheDir, "pitch_deck.pdf")

            file.outputStream().use { output ->
                inputStream?.copyTo(output)
            }

            val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("pitchDeck", file.name, requestFile)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
//    private val startForImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
//        try {
//            val resultCode = result.resultCode
//            val data = result.data
//            if (resultCode == Activity.RESULT_OK) {
//                val fileUri = data?.data
//                imageUri = fileUri
//                binding.ivUploadImage.setImageURI(fileUri)
//
//                //  Log.i("dasd", ": $imageUri")
//            } else if (resultCode == ImagePicker.RESULT_ERROR) {
//
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }



    /** handle observer **/
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                         showLoading()
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "createVault"  ->{
                            val myDataModel : CreateVaultApiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.vault != null){
                                    showToast(myDataModel.message.toString())
                                    val fragment = VaultRoomFragment().apply {
                                        arguments = Bundle().apply {
                                            putString("vaultId", myDataModel.vault._id)
                                        }
                                    }

                                    requireActivity().supportFragmentManager.beginTransaction()
                                        .replace(R.id.frameLayout, fragment)
                                        .addToBackStack(null)
                                        .commit()
                                }


                            }
                        }
                        "getAds" ->{
                            val myDataModel : GetAdsAPiResponse ? = BindingUtils.parseJson(it.data.toString())
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
                }
                else ->{

                }
            }
        })

    }



    private fun setupRecycler() {

        menuList = getMenuList()

//        adapter = ExpandableMenuAdapter(menuList) { item ->
//            // 👉 Handle child click
//       //     binding.etTopic.setText(item.title)
//        }


        adapter = ExpandableMenuAdapter(menuList) { selectedItems  ->
            // 👉 Group topics by header
            val groupedTopics = mutableMapOf<String, MutableList<String>>()
            selectedItems.forEach { child ->
                val parentHeader = menuList.find { header ->
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

                // Comma separated string
                selectedTopicsJson = groupedTopics
                    .flatMap { (_, children) -> children }
                    .joinToString(",")

                val displayValues = selectedItems.joinToString(", ") { it.title }
                binding.etTopic.setText(displayValues)
            }
        }
        
        
        
        binding.rvTopics.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTopics.adapter = adapter
    }


    /** handle adapter **/
    private fun initAdapter() {
        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        binding.rvAds.adapter = adsAdapter


        topicAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down,BR.bean){v,m,pos ->
            when(v.id){
                R.id.consMain , R.id.title->{
                    binding.etTopic.setText(m.title)
                    
                    val parentHeader = getMenuList().find { header ->
                        header.children.any { it.title.equals(m.title, ignoreCase = true) }
                    }?.title ?: "Other"

//                    val roleKey = getHeaderKey(parentHeader)
//                    selectedTopicsJson = "{ \"$roleKey\": [\"${m.title}\"] }"

                    topicBottomSheet.dismiss()
                }
            }
        }
        topicBottomSheet.binding.rvTopics.adapter = topicAdapter
        topicAdapter.list = topicList
        topicAdapter.notifyDataSetChanged()

        categoryAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down,BR.bean){v,m,pos ->
            when(v.id){
                R.id.consMain , R.id.title->{
                    if (!selectedCategory.contains(m)) {
                        selectedCategory.add(m)
                        selectedCategoryAdapter.list = selectedCategory
                        selectedCategoryAdapter.notifyDataSetChanged()

                        Log.i("dasdasdasd", "initAdapter: $selectedCategory")
                    }
                    categoryBottomSheet.dismiss()
                }
            }
        }
        categoryBottomSheet.binding.rvCategories.adapter = categoryAdapter
        categoryAdapter.list = categoryList
        categoryAdapter.notifyDataSetChanged()


        selectedCategoryAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_rv_category,BR.bean){v,m,pos ->
            when(v.id){
                R.id.ivCross ->{
                    selectedCategory.removeAt(pos)
                    selectedCategoryAdapter.list = selectedCategory
                    selectedCategoryAdapter.notifyDataSetChanged()
                }
            }
        }

        binding.rvSelectedCategory.adapter = selectedCategoryAdapter
        selectedCategoryAdapter.list = selectedCategory
        categoryAdapter.notifyDataSetChanged()
    }

    private var getList = listOf(
        "", "", "", "", ""
    )

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


    private fun convertImageToMultipart(imageUri: Uri): MultipartBody.Part {
        val file = FileUtil.getTempFile(requireContext(), imageUri)
        return MultipartBody.Part.createFormData(
            "image",
            file!!.name,
            file.asRequestBody("image/png".toMediaTypeOrNull())
        )
    }

    private fun isEmptyField() : Boolean{
        Log.i("dasdasdasdas", "isEmptyField: $selectedUserId")
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
//        if (selectedUserId.isEmpty()){
//            showToast("Please choose users")
//            return false
//        }

        return true
    }



    private fun getMenuList(): MutableList<MenuItem> {
        val role = sharedPrefManager.getLoginData()?.role
        val allMenus = mutableListOf(

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

        val targetHeader = roleToHeaderMap[role]

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
