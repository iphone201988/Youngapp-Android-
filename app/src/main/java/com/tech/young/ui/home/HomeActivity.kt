package com.tech.young.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.tech.young.base.utils.Resource
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.event.SingleRequestEvent
import com.tech.young.data.SubViewClickBean
import com.tech.young.data.UserData
import com.tech.young.data.api.Constants
import com.tech.young.data.api.SimpleApiResponse
import com.tech.young.data.model.FcmPayload
import com.tech.young.data.model.GetProfileApiResponse
import com.tech.young.data.model.SideMenuBar
import com.tech.young.databinding.ActivityHomeBinding
import com.tech.young.databinding.ItemLayoutLeavePagePopupBinding
import com.tech.young.databinding.ItemLayoutLogoutPopupBinding
import com.tech.young.databinding.ItemLayoutSideNavBinding
import com.tech.young.ui.MySplashActivity
import com.tech.young.ui.advertise_screen.AdvertiseFragment
import com.tech.young.ui.ai_chat.AiChatFragment
import com.tech.young.ui.change_password.ChangePasswordFragment
import com.tech.young.ui.contact_screens.ContactUsFragment
import com.tech.young.ui.ecosystem.EcosystemFragment
import com.tech.young.ui.ecosystem.EcosystemFragment.Companion.selectedCategoryForEcosystem
import com.tech.young.ui.engagement.EngagementFragment
import com.tech.young.ui.exchange.ExchangeFragment
import com.tech.young.ui.exchange.exchange_share_detail.ExchangeShareDetailFragment
import com.tech.young.ui.exchange.screens.ShareExchangeFragment.Companion.selectedCategoryForExchange
import com.tech.young.ui.exchange.stream_detail_fragment.StreamDetailFragment
import com.tech.young.ui.financial_business.FinancialBusinessFragment
import com.tech.young.ui.inbox.InboxFragment
import com.tech.young.ui.inbox.view_message.ViewMessageFragment
import com.tech.young.ui.investment_plan.InvestmentPlanFragment
import com.tech.young.ui.investment_tracker.InvestmentTrackerFragment
import com.tech.young.ui.media.MediaFragment
import com.tech.young.ui.my_profile_screens.YourProfileFragment
import com.tech.young.ui.my_profile_screens.common_ui.BusinessInfoFragment
import com.tech.young.ui.my_profile_screens.common_ui.EditProfileDetailFragment
import com.tech.young.ui.my_profile_screens.common_ui.FormUploadFragment
import com.tech.young.ui.my_profile_screens.forFinance.PersonalPreferencesFragment
import com.tech.young.ui.my_profile_screens.forFinance.ProfessionalInformationFragment
import com.tech.young.ui.my_profile_screens.forNormal.FamilyDetailsFragment
import com.tech.young.ui.my_profile_screens.forNormal.FinanceInfoFragment
import com.tech.young.ui.my_profile_screens.forNormal.InvestmentInfoFragment
import com.tech.young.ui.my_profile_screens.forNormal.PerformanceFragment
import com.tech.young.ui.my_profile_screens.profile_fragments.CalendarFragment
import com.tech.young.ui.my_share.MyShareFragment
import com.tech.young.ui.notification_fragment.NotificationFragment
import com.tech.young.ui.payment.PaymentDetailsFragment
import com.tech.young.ui.payment.payment_history.PaymentHistoryFragment
import com.tech.young.ui.policies_about.AboutFragment
import com.tech.young.ui.policies_about.PoliciesFragment
import com.tech.young.ui.share_screen.CommonShareFragment
import com.tech.young.ui.share_screen.share_confirmation.ShareConfirmationFragment
import com.tech.young.ui.stream_screen.CommonStreamFragment
import com.tech.young.ui.stream_screen.stream_confirmation.StreamConfirmationFragment
import com.tech.young.ui.user_profile.UserProfileFragment
import com.tech.young.ui.vault_screen.CommonVaultFragment
import com.tech.young.ui.vault_screen.people_screen.PeopleFragment
import com.tech.young.ui.vault_screen.vault_room.VaultRoomFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() , BaseCustomDialog.Listener{
    private val viewModel: HomeActivityVM by viewModels()
    // private lateinit var navController: NavController
    private lateinit var logoutPopup : BaseCustomDialog<ItemLayoutLogoutPopupBinding>
    private lateinit var leavePagePopup : BaseCustomDialog<ItemLayoutLeavePagePopupBinding>
    private var currentFragment : Fragment ?= null
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


        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, 0, bars.right, bars.bottom)
            insets
        }

     
        initView()
        initPopup()
        viewModel.getProfile(Constants.GET_USER_PROFILE)
        initOnClick()
        initObserver()


        val bundle = intent.extras
        Log.i("dfddfd", "onCreateView: $bundle")

        val payload: FcmPayload? = bundle?.getParcelable("notificationData")
        Log.i("fdsfdsfsd", "onCreateView: $payload")

        if (payload != null) {
            when(payload.type){
                "share" ->{
                    Log.i("dsadasdas", "onCreateView: shareload")
                    val fragment = ExchangeShareDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString("userId", payload.postId)
                        }
                    }

                     supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
                }


                "message" -> {
                    val chatUser = UserData(
                        _id = payload?.userId, //  should map from FcmPayload.userId, not payload?._id
                        profileImage = payload?.profileImage,
                        role = payload?.role,
                        username = payload?.username,
                        firstName =  payload?.firstName,
                        lastName = payload?.lastName
                    )

                    val bundle = Bundle().apply {
                        putString("from", "view_message")
                        putString("threadId", payload?.chatId)
                        putParcelable("userData", chatUser)
                    }

                    val viewMessageFragment = ViewMessageFragment().apply {
                        arguments = bundle
                    }

                   supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, viewMessageFragment)
                        .addToBackStack(null)
                        .commit()
                }

                "live_stream" ->{

                }
                "customer" ->{

                }
                "follower" ->{
                    updateOtherUI(payload.username.toString())
                    val userProfileFragment = UserProfileFragment().apply {
                        arguments  = Bundle().apply {
                            putString("from", "user_profile")
                            putString("userId", payload.userId) // assuming m._id is a String
                        }
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, userProfileFragment)
                        .addToBackStack(null)
                        .commit()


                }
                "share_comment" ->{
                    val fragment = ExchangeShareDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString("userId", payload.shareId)
                        }
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
                }
                "stream_comment" ->{
                    val fragment = StreamDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString("streamId", payload.streamId)
                        }
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
                }
                "vault_comment" ->{

                    Log.i("fdsfdsf", "onCreateView: ${payload.vaultId}")
                    val fragment = VaultRoomFragment().apply {
                        arguments = Bundle().apply {
                            putString("vaultId", payload.vaultId)
                        }
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
                }
                "share_like" ->{
                    val fragment = ExchangeShareDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString("userId", payload.shareId)
                        }
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
                }
                "reshare" ->{
                    val fragment = ExchangeShareDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString("userId", payload.shareId)
                        }
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
                }

                "stream_like" ->{
                    val fragment = StreamDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString("streamId", payload.streamId)
                        }
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
                }

                "ratings_share" ->{
                    val fragment = ExchangeShareDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString("userId", payload.shareId)
                        }
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
                }
                "ratings_stream" ->{
                    val fragment = StreamDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString("streamId", payload.streamId)
                        }
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
                }
                "ratings_vault" ->{
                    Log.i("fdsfdsf", "onCreateView: ${payload.vaultId}")
                    val fragment = VaultRoomFragment().apply {
                        arguments = Bundle().apply {
                            putString("vaultId", payload.vaultId)
                        }
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
                }
                "ratings_user" ->{
                    updateOtherUI(payload.username.toString())
                    val userProfileFragment = UserProfileFragment().apply {
                        arguments  = Bundle().apply {
                            putString("from", "user_profile")
                            putString("userId", payload.userId) // assuming m._id is a String
                        }
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, userProfileFragment)
                        .addToBackStack(null)
                        .commit()

                }

            }
            Log.i("HomeActivity", "Payload received: $payload")
        } else {
            Log.w("HomeActivity", "No payload found in intent")
        }


    }
    private fun initPopup() {
        logoutPopup = BaseCustomDialog(this , R.layout.item_layout_logout_popup,this )
        leavePagePopup = BaseCustomDialog(this , R.layout.item_layout_leave_page_popup,this)
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
                                    val isUserSubscribed = myDataModel.data?.user?.isSubscribed != null
                                    sharedPrefManager.setSubscribed(isUserSubscribed)
                                    if(myDataModel.data?.user?.lastName.toString()!=null){
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
            currentFragment = supportFragmentManager.findFragmentById(R.id.frameLayout)
            if (currentFragment != null && currentFragment!!.isAdded) {
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
                    is EditProfileDetailFragment ->{
                        updateOtherUI("Profile Details")
                    }
                    is ProfessionalInformationFragment ->{
                        updateOtherUI("Professional Information")
                    }
                    is PersonalPreferencesFragment -> {
                        val role = sharedPrefManager.getLoginData()?.role
                        when (role) {
                            "financial_advisor", "financial_firm" ,"life_insurance","broker","investment_managers" -> updateOtherUI("Personal Preferences")
                            else -> updateOtherUI("Additional Information")
                        }
                    }
                    is FormUploadFragment ->{
                        updateOtherUI("Form Upload")
                    }

                    is FamilyDetailsFragment ->{
                        updateOtherUI("Family & Education")
                    }

                    is FinanceInfoFragment ->{
                        updateOtherUI("Financial Information")
                    }
                    is  InvestmentInfoFragment ->{
                        updateOtherUI("Investment Summary")
                    }

                    is BusinessInfoFragment ->{
                        updateOtherUI("Business & Financial Information")
                    }

                    is ExchangeShareDetailFragment ->{
                        updateOtherUI("Share")
                    }
                    is StreamDetailFragment -> {
                        updateOtherUI("Stream")
                    }
                    is VaultRoomFragment ->{
                        updateOtherUI("Vault Room")
                    }
                    is VaultRoomFragment ->{
                        updateOtherUI("Vault Room")
                    }
                    is PeopleFragment ->{
                        updateOtherUI("Members")
                    }
                    is PaymentDetailsFragment ->{
                        updateOtherUI("Account Details")
                    }
                    is  MyShareFragment ->{
//                        updateOtherUI("My Shares")
                    }
                    is ViewMessageFragment->{
                        updateOtherUI("View Message")
                    }
                    is ChangePasswordFragment ->{
                        updateOtherUI("Change Password")
                    }
                    is NotificationFragment ->{
                        updateOtherUI("Notification")
                    }
                    is PaymentHistoryFragment ->{
                        updateOtherUI("Payment History")
                    }
                    is ShareConfirmationFragment ->{
                        updateOtherUI("Share Confirmation")
                    }
                    is StreamConfirmationFragment ->{
                        updateOtherUI("Stream Confirmation")
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
                    currentFragment = supportFragmentManager.findFragmentById(R.id.frameLayout)

                    // 1️⃣ Check nested YourProfileFragment (handles CalendarFragment inside)
                    if (currentFragment is YourProfileFragment && (currentFragment as YourProfileFragment).handleBackPress()) return@Observer

                    if (currentFragment is PeopleFragment) {
                        CommonVaultFragment.reload = true
                        PeopleFragment.sendData.value = Resource.success("dasdas", true)
                    } else {
                        CommonVaultFragment.reload = false
                    }



                    val fragment = currentFragment
                    if (fragment is ContactUsFragment) {
                        if (fragment.hasUserEdited()) {
                            leavePagePopup.show()
                        } else {
                            supportFragmentManager.popBackStack()
                        }
                    } else {
                        supportFragmentManager.popBackStack()
                    }


                }



                R.id.tvContactUs -> {
                    displayFragment(ContactUsFragment())
                    updateOtherUI(getString(R.string.contact_us))
                }

                R.id.tvMedia ->{
                    displayFragment(MediaFragment())
                    updateOtherUI(getString(R.string.media))
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
                R.id.ivAppLogo ->{
                    displayFragment(HomeFragment())
                    updateHomeUI()
                }
                R.id.tvLogout ->{
                    logoutPopup.show()
                }

                R.id.ivAppLogoTop ->{
                    displayFragment(YourProfileFragment())
                    updateOtherUI(getString(R.string.your_profile))
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

        adapter = SimpleRecyclerViewAdapter(R.layout.item_layout_side_nav, BR.bean) { _v, m, pos ->

            when (_v.id) {

                R.id.consMain -> {

                    if (m.isHeader) {

                        val index = adapter.list.indexOf(m)

                        if (m.isExpanded) {
                            // COLLAPSE
                            val toRemove = adapter.list.filter { it.heading == m.name }
                            adapter.list.removeAll(toRemove)
                            m.isExpanded = false

                        } else {
                            // EXPAND
                            val children = getChildren(m.name ?: "")
                            adapter.list.addAll(index + 1, children)
                            m.isExpanded = true
                        }

                        adapter.notifyDataSetChanged()

                    } else {

                        adapter.list.forEach { it.isSelected = false }
                        m.isSelected = true

                        Constants.chooseAccountType = m.name!!
                        adapter.notifyDataSetChanged()

                        handleNavClick(m.name, m.heading)
                    }
                }
            }
        }

        binding.rvNav.adapter = adapter
        adapter.list = listChooseAccountType()
    }

    private fun listChooseAccountType(): ArrayList<SideMenuBar> {
        val list = ArrayList<SideMenuBar>()

        list.add(SideMenuBar(true, "Home" , null, false))

        // Name (Header)
        list.add(SideMenuBar(false, name.toString(), null, true))

        // Exchange
        list.add(SideMenuBar(false, "Exchange", null, true))

        // Ecosystem
        list.add(SideMenuBar(false, "Ecosystem", null, true))

        // Tools
        list.add(SideMenuBar(false, "Tools", null, true))

        return list
    }



    private fun getChildren(header: String): List<SideMenuBar> {
        return when (header) {

            name -> listOf(
                SideMenuBar(false, "Profile", name),
                SideMenuBar(false, "Inbox", name),
                SideMenuBar(false, "Notification", name)
            )

            "Exchange" -> listOf(
                SideMenuBar(false, "Member", "Exchange"),
                SideMenuBar(false, "Financial Advisors", "Exchange"),
                SideMenuBar(false, "Startups", "Exchange"),
                SideMenuBar(false, "Small Business", "Exchange"),
                SideMenuBar(false, "Investor/ VC", "Exchange"),
                SideMenuBar(false, "Life Insurance", "Exchange"),
                SideMenuBar(false, "Brokers/Dealer", "Exchange"),
                SideMenuBar(false, "Investment Mangers", "Exchange")
            )

            "Ecosystem" -> listOf(
                SideMenuBar(false, "Member", "Ecosystem"),
                SideMenuBar(false, "Financial Advisors", "Ecosystem"),
                SideMenuBar(false, "Startups", "Ecosystem"),
                SideMenuBar(false, "Small Business", "Ecosystem"),
                SideMenuBar(false, "Investor/ VC", "Ecosystem"),
                SideMenuBar(false, "Life Insurance", "Ecosystem"),
                SideMenuBar(false, "Brokers/Dealer", "Ecosystem"),
                SideMenuBar(false, "Investment Mangers", "Ecosystem")
            )

            "Tools" -> listOf(
                SideMenuBar(false, "Portfolio", "Tools"),
                SideMenuBar(false, "Performance", "Tools"),
                SideMenuBar(false, "Engagement", "Tools"),
                SideMenuBar(false, "Investment Plan", "Tools"),
                SideMenuBar(false, "Financial/Business", "Tools"),
                SideMenuBar(false, "Research Engine", "Tools")
            )

            else -> emptyList()
        }
    }



    /** handle nav clicks **/
    private fun handleNavClick(title: String?, heading: String?) {

        if (title.isNullOrEmpty()) return

        when (heading) {

            "Exchange" -> {
                when (title) {
                    "Member" -> selectedCategoryForExchange = "Members"
                    "Financial Advisors" -> selectedCategoryForExchange = "Advisors"
                    "Startups" -> selectedCategoryForExchange = "Startups"
                    "Small Business" -> selectedCategoryForExchange = "Small Businesses"
                    "Investor/ VC" -> selectedCategoryForExchange = "Investor"
                    "Life Insurance" -> selectedCategoryForExchange = "Insurance"
                    "Brokers/Dealer" -> selectedCategoryForExchange = "Broker"
                    "Investment Mangers" -> selectedCategoryForExchange = "Investment Managers"
                }

                displayFragment(ExchangeFragment())
                updateOtherUI(getString(R.string.exchange))
            }

            "Ecosystem" -> {
                when (title) {
                    "Member" -> selectedCategoryForEcosystem = "Members"
                    "Financial Advisors" -> selectedCategoryForEcosystem = "Advisors"
                    "Startups" -> selectedCategoryForEcosystem = "Startups"
                    "Small Business" -> selectedCategoryForEcosystem = "Small Businesses"
                    "Investor/ VC" -> selectedCategoryForEcosystem = "Investor"
                    "Life Insurance" -> selectedCategoryForEcosystem = "Insurance"
                    "Brokers/Dealer" -> selectedCategoryForEcosystem = "Broker"
                    "Investment Mangers" -> selectedCategoryForEcosystem = "Investment Managers"
                }

                displayFragment(EcosystemFragment())
                updateOtherUI(getString(R.string.ecosystem))
            }

            "Tools" -> {
                when (title) {
                    "Performance" -> {
                        displayFragment(PerformanceFragment())
                        updateOtherUI("Performance")
                    }

                    "Portfolio" -> {
                        displayFragment(InvestmentTrackerFragment())
                        updateOtherUI("Investment & financial tracker")
                    }

                    "Research Engine" -> {
                        displayFragment(AiChatFragment())
                        updateOtherUI("Research Engine")
                    }

                    "Investment Plan" -> {
                        displayFragment(InvestmentPlanFragment())
                        updateOtherUI("Investment Plan")
                    }

                    "Financial/Business" -> {
                        displayFragment(FinancialBusinessFragment())
                        updateOtherUI("Financial Business")
                    }
                    "Engagement" -> {
                        displayFragment(EngagementFragment())
                        updateOtherUI("Engagement")
                    }
                }
            }

            else  -> {
                // Main menu (no heading)
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

                    "Notification" -> {
                        displayFragment(NotificationFragment())
                        updateOtherUI("Notification")
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

    fun updateOtherUI(title: String) {
        Log.i("gfdggdfg", "updateOtherUI: $title ")
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
            R.id.tvYes ->{
                supportFragmentManager.popBackStack()
                leavePagePopup.dismiss()
            }
            R.id.tvLeavePageNo ->{
                leavePagePopup.dismiss()
            }
        }
    }

    override fun onBackPressed() {

        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressedDispatcher.onBackPressed()
        }
        finishAffinity()
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
