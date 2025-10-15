package com.tech.young.ui.my_profile_screens.profile_fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.SocketManager
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BaseCustomDialog
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.api.Constants
import com.tech.young.data.api.SimpleApiResponse
import com.tech.young.data.model.EditProfileListModel
import com.tech.young.data.model.GetProfileApiResponse
import com.tech.young.data.model.GetProfileApiResponse.GetProfileApiResponseData
import com.tech.young.databinding.EditProfileItemViewBinding
import com.tech.young.databinding.FragmentEditProfileBinding
import com.tech.young.databinding.ItemLayoutDeleteAccountPopupBinding
import com.tech.young.databinding.ItemLayoutLogoutPopupBinding
import com.tech.young.ui.MySplashActivity
import com.tech.young.ui.change_password.ChangePasswordFragment
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.ecosystem.EcosystemFragment
import com.tech.young.ui.exchange.ExchangeFragment
import com.tech.young.ui.my_profile_screens.YourProfileVM
import com.tech.young.ui.my_profile_screens.common_ui.BusinessInfoFragment
import com.tech.young.ui.my_profile_screens.common_ui.EditProfileDetailFragment
import com.tech.young.ui.my_profile_screens.common_ui.FormUploadFragment
import com.tech.young.ui.my_profile_screens.forFinance.PersonalPreferencesFragment
import com.tech.young.ui.my_profile_screens.forFinance.ProfessionalInformationFragment
import com.tech.young.ui.my_profile_screens.forNormal.FamilyDetailsFragment
import com.tech.young.ui.my_profile_screens.forNormal.FinanceInfoFragment
import com.tech.young.ui.my_profile_screens.forNormal.InvestmentInfoFragment
import com.tech.young.ui.payment.PaymentDetailsFragment
import com.tech.young.utils.DiditService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class EditProfileFragment : BaseFragment<FragmentEditProfileBinding>(),BaseCustomDialog.Listener {
    private val viewModel: YourProfileVM by viewModels()

    private var lastLogin : String ? = null

    private var userId : String ? = null
    // adapter
    private lateinit var editProfileAdapter: SimpleRecyclerViewAdapter<EditProfileListModel, EditProfileItemViewBinding>
    private var profileData: GetProfileApiResponseData?=null
    // menu list
    private var editMenuList=ArrayList<EditProfileListModel>()

    private  var notClickable  = false
    private lateinit var logoutPopup : BaseCustomDialog<ItemLayoutLogoutPopupBinding>
    private lateinit var deleteAccountPopup : BaseCustomDialog<ItemLayoutDeleteAccountPopupBinding>
    override fun onCreateView(view: View) {
        // view
        initView()
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
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_edit_profile
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /**handle view **/
    private fun initView() {
        // handle list
        val role = sharedPrefManager.getLoginData()?.role
        viewModel.getProfile(Constants.GET_USER_PROFILE)
        Log.i("dsadasdas", "initView: $role")
        if (role != null) {
            when (role) {
                "admin" -> {
                    // Handle admin-specific logic
                    Log.d("RoleCheck", "Admin user")
                }

                "general_member" -> {
                    // Handle general member logic
                    Log.d("RoleCheck", "General member")
                    editMenuList=showList()
                }

                "financial_advisor" -> {
                    // Advisor-specific logic
                    Log.d("RoleCheck", "Financial advisor")
                    editMenuList=showListForFinancialAdvisor()


                }

                "financial_firm" -> {
                    Log.d("RoleCheck", "Financial firm")
                    editMenuList=showListForFinancialAdvisor()

                }

                "small_business" -> {
                    Log.d("RoleCheck", "Small business")
                    editMenuList=showListForStartUp()
                }

                "startup" -> {
                    Log.d("RoleCheck", "Startup")
                    editMenuList=showListForStartUp()
                }

                "investor" -> {
                    Log.d("RoleCheck", "Investor")
                    editMenuList=showListForStartUp()

                }

                else -> {
                    Log.w("RoleCheck", "Unknown role: $role")
                }
            }
        }
        // handle adapter
        initAdapter()

        // handle popup
        initPopup()
    }

    /**handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()){
            when(it?.id){

            }
        }

    }

    /** handle observer **/
    private fun initObserver() {
        viewModel.observeCommon.observe(requireActivity()){
            when(it?.status){
                Status.LOADING -> showLoading()
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "getProfile" -> {
                            val myDataModel: GetProfileApiResponse? =
                                BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                if (myDataModel.data != null) {
                                    userId = myDataModel.data?.user?._id
                                    profileData = myDataModel.data
                                    lastLogin = myDataModel.data?.user?.lastLogin
                                    val document = myDataModel.data?.user?.isDocumentVerified

                                    BindingUtils.lastLogin = lastLogin.toString()
                                    BindingUtils.documentStatus = document

                                 val verify = document?.contains("approved") == true

                                    for (i in editProfileAdapter.list){
                                        if (verify){
                                            if (i.subTitle.contains("Account Verification")){
                                               i.verificationClickable = true
                                                break
                                            }
                                        }
                                    }
                                    editProfileAdapter.notifyDataSetChanged()
                               //     initAdapter()
                                }
                            }

                        }
                        "logout" ->{
                            val myDataModel : SimpleApiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                sharedPrefManager.clear()
                                logoutPopup.dismiss()
                                SocketManager.closeConnection()
                                showToast(myDataModel.message.toString())
                                val intent = Intent(requireContext() , MySplashActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    }
                }
                Status.ERROR -> {
                    hideLoading()
                    try {
                        showToast(it.message.toString())
                    }
                    catch (e:Exception){
                        e.printStackTrace()
                    }
                }
                else -> {

                }
            }
        }
    }

    /** handle dialog **/
    private fun initPopup() {
        logoutPopup = BaseCustomDialog(requireContext() , R.layout.item_layout_logout_popup,this )
        deleteAccountPopup = BaseCustomDialog(requireContext(), R.layout.item_layout_delete_account_popup,this)
    }

    /** handle adapter **/
    private fun initAdapter() {
        editProfileAdapter =
            SimpleRecyclerViewAdapter(R.layout.edit_profile_item_view, BR.bean) { v, m, pos ->
                val date = v.rootView.findViewById<TextView>(R.id.date)
                Log.i("last", "initAdapter: $lastLogin")
                date.text = lastLogin
                when (v.id) {
                    R.id.clMain -> {
                        when(m.listType){
                            0->{
                                when (m.subTitle) {
                                    // edit profile
                                    getString(R.string.profile_details) -> {
//                                        val intent=Intent(requireContext(), CommonActivity::class.java)
//                                        intent.putExtra("profileData",profileData)
//                                        intent.putExtra("from","edit_profile")
//                                        startActivity(intent)

                                        val fragment = EditProfileDetailFragment().apply {
                                            arguments = Bundle().apply {
                                                putParcelable("profileData", profileData)
                                            }
                                        }

                                        requireActivity().supportFragmentManager.beginTransaction()
                                            .replace(R.id.frameLayout, fragment)
                                            .addToBackStack(null)
                                            .commit()

                                    }
                                    // other profile details
                                    getString(R.string.family_education) -> {
//                                        val intent=Intent(requireContext(), CommonActivity::class.java)
//                                        intent.putExtra("profileData",profileData)
//                                        intent.putExtra("from","normal_family")
//                                        startActivity(intent)

                                        val fragment = FamilyDetailsFragment().apply {
                                            arguments = Bundle().apply {
                                                putParcelable("profileData", profileData)
                                            }
                                        }

                                        requireActivity().supportFragmentManager.beginTransaction()
                                            .replace(R.id.frameLayout, fragment)
                                            .addToBackStack(null)
                                            .commit()
                                    }

                                    getString(R.string.financial_information) -> {
//                                        val intent=Intent(requireContext(), CommonActivity::class.java)
//                                        intent.putExtra("profileData",profileData)
//                                        intent.putExtra("from","normal_finance_detail")
//                                        startActivity(intent)

                                        val fragment = FinanceInfoFragment().apply {
                                            arguments = Bundle().apply {
                                                putParcelable("profileData", profileData)
                                            }
                                        }

                                        requireActivity().supportFragmentManager.beginTransaction()
                                            .replace(R.id.frameLayout, fragment)
                                            .addToBackStack(null)
                                            .commit()
                                    }

                                    "Investment Summary (Optional)" -> {
//                                        val intent=Intent(requireContext(), CommonActivity::class.java)
//                                        intent.putExtra("profileData",profileData)
//                                        intent.putExtra("from","normal_investment")
//                                        startActivity(intent)


                                        val fragment = InvestmentInfoFragment().apply {
                                            arguments = Bundle().apply {
                                                putParcelable("profileData", profileData)
                                            }
                                        }

                                        requireActivity().supportFragmentManager.beginTransaction()
                                            .replace(R.id.frameLayout, fragment)
                                            .addToBackStack(null)
                                            .commit()
                                    }

                                    getString(R.string.additional_information) -> {
//                                        val intent=Intent(requireContext(), CommonActivity::class.java)
//                                        intent.putExtra("profileData",profileData)
//                                        intent.putExtra("from","personal_preference")
//                                        startActivity(intent)

                                        val fragment = PersonalPreferencesFragment().apply {
                                            arguments = Bundle().apply {
                                                putParcelable("profileData", profileData)
                                            }
                                        }

                                        requireActivity().supportFragmentManager.beginTransaction()
                                            .replace(R.id.frameLayout, fragment)
                                            .addToBackStack(null)
                                            .commit()
                                    }

                                    getString(R.string.account_verification) -> {
                                        userVerification()
//                                        val intent =
//                                            Intent(requireContext(), CommonActivity::class.java)
//                                        intent.putExtra("from", "account_verify")
//                                        startActivity(intent)
                                    }

                                    getString(R.string.account_details) ->{
                                        if (userId != null){
                                            val fragment = PaymentDetailsFragment().apply {
                                                arguments = Bundle().apply {
                                                    putString("userId", userId)
                                                }
                                            }

                                            requireActivity().supportFragmentManager.beginTransaction()
                                                .replace(R.id.frameLayout, fragment)
                                                .addToBackStack(null)
                                                .commit()
                                        }
                                    }
                                    getString(R.string.change_password) ->{
                                            val fragment = ChangePasswordFragment().apply {
                                            }

                                            requireActivity().supportFragmentManager.beginTransaction()
                                                .replace(R.id.frameLayout, fragment)
                                                .addToBackStack(null)
                                                .commit()

                                    }
                                    "Logout"->{
                                        logoutPopup.show()
                                    }
                                    "Delete Account" ->{
                                        deleteAccountPopup.show()
                                    }
                                }
                            }
                            1->{
                                when(m.subTitle){
                                    getString(R.string.profile_details) -> {
//                                        val intent =
//                                            Intent(requireContext(), CommonActivity::class.java)
//                                        intent.putExtra("profileData",profileData)
//                                        intent.putExtra("from", "edit_profile")
//                                        startActivity(intent)


                                        val fragment = EditProfileDetailFragment().apply {
                                            arguments = Bundle().apply {
                                                putParcelable("profileData", profileData)
                                            }
                                        }

                                        requireActivity().supportFragmentManager.beginTransaction()
                                            .replace(R.id.frameLayout, fragment)
                                            .addToBackStack(null)
                                            .commit()
                                    }

                                    getString(R.string.professional_information)->{
//                                        val intent =
//                                            Intent(requireContext(), CommonActivity::class.java)
//                                        intent.putExtra("profileData",profileData)
//                                        intent.putExtra("from", "professional_info")
//                                        startActivity(intent)


                                        val fragment = ProfessionalInformationFragment().apply {
                                            arguments = Bundle().apply {
                                                putParcelable("profileData", profileData)
                                            }
                                        }

                                        requireActivity().supportFragmentManager.beginTransaction()
                                            .replace(R.id.frameLayout, fragment)
                                            .addToBackStack(null)
                                            .commit()

                                    }

                                    getString(R.string.personal_preferences)->{
//                                        val intent =
//                                            Intent(requireContext(), CommonActivity::class.java)
//                                        intent.putExtra("profileData",profileData)
//                                        intent.putExtra("from", "personal_preference")
//                                        startActivity(intent)


                                        val fragment = PersonalPreferencesFragment().apply {
                                            arguments = Bundle().apply {
                                                putParcelable("profileData", profileData)
                                            }
                                        }

                                        requireActivity().supportFragmentManager.beginTransaction()
                                            .replace(R.id.frameLayout, fragment)
                                            .addToBackStack(null)
                                            .commit()

                                    }

                                    getString(R.string.account_verification) -> {
                                        userVerification()
//                                        val intent =
//                                            Intent(requireContext(), CommonActivity::class.java)
//                                        intent.putExtra("from", "account_verify")
//                                        startActivity(intent)
                                    }
                                    getString(R.string.form_upload)->{
//                                        val intent=Intent(requireContext(), CommonActivity::class.java)
//                                        intent.putExtra("profileData",profileData)
//                                        intent.putExtra("from","form_upload")
//                                        startActivity(intent)

                                        val fragment = FormUploadFragment().apply {
                                            arguments = Bundle().apply {
                                                putParcelable("profileData", profileData)
                                            }
                                        }

                                        requireActivity().supportFragmentManager.beginTransaction()
                                            .replace(R.id.frameLayout, fragment)
                                            .addToBackStack(null)
                                            .commit()
                                    }
                                    getString(R.string.account_details) ->{
                                        if (userId != null){
                                            val fragment = PaymentDetailsFragment().apply {
                                                arguments = Bundle().apply {
                                                    putString("userId", userId)
                                                }
                                            }

                                            requireActivity().supportFragmentManager.beginTransaction()
                                                .replace(R.id.frameLayout, fragment)
                                                .addToBackStack(null)
                                                .commit()
                                        }
                                    }
                                    getString(R.string.change_password) ->{
                                            val fragment = ChangePasswordFragment().apply {
                                            }
                                            requireActivity().supportFragmentManager.beginTransaction()
                                                .replace(R.id.frameLayout, fragment)
                                                .addToBackStack(null)
                                                .commit()

                                    }
                                    "Logout"->{
                                        logoutPopup.show()
                                    }
                                    "Delete Account" ->{
                                        deleteAccountPopup.show()
                                    }
                                }

                            }
                            2->{
                                when(m.subTitle){
                                    getString(R.string.profile_details) -> {
//                                        val intent=Intent(requireContext(), CommonActivity::class.java)
//                                        intent.putExtra("profileData",profileData)
//                                        intent.putExtra("from","edit_profile")
//                                        startActivity(intent)

                                        val fragment = EditProfileDetailFragment().apply {
                                            arguments = Bundle().apply {
                                                putParcelable("profileData", profileData)
                                            }
                                        }

                                        requireActivity().supportFragmentManager.beginTransaction()
                                            .replace(R.id.frameLayout, fragment)
                                            .addToBackStack(null)
                                            .commit()


                                    }
                                    getString(R.string.business_financial_information)->{
//                                        val intent=Intent(requireContext(), CommonActivity::class.java)
//                                        intent.putExtra("profileData",profileData)
//                                        intent.putExtra("from","business_info")
//                                        startActivity(intent)

                                        val fragment = BusinessInfoFragment().apply {
                                            arguments = Bundle().apply {
                                                putParcelable("profileData", profileData)
                                            }
                                        }

                                        requireActivity().supportFragmentManager.beginTransaction()
                                            .replace(R.id.frameLayout, fragment)
                                            .addToBackStack(null)
                                            .commit()
                                    }
                                    getString(R.string.additional_information)->{
//                                        val intent=Intent(requireContext(), CommonActivity::class.java)
//                                        intent.putExtra("from","personal_preference")
//                                        startActivity(intent)


                                        val fragment = PersonalPreferencesFragment().apply {
                                            arguments = Bundle().apply {
                                                putParcelable("profileData", profileData)
                                            }
                                        }

                                        requireActivity().supportFragmentManager.beginTransaction()
                                            .replace(R.id.frameLayout, fragment)
                                            .addToBackStack(null)
                                            .commit()

                                    }
                                    getString(R.string.account_details) ->{
                                        if (userId != null){
                                            val fragment = PaymentDetailsFragment().apply {
                                                arguments = Bundle().apply {
                                                    putString("userId", userId)
                                                }
                                            }

                                            requireActivity().supportFragmentManager.beginTransaction()
                                                .replace(R.id.frameLayout, fragment)
                                                .addToBackStack(null)
                                                .commit()
                                        }
                                    }
                                    getString(R.string.change_password) ->{
                                            val fragment = ChangePasswordFragment().apply {

                                            }

                                            requireActivity().supportFragmentManager.beginTransaction()
                                                .replace(R.id.frameLayout, fragment)
                                                .addToBackStack(null)
                                                .commit()

                                    }
                                    getString(R.string.form_upload)->{
//                                        val intent=Intent(requireContext(), CommonActivity::class.java)
//                                        intent.putExtra("profileData",profileData)
//                                        intent.putExtra("from","form_upload")
//                                        startActivity(intent)


                                        val fragment = FormUploadFragment().apply {
                                            arguments = Bundle().apply {
                                                putParcelable("profileData", profileData)
                                            }
                                        }

                                        requireActivity().supportFragmentManager.beginTransaction()
                                            .replace(R.id.frameLayout, fragment)
                                            .addToBackStack(null)
                                            .commit()
                                    }
                                    getString(R.string.account_verification) -> {

                                        userVerification()
//                                        val intent =
//                                            Intent(requireContext(), CommonActivity::class.java)
//                                        intent.putExtra("from", "account_verify")
//                                        startActivity(intent)
                                    }
                                    "Logout"->{
                                        logoutPopup.show()
                                    }
                                    "Delete Account" ->{
                                        deleteAccountPopup.show()
                                    }
                                }
                            }
                        }

                    }
                }

            }
        editProfileAdapter.list = editMenuList
        binding.rvEdit.adapter = editProfileAdapter
    }

    /** for normal profile **/
    private fun showList(): ArrayList<EditProfileListModel> {
        val list = ArrayList<EditProfileListModel>()
        list.add(
            EditProfileListModel(
                "Account", getString(R.string.profile_details), R.drawable.iv_forword,0, false,true
            )
        )
        list.add(
            EditProfileListModel("Account",
                getString(R.string.family_education), R.drawable.iv_forword,0,false))
        list.add(
            EditProfileListModel("Account",
                getString(R.string.financial_information), R.drawable.iv_forword,0,false)
        )
        list.add(
            EditProfileListModel(
                "Account", "Investment Summary (Optional)", R.drawable.iv_forword,0,false
            )
        )

        list.add(
            EditProfileListModel(
                "Account", getString(R.string.additional_information), R.drawable.iv_forword,0,false
            )
        )
        list.add(
            EditProfileListModel(
                "Account", getString(R.string.account_details), R.drawable.iv_forword,0,false
            )
        )
        list.add(
            EditProfileListModel(
                "Account", getString(R.string.change_password), R.drawable.iv_forword,0,false
            )
        )
//        list.add(
//            EditProfileListModel(
//                "Account Management",
//                getString(R.string.member_agreement),
//                R.drawable.iv_forword,
//                true
//            )
//        )
        list.add(
            EditProfileListModel(
                "Account Management",
                getString(R.string.account_verification),
                R.drawable.iv_forword,
                0,
                false,
                true,
                1
            )
        )
        list.add(
            EditProfileListModel("Account Management", "Last Login", 0,0,false)
        )
        list.add(
            EditProfileListModel("Account Management", "Delete Account", R.drawable.ic_delete,0,false)
        )
        list.add(
            EditProfileListModel("Account Management", "Logout", R.drawable.ic_logout,0,false)
        )
        return list
    }

    /**for finance advisor**/
    private fun showListForFinancialAdvisor(): ArrayList<EditProfileListModel> {
        val list = ArrayList<EditProfileListModel>()
        list.add(
            EditProfileListModel(
                "Account", getString(R.string.profile_details), R.drawable.iv_forword,1, false,true
            )
        )
        list.add(
            EditProfileListModel("Account",
                getString(R.string.professional_information), R.drawable.iv_forword,1,false)
        )
        list.add(
            EditProfileListModel("Account",
                getString(R.string.personal_preferences), R.drawable.iv_forword,1,false)
        )
        list.add(
            EditProfileListModel(
                "Account", getString(R.string.form_upload), R.drawable.iv_forword,1,false
            )
        )
        list.add(
            EditProfileListModel(
                "Account", getString(R.string.account_details), R.drawable.iv_forword,1,false
            )
        )
        list.add(
            EditProfileListModel(
                "Account", getString(R.string.change_password), R.drawable.iv_forword,1,false
            )
        )
//        list.add(
//            EditProfileListModel(
//                "Account Management",
//                getString(R.string.member_agreement),
//                R.drawable.iv_forword,
//                true
//            )
//        )
        list.add(
            EditProfileListModel(
                "Account Management",
                getString(R.string.account_verification),
                R.drawable.iv_forword,1,
                false,
                true,
                1

            )
        )
        list.add(
            EditProfileListModel("Account Management", "Last Login", 0,1,false)
        )
        list.add(
            EditProfileListModel("Account Management", "Delete Account", R.drawable.ic_delete,1,false)
        )
        list.add(
            EditProfileListModel("Account Management", "Logout", R.drawable.ic_logout,1,false,)
        )
        return list
    }

    /**for startup & small business & investor/VC  & insurance **/
    private fun showListForStartUp(): ArrayList<EditProfileListModel> {
        val list = ArrayList<EditProfileListModel>()
        list.add(
            EditProfileListModel(
                "Account", getString(R.string.profile_details), R.drawable.iv_forword, 2,false,true
            )
        )
        list.add(
            EditProfileListModel("Account",
                getString(R.string.business_financial_information), R.drawable.iv_forword,2,false)
        )
        list.add(
            EditProfileListModel("Account",
                getString(R.string.additional_information), R.drawable.iv_forword,2,false)
        )
        list.add(
            EditProfileListModel(
                "Account", getString(R.string.form_upload), R.drawable.iv_forword,2,false
            )
        )
        list.add(
            EditProfileListModel(
                "Account", getString(R.string.account_details), R.drawable.iv_forword,2,false
            )
        )
        list.add(
            EditProfileListModel(
                "Account", getString(R.string.change_password), R.drawable.iv_forword,2,false
            )
        )
        list.add(
            EditProfileListModel(
                "Account Management",
                getString(R.string.account_verification),
                R.drawable.iv_forword,2, false,
                true,
                1

            )
        )
        list.add(
            EditProfileListModel("Account Management", "Last Login", 0,2,false)
        )
        list.add(
            EditProfileListModel("Account Management", "Delete Account", R.drawable.ic_delete,2,false)
        )
        list.add(
            EditProfileListModel("Account Management", "Logout", R.drawable.ic_logout,2,false)
        )
        return list
    }

    override fun onViewClick(view: View?) {
        when(view?.id){
            R.id.tvLogout ->{
                viewModel.logout(Constants.LOG_OUT)
            }
            R.id.tvNo ->{
                logoutPopup.dismiss()
            }
            R.id.tvDelete ->{

            }
            R.id.tvNoDelete ->{
                deleteAccountPopup.dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getProfile(Constants.GET_USER_PROFILE)

    }


    private fun userVerification() {
        // Show loading (optional)
        showLoading()
        Log.d("Didit", "Verification process started")

        // Grab vendor/user ID
        val vendorId = getLoggedInUserIdOrFallback()
        Log.d("Didit", "Using vendor ID: $vendorId")

        // Launch coroutine
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d("Didit", "Calling createVerificationSession API")

                val response: Map<String, Any> = withContext(Dispatchers.IO) {
                    DiditService.createVerificationSession(vendorId)
                }

                Log.d("Didit", "API response: $response")

                val url = (response["url"] as? String).orEmpty()
                Log.d("Didit", "Extracted URL: $url")

                if (url.isBlank()) {
                    Log.d("Didit", "Verification URL missing!")
                    showToast("Verification URL missing!")
                    return@launch
                }


                // Log successful retrieval
                Log.d("Didit", "Opening verification URL: $url")

                if (url != null){
                    val bundle = Bundle().apply {
                        putString("url", url)
                    }

                    val intent =
                        Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("from", "account_verify")
                    intent.putExtra("url",url)
                    startActivity(intent)
                }
                // openVerificationPage(url)

            } catch (t: Throwable) {
                Log.e("Didit", "Error during verification session creation: ${t.message}", t)
                showToast("Verification failed: ${t.localizedMessage ?: "Unknown error"}")
            } finally {
                hideLoading()
                Log.d("Didit", "Verification process finished")
            }
        }
    }


    private fun getLoggedInUserIdOrFallback(): String {
        // Replace with your real user session / SharedPreferences / DataStore fetch.
        // The Swift code used UserDefaults.standard[.loggedUserDetails]?._id ?? "123"
        return sharedPrefManager.getUserId()?: "123"
    }
}