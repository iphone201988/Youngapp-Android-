package com.tech.young.ui.home

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.SocketManager
import com.tech.young.base.BaseActivity
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BaseCustomDialog
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.event.SingleRequestEvent
import com.tech.young.data.SubViewClickBean
import com.tech.young.data.api.Constants
import com.tech.young.data.api.SimpleApiResponse
import com.tech.young.data.model.GetProfileApiResponse
import com.tech.young.data.model.SideMenuBar
import com.tech.young.databinding.ActivityHomeBinding
import com.tech.young.databinding.ItemLayoutLogoutPopupBinding
import com.tech.young.databinding.ItemLayoutSideNavBinding
import com.tech.young.ui.MySplashActivity
import com.tech.young.ui.advertise_screen.AdvertiseFragment
import com.tech.young.ui.contact_screens.ContactUsFragment
import com.tech.young.ui.ecosystem.EcosystemFragment
import com.tech.young.ui.ecosystem.EcosystemFragment.Companion.selectedCategoryForEcosystem
import com.tech.young.ui.exchange.ExchangeFragment
import com.tech.young.ui.exchange.screens.ShareExchangeFragment.Companion.selectedCategoryForExchange
import com.tech.young.ui.inbox.InboxFragment
import com.tech.young.ui.my_profile_screens.YourProfileFragment
import com.tech.young.ui.policies_about.AboutFragment
import com.tech.young.ui.policies_about.PoliciesFragment
import com.tech.young.ui.share_screen.CommonShareFragment
import com.tech.young.ui.stream_screen.CommonStreamFragment
import com.tech.young.ui.user_profile.UserProfileFragment
import com.tech.young.ui.vault_screen.CommonVaultFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() , BaseCustomDialog.Listener{
    private val viewModel: HomeActivityVM by viewModels()
    // private lateinit var navController: NavController
    private lateinit var logoutPopup : BaseCustomDialog<ItemLayoutLogoutPopupBinding>

    private var name: String = "Your name"

    companion object{
        var userName  : String ? = null
    }


    override fun getLayoutResource(): Int {
        return R.layout.activity_home
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        initView()
        initPopup()
        viewModel.getProfile(Constants.GET_USER_PROFILE)
        initOnClick()
        initObserver()
    }
    private fun initPopup() {
        logoutPopup = BaseCustomDialog(this , R.layout.item_layout_logout_popup,this )
    }
    private fun initObserver() {
        viewModel.observeCommon.observe(this, Observer {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    hideLoading()
                    when (it.message) {
                        "getProfile" -> {
                            val myDataModel: GetProfileApiResponse? =
                                BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                if (myDataModel.data != null) {
                                    if (myDataModel.data?.user?.lastName.toString()!=null){
                                        name = myDataModel.data?.user?.firstName.toString() +" "+ myDataModel.data?.user?.lastName.toString()
                                    }
                                    else{
                                        name = myDataModel.data?.user?.firstName.toString()
                                    }
                                    Log.i("dsadas", "initObserver: $name")
                                    initAdapter()
                                }
                            }
                        }
                        "logout" ->{
                            val myDataModel : SimpleApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                sharedPrefManager.clear()
                                logoutPopup.dismiss()
                                SocketManager.closeConnection()
                                showToast(myDataModel.message.toString())
                                val intent = Intent(this , MySplashActivity::class.java)
                                startActivity(intent)
                            }
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
    }

    private fun initView() {
        BindingUtils.statusBarStyleBlack(this)
        BindingUtils.styleSystemBars(this, getColor(R.color.white))
//        navController = (supportFragmentManager
//            .findFragmentById(R.id.navHostFragment) as NavHostFragment).navController
        displayFragment(HomeFragment())
        updateHomeUI()
        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.frameLayout)
            if (currentFragment != null && currentFragment.isAdded) {
                when (currentFragment) {
                    is HomeFragment -> {
                        updateHomeUI()
                    }

                    is YourProfileFragment -> {
                        updateOtherUI(getString(R.string.your_profile))
                    }

                    is InboxFragment -> {
                        updateOtherUI(getString(R.string.inbox))
                    }

                    is ContactUsFragment -> {
                        updateOtherUI(getString(R.string.contact_us))
                    }

                    is AdvertiseFragment -> {
                        updateOtherUI(getString(R.string.advertise))
                    }

                    is PoliciesFragment -> {
                        updateOtherUI(getString(R.string.policies_and_agreement))
                    }

                    is AboutFragment -> {
                        updateOtherUI(getString(R.string.about))
                    }

                    is ExchangeFragment -> {
                        updateOtherUI(getString(R.string.exchange))
                    }

                    is EcosystemFragment -> {
                        updateOtherUI(getString(R.string.ecosystem))
                    }

                    is ViewMoreFragment ->{
                        updateOtherUI("News")
                    }

                    is CommonShareFragment ->{
                        updateOtherUI("Share")
                    }

                    is  CommonStreamFragment ->{
                        updateOtherUI("Stream")
                    }

                    is CommonVaultFragment ->{
                        updateOtherUI("Vault")
                    }
                    is UserProfileFragment ->{
                        updateOtherUI(userName.toString())
                    }
                }

            }
        }

    }


    private fun initOnClick() {
        viewModel.onClick.observe(this, Observer {
            when (it?.id) {
                R.id.actionToggleBtn -> {
                    binding.drawerLayout.openDrawer(GravityCompat.START)
                }

                R.id.ivBack -> {
                    supportFragmentManager.popBackStack()
                }

                R.id.tvContactUs -> {
                    displayFragment(ContactUsFragment())
                    updateOtherUI(getString(R.string.contact_us))
                }

                R.id.tvAdvertise -> {
                    displayFragment(AdvertiseFragment())
                    updateOtherUI(getString(R.string.advertise))
                }

                R.id.tvAbout -> {
                    displayFragment(AboutFragment())
                    updateOtherUI(getString(R.string.about))
                }

                R.id.tvPoliciesAndAgreement -> {
                    displayFragment(PoliciesFragment())
                    updateOtherUI(getString(R.string.policies_and_agreement))
                }
                R.id.tvLogout ->{
                    logoutPopup.show()
                }

            }
        })
    }

    /**Display Fragment**/
    private fun displayFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, fragment)
            .addToBackStack(null).commit()
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    private lateinit var adapter: SimpleRecyclerViewAdapter<SideMenuBar, ItemLayoutSideNavBinding>
    private fun initAdapter() {
        adapter = SimpleRecyclerViewAdapter(R.layout.item_layout_side_nav, BR.bean) { _v, m, pos_ ->
            when (_v.id) {
                R.id.consMain -> {
                    adapter.list.forEach {
                        it.isSelected = false
                    }
                    m.isSelected = true
                    Constants.chooseAccountType = m.name
                    adapter.notifyDataSetChanged()
                    handleNavClick(m.name, m.heading)
//                    m.id?.let {it->
//                        if (navController.currentDestination?.id != it) {
//                            navController.navigate(it)
//                        }
//                        binding.drawerLayout.closeDrawer(GravityCompat.START)
//                    }

                }
            }

        }
        binding.rvNav.adapter = adapter
        adapter.list = listChooseAccountType()
    }

    private fun listChooseAccountType(): ArrayList<SideMenuBar> {
        val list = ArrayList<SideMenuBar>()
        list.add(SideMenuBar(true, "Home"))
        list.add(SideMenuBar(false, name.toString(), null))
        list.add(SideMenuBar(false, "Profile"))
        list.add(SideMenuBar(false, "Inbox"))
        list.add(SideMenuBar(false, "Dashboard"))
        list.add(SideMenuBar(false, "Analytics"))
        list.add(SideMenuBar(false, "Member", "Exchange", true))
        list.add(SideMenuBar(false, "Financial Advisors", "Exchange"))
        list.add(SideMenuBar(false, "Startups", "Exchange"))
        list.add(SideMenuBar(false, "Small Business", "Exchange"))
        list.add(SideMenuBar(false, "Investor/ VC", "Exchange"))
        list.add(SideMenuBar(false, "Member", "Ecosystem", true))
        list.add(SideMenuBar(false, "Financial Advisors", "Ecosystem"))
        list.add(SideMenuBar(false, "Startups", "Ecosystem"))
        list.add(SideMenuBar(false, "Small Business", "Ecosystem"))
        list.add(SideMenuBar(false, "Investor/ VC", "Ecosystem"))
        return list

    }

    /** handle nav clicks **/
    private fun handleNavClick(title: String?, heading: String?) {
        if (heading != null) {
            if (heading == "Exchange") {
                if (title != null) {
                    when (title) {
                        "Member" -> {
                            selectedCategoryForExchange = "Members"
                            displayFragment(ExchangeFragment())
                            updateOtherUI(getString(R.string.exchange))
                        }

                        "Financial Advisors" -> {
                            selectedCategoryForExchange = "Advisors"
                            displayFragment(ExchangeFragment())
                            updateOtherUI(getString(R.string.exchange))
                        }

                        "Startups" -> {
                            selectedCategoryForExchange = "Startups"
                            displayFragment(ExchangeFragment())
                            updateOtherUI(getString(R.string.exchange))
                        }

                        "Small Business" -> {
                            selectedCategoryForExchange = "Small Businesses"
                            displayFragment(ExchangeFragment())
                            updateOtherUI(getString(R.string.exchange))
                        }

                        "Investor/ VC" -> {
                            selectedCategoryForExchange = "Investor"
                            //selectedCategoryForExchange="VCs"
                            displayFragment(ExchangeFragment())
                            updateOtherUI(getString(R.string.exchange))
                        }
                    }
                }
            } else if (heading == "Ecosystem") {
                if (title != null) {
                    when (title) {
                        "Member" -> {
                            selectedCategoryForEcosystem = "Members"
                            displayFragment(EcosystemFragment())
                            updateOtherUI(getString(R.string.ecosystem))
                        }

                        "Financial Advisors" -> {
                            selectedCategoryForEcosystem = "Advisors"
                            displayFragment(EcosystemFragment())
                            updateOtherUI(getString(R.string.ecosystem))
                        }

                        "Startups" -> {
                            selectedCategoryForEcosystem = "Startups"
                            displayFragment(EcosystemFragment())
                            updateOtherUI(getString(R.string.ecosystem))
                        }

                        "Small Business" -> {
                            selectedCategoryForEcosystem = "Small Businesses"
                            displayFragment(EcosystemFragment())
                            updateOtherUI(getString(R.string.ecosystem))
                        }

                        "Investor/ VC" -> {
                            selectedCategoryForEcosystem = "Investor"
                            //selectedCategoryForEcosystem ="VCs"
                            displayFragment(EcosystemFragment())
                            updateOtherUI(getString(R.string.ecosystem))
                        }
                    }
                }
            }
        } else {
            if (title != null) {
                when (title) {
                    "Home" -> {
                        displayFragment(HomeFragment())
                        updateHomeUI()
                    }

                    "Profile" -> {
                        displayFragment(YourProfileFragment())
                        updateOtherUI(getString(R.string.your_profile))
                    }

                    "Inbox" -> {
                        displayFragment(InboxFragment())
                        updateOtherUI(getString(R.string.inbox))
                    }
                }
            }
        }
    }


    private fun updateHomeUI() {
        binding.ivBack.visibility = View.GONE
        binding.ivAppLogoTop.visibility = View.VISIBLE
        binding.tvTitle.visibility = View.GONE
    }

    private fun updateOtherUI(title: String) {
        binding.ivBack.visibility = View.VISIBLE
        binding.ivAppLogoTop.visibility = View.GONE
        binding.tvTitle.visibility = View.VISIBLE
        binding.tvTitle.text = title
    }

    override fun onViewClick(view: View?) {
        when(view?.id){
            R.id.tvLogout ->{
                viewModel.logout(Constants.LOG_OUT)
            }
            R.id.tvNo ->{
                logoutPopup.dismiss()
            }
        }
    }

//    override fun onResume() {
//        navClick.observe(this){
//            Log.i("fdfdsfs", "onResume:${navClick} ")
//            when(it?.status){
//                Status.LOADING ->{
//
//                }
//                Status.SUCCESS ->{
//                    Log.i("fdfdsfs", "success:${navClick} ")
//                    binding.drawerLayout.openDrawer(GravityCompat.START)
//
//            }Status.ERROR ->{
//
//            }
//                else ->{
//
//                }
//            }
//        }
//        super.onResume()
//    }
}