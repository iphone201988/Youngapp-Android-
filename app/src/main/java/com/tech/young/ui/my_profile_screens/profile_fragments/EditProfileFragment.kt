package com.tech.young.ui.my_profile_screens.profile_fragments

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
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
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.my_profile_screens.YourProfileVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileFragment : BaseFragment<FragmentEditProfileBinding>(),BaseCustomDialog.Listener {
    private val viewModel: YourProfileVM by viewModels()

    private var lastLogin : String ? = null
    // adapter
    private lateinit var editProfileAdapter: SimpleRecyclerViewAdapter<EditProfileListModel, EditProfileItemViewBinding>
    private var profileData: GetProfileApiResponseData?=null
    // menu list
    private var editMenuList=ArrayList<EditProfileListModel>()

    private lateinit var logoutPopup : BaseCustomDialog<ItemLayoutLogoutPopupBinding>
    private lateinit var deleteAccountPopup : BaseCustomDialog<ItemLayoutDeleteAccountPopupBinding>
    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()
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
    //    viewModel.getProfile(Constants.GET_USER_PROFILE)
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
                R.id.tabEcosystem->{
                    // handle click
                }
                R.id.tabExchange->{
                    // handle click
                }
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
                                    profileData = myDataModel.data
                                    lastLogin = myDataModel.data?.user?.lastLogin
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
                date.text = lastLogin
                when (v.id) {
                    R.id.clMain -> {
                        when(m.listType){
                            0->{
                                when (m.subTitle) {
                                    // edit profile
                                    getString(R.string.profile_details) -> {
                                        val intent=Intent(requireContext(), CommonActivity::class.java)
                                        intent.putExtra("profileData",profileData)
                                        intent.putExtra("from","edit_profile")
                                        startActivity(intent)
                                    }
                                    // other profile details
                                    getString(R.string.family_education) -> {
                                        val intent=Intent(requireContext(), CommonActivity::class.java)
                                        intent.putExtra("profileData",profileData)
                                        intent.putExtra("from","normal_family")
                                        startActivity(intent)
                                    }

                                    getString(R.string.financial_information) -> {
                                        val intent=Intent(requireContext(), CommonActivity::class.java)
                                        intent.putExtra("profileData",profileData)
                                        intent.putExtra("from","normal_finance_detail")
                                        startActivity(intent)
                                    }

                                    "Investment Summary (Optional)" -> {
                                        val intent=Intent(requireContext(), CommonActivity::class.java)
                                        intent.putExtra("profileData",profileData)
                                        intent.putExtra("from","normal_investment")
                                        startActivity(intent)
                                    }

                                    getString(R.string.additional_information) -> {
                                        val intent=Intent(requireContext(), CommonActivity::class.java)
                                        intent.putExtra("profileData",profileData)
                                        intent.putExtra("from","personal_preference")
                                        startActivity(intent)
                                    }

                                    getString(R.string.account_verification) -> {
                                        val intent =
                                            Intent(requireContext(), CommonActivity::class.java)
                                        intent.putExtra("from", "account_verify")
                                        startActivity(intent)
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
                                        val intent =
                                            Intent(requireContext(), CommonActivity::class.java)
                                        intent.putExtra("profileData",profileData)
                                        intent.putExtra("from", "edit_profile")
                                        startActivity(intent)
                                    }
                                    getString(R.string.professional_information)->{
                                        val intent =
                                            Intent(requireContext(), CommonActivity::class.java)
                                        intent.putExtra("profileData",profileData)
                                        intent.putExtra("from", "professional_info")
                                        startActivity(intent)

                                    }

                                    getString(R.string.personal_preferences)->{
                                        val intent =
                                            Intent(requireContext(), CommonActivity::class.java)
                                        intent.putExtra("profileData",profileData)
                                        intent.putExtra("from", "personal_preference")
                                        startActivity(intent)

                                    }

                                    getString(R.string.account_verification) -> {
                                        val intent =
                                            Intent(requireContext(), CommonActivity::class.java)
                                        intent.putExtra("from", "account_verify")
                                        startActivity(intent)
                                    }
                                    getString(R.string.form_upload)->{
                                        val intent=Intent(requireContext(), CommonActivity::class.java)
                                        intent.putExtra("profileData",profileData)
                                        intent.putExtra("from","form_upload")
                                        startActivity(intent)
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
                                        val intent=Intent(requireContext(), CommonActivity::class.java)
                                        intent.putExtra("profileData",profileData)
                                        intent.putExtra("from","edit_profile")
                                        startActivity(intent)
                                    }
                                    getString(R.string.business_financial_information)->{
                                        val intent=Intent(requireContext(), CommonActivity::class.java)
                                        intent.putExtra("profileData",profileData)
                                        intent.putExtra("from","business_info")
                                        startActivity(intent)

                                    }
                                    getString(R.string.additional_information)->{
                                        val intent=Intent(requireContext(), CommonActivity::class.java)
                                        intent.putExtra("from","personal_preference")
                                        startActivity(intent)
                                    }
                                    getString(R.string.form_upload)->{
                                        val intent=Intent(requireContext(), CommonActivity::class.java)
                                        intent.putExtra("profileData",profileData)
                                        intent.putExtra("from","form_upload")
                                        startActivity(intent)
                                    }
                                    getString(R.string.account_verification) -> {
                                        val intent =
                                            Intent(requireContext(), CommonActivity::class.java)
                                        intent.putExtra("from", "account_verify")
                                        startActivity(intent)
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
                "Account", getString(R.string.profile_details), R.drawable.iv_forword,0, true
            )
        )
        list.add(
            EditProfileListModel("Account",
                getString(R.string.family_education), R.drawable.iv_forword,0))
        list.add(
            EditProfileListModel("Account",
                getString(R.string.financial_information), R.drawable.iv_forword,0)
        )
        list.add(
            EditProfileListModel(
                "Account", "Investment Summary (Optional)", R.drawable.iv_forword,0
            )
        )

        list.add(
            EditProfileListModel(
                "Account", getString(R.string.additional_information), R.drawable.iv_forword,0
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
                true
            )
        )
        list.add(
            EditProfileListModel("Account Management", "Last Login", 0,0)
        )
        list.add(
            EditProfileListModel("Account Management", "Delete Account", R.drawable.ic_delete,0)
        )
        list.add(
            EditProfileListModel("Account Management", "Logout", R.drawable.ic_logout,0)
        )
        return list
    }

    /**for finance advisor**/
    private fun showListForFinancialAdvisor(): ArrayList<EditProfileListModel> {
        val list = ArrayList<EditProfileListModel>()
        list.add(
            EditProfileListModel(
                "Account", getString(R.string.profile_details), R.drawable.iv_forword,1, true
            )
        )
        list.add(
            EditProfileListModel("Account",
                getString(R.string.professional_information), R.drawable.iv_forword,1)
        )
        list.add(
            EditProfileListModel("Account",
                getString(R.string.personal_preferences), R.drawable.iv_forword,1)
        )
        list.add(
            EditProfileListModel(
                "Account", getString(R.string.form_upload), R.drawable.iv_forword,1
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
                true
            )
        )
        list.add(
            EditProfileListModel("Account Management", "Last Login", 0,1)
        )
        list.add(
            EditProfileListModel("Account Management", "Delete Account", R.drawable.ic_delete,1)
        )
        list.add(
            EditProfileListModel("Account Management", "Logout", R.drawable.ic_logout,1)
        )
        return list
    }

    /**for startup & small business & investor/VC  & insurance **/
    private fun showListForStartUp(): ArrayList<EditProfileListModel> {
        val list = ArrayList<EditProfileListModel>()
        list.add(
            EditProfileListModel(
                "Account", getString(R.string.profile_details), R.drawable.iv_forword, 2,true
            )
        )
        list.add(
            EditProfileListModel("Account",
                getString(R.string.business_financial_information), R.drawable.iv_forword,2)
        )
        list.add(
            EditProfileListModel("Account",
                getString(R.string.additional_information), R.drawable.iv_forword,2)
        )
        list.add(
            EditProfileListModel(
                "Account", getString(R.string.form_upload), R.drawable.iv_forword,2
            )
        )
        list.add(
            EditProfileListModel(
                "Account Management",
                getString(R.string.account_verification),
                R.drawable.iv_forword,2,
                true
            )
        )
        list.add(
            EditProfileListModel("Account Management", "Last Login", 0,2)
        )
        list.add(
            EditProfileListModel("Account Management", "Delete Account", R.drawable.ic_delete,2)
        )
        list.add(
            EditProfileListModel("Account Management", "Logout", R.drawable.ic_logout,2)
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
}