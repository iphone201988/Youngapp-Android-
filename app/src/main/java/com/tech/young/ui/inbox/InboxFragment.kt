package com.tech.young.ui.inbox

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.UserData
import com.tech.young.data.api.Constants
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.data.model.GetChatApiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentInboxBinding
import com.tech.young.databinding.ItemLayoutInboxBinding
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.home.HomeActivity
import com.tech.young.ui.inbox.view_message.ViewMessageFragment
import com.tech.young.ui.payment.PaymentDetailsFragment
import com.tech.young.ui.user_profile.UserProfileFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class InboxFragment : BaseFragment<FragmentInboxBinding>() {
    private val viewModel : InboxFragmentVm by viewModels()

    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>
    private lateinit var inboxAdapter: SimpleRecyclerViewAdapter<GetChatApiResponse.Data.Chat,ItemLayoutInboxBinding>


    private var page  = 1
    private var isLoading = false
    private var isLastPage = false
    private var totalPages : Int ? = null



    override fun onCreateView(view: View){
        // adapter
        initAdapter()
        // click

        getChat()
        initOnClick()
        // observer
        initObserver()


        binding.nestedScrollView.setOnScrollChangeListener { v: NestedScrollView, _, scrollY, _, oldScrollY ->
            val view = v.getChildAt(v.childCount - 1)
            if (view != null) {
                val diff = view.bottom - (v.height + v.scrollY)
                if (diff <= 0 && scrollY > oldScrollY) {

                    Log.d("Pagination", "Reached bottom, loading next page…")
                    // ✅ User reached bottom
                    if (!isLoading && page < totalPages!!) {
                        isLoading = true
                        loadNextPage()
                    }
                }
            }
        }


    }

    private fun loadNextPage() {
        isLoading = true
        page++

        val data = HashMap<String, Any>()
        data["page"]  = page
        data["limit"] = 10
        viewModel.getChat(Constants.GET_CHAT, data)

    }

    private fun getChat() {
        page = 1

        val data = HashMap<String, Any>()
        data["page"]  = 1
        data["limit"] = 10
        viewModel.getChat(Constants.GET_CHAT,data)
        viewModel.getAds(Constants.GET_ADS)
    }

    override fun getLayoutResource(): Int {
         return R.layout.fragment_inbox
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private fun initAdapter() {
        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        binding.rvAds.adapter = adsAdapter

        inboxAdapter=SimpleRecyclerViewAdapter(R.layout.item_layout_inbox,BR.bean){
                v,m,pos->
            when(v.id){
                R.id.clInbox ,R.id.ivChatIcon->{
                    val user = m.chatUsers?.getOrNull(0) // safer than get(0)
                    val fullName = buildString {
                        user?.firstName?.takeIf { it.isNotEmpty() }?.let { append(it) }
                        user?.lastName?.takeIf { it.isNotEmpty() }?.let { append(" $it") }
                        user?.username?.takeIf { it.isNotEmpty() }?.let { append(" ($it)") }
                    }.trim()

                    val chatUser = UserData(
                        _id = m.chatUsers?.get(0)?._id,
                        profileImage = m.chatUsers?.get(0)?.profileImage,
                        role = m.chatUsers?.get(0)?.role,
                       username =  m.chatUsers?.get(0)?.username,
                        firstName =  m.chatUsers?.get(0)?.firstName,
                        lastName =  m.chatUsers?.get(0)?.lastName
                    )
//                    val intent=Intent(requireContext(),CommonActivity::class.java)
//                    intent.putExtra("from","view_message")
//                    intent.putExtra("threadId",m._id)
//                    intent.putExtra("userData", chatUser)// Pass entire object
//                    startActivity(intent)


                    val bundle = Bundle().apply {
                        putString("from", "view_message")
                        putString("threadId", m._id)
                        putParcelable("userData", chatUser)
                    }

                    val viewMessageFragment = ViewMessageFragment().apply {
                        arguments = bundle
                    }

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, viewMessageFragment)
                        .addToBackStack(null)
                        .commit()
                }
                R.id.ivAboutIcon->{
//                    val intent=Intent(requireContext(),CommonActivity::class.java)
//                    intent.putExtra("from","user_profile")
//                    intent.putExtra("userId","${m.chatUsers?.get(0)?._id}")
//                    startActivity(intent)

                    val name = m.chatUsers?.get(0)?.username  // ← add space here
                    HomeActivity.userName  = name
                    val fragment = UserProfileFragment().apply {
                        arguments = Bundle().apply {
                            putString("userId", m.chatUsers?.get(0)?._id)
                        }
                    }

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()

                }

            }
        }
        binding.rvInbox.adapter = inboxAdapter
    }

    private var getList = listOf(
        "", "", "", "", ""
    )

    private fun initOnClick(){
        viewModel.onClick.observe(requireActivity()){
            when(it?.id){
                R.id.ivAddIcon->{
                    val intent=Intent(requireContext(),CommonActivity::class.java)
                    intent.putExtra("from","new_message")

                    startActivity(intent)
                }
            }
        }
    }

    private fun initObserver(){
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    showLoading()
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "getChat" -> {
                            val myDataModel: GetChatApiResponse? = BindingUtils.parseJson(it.data.toString())

                            if (myDataModel != null && myDataModel.data != null) {
                                totalPages = myDataModel?.pagination?.total ?: 1

                                if (page <= totalPages!!) {
                                    isLoading = false
                                }

                                // --- FILTER CHAT USERS ---
                                val id = sharedPrefManager.getUserId()
                                val filteredChats = myDataModel.data?.chats?.map { chat ->
                                    val filteredUsers = chat?.chatUsers?.filter { it?._id != id }
                                    chat?.copy(chatUsers = filteredUsers)
                                } ?: emptyList()

                                // --- ASSIGN LIST BASED ON PAGE NUMBER ---
                                if (page == 1) {
                                    inboxAdapter.list = filteredChats
                                    inboxAdapter.notifyDataSetChanged()
                                } else {
                                    inboxAdapter.addToList(filteredChats)
                                    inboxAdapter.notifyDataSetChanged()
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
                    showToast(it.message.toString())
                }
                else ->{

                }
            }
        })

    }
}