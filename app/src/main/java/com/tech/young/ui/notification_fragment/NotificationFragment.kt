package com.tech.young.ui.notification_fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.tech.young.data.model.NotificationApiResponse
import com.tech.young.data.model.TrendingTopicApiResponse
import com.tech.young.databinding.FragmentNotificationBinding
import com.tech.young.databinding.ItemLayoutNotificationBinding
import com.tech.young.ui.exchange.exchange_share_detail.ExchangeShareDetailFragment
import com.tech.young.ui.exchange.stream_detail_fragment.StreamDetailFragment
import com.tech.young.ui.home.HomeActivity
import com.tech.young.ui.inbox.view_message.ViewMessageFragment
import com.tech.young.ui.user_profile.UserProfileFragment
import com.tech.young.ui.vault_screen.vault_room.VaultRoomFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationFragment : BaseFragment<FragmentNotificationBinding>() {


    private lateinit var notificationAdapter : SimpleRecyclerViewAdapter<NotificationApiResponse.Data.Notification, ItemLayoutNotificationBinding>
    private val viewModel : NotificationFragmentVm by viewModels()

    private var page  = 1
    private var isLoading = false
    private var isLastPage = false
    private var totalPages : Int ? = null


    override fun getLayoutResource(): Int {
        return R.layout.fragment_notification
    }

    override fun getViewModel(): BaseViewModel {
    return viewModel
    }

    override fun onCreateView(view: View) {
        getNotification()
        initAdapter()
        initObserver()

        binding.rvNotification.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // 👇 Only continue if scrolling down
                if (dy <= 0) return

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && page < totalPages!!) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3 &&
                        firstVisibleItemPosition >= 0
                    ) {
                        isLoading = true // ✅ Lock before load
                        loadMoreData()
                    }
                }
            }

            private fun loadMoreData() {
                isLoading = true
                page++

                val data = HashMap<String, Any>()
                data["page"] = page
                data["limit"] = 20
                viewModel.getNotification(data, Constants.NOTIFICATION)
            }
        })

    }

    private fun initAdapter() {
      notificationAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_notification, BR.bean){v,m,pos ->
          when(v.id){
              R.id.ivImage ->{
                  val bundle = Bundle().apply {
                      putString("from", "user_profile")
                      putString("userId", m.senderId?._id) // assuming m._id is a String
                  }
//                    val name = m.firstName + " " + m.lastName  // ← add space here
                  val name  = m?.senderId?.firstName + " " + m?.senderId?.lastName
                  HomeActivity.userName  = name
                  val userProfileFragment = UserProfileFragment().apply {
                      arguments = bundle
                  }

                  requireActivity().supportFragmentManager.beginTransaction()
                      .replace(R.id.frameLayout, userProfileFragment)
                      .addToBackStack(null)
                      .commit()
              }
              R.id.clInbox ->{
                  when(m.type){
                      "share" ->{
                          Log.i("dsadasdas", "onCreateView: ${m.postId}")
                          val fragment = ExchangeShareDetailFragment().apply {
                              arguments = Bundle().apply {
                                  putString("userId", m.postId)
                              }
                          }

                          requireActivity().supportFragmentManager.beginTransaction()
                              .replace(R.id.frameLayout, fragment)
                              .addToBackStack(null)
                              .commit()
                      }


                      "message" -> {
                          val chatUser = UserData(
                              _id = m.senderId?._id, // ✅ should map from FcmPayload.userId, not payload?._id
                              profileImage = m.senderId?.profileImage,
                              role = m.senderId?.role,
                              username = m.senderId?.username,
                              firstName =  m.senderId?.firstName,
                              lastName = m.senderId?.lastName
                          )

                          val bundle = Bundle().apply {
                              putString("from", "view_message")
                              putString("threadId", m.chatId)
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

                      "live_stream" ->{

                      }
                      "customer" ->{

                      }
                      "follower" ->{
                          Log.i("fdfdsfsd", "initAdapter: ${m.senderId?.username}")

                          val name  = m?.senderId?.firstName + " " + m?.senderId?.lastName
                          HomeActivity.userName  = name
                          val userProfileFragment = UserProfileFragment().apply {
                              arguments  = Bundle().apply {
                                  putString("from", "user_profile")
                                  putString("userId", m.senderId?._id) // assuming m._id is a String
                              }
                          }

                          requireActivity().supportFragmentManager.beginTransaction()
                              .replace(R.id.frameLayout, userProfileFragment)
                              .addToBackStack(null)
                              .commit()


                      }
                      "share_comment" ->{
                          val fragment = ExchangeShareDetailFragment().apply {
                              arguments = Bundle().apply {
                                  putString("userId", m.shareId)
                              }
                          }

                          requireActivity().supportFragmentManager.beginTransaction()
                              .replace(R.id.frameLayout, fragment)
                              .addToBackStack(null)
                              .commit()
                      }
                      "stream_comment" ->{
                          val fragment = StreamDetailFragment().apply {
                              arguments = Bundle().apply {
                                  putString("streamId", m.streamId)
                              }
                          }

                          requireActivity().supportFragmentManager.beginTransaction()
                              .replace(R.id.frameLayout, fragment)
                              .addToBackStack(null)
                              .commit()
                      }
                      "vault_comment" ->{
                          val fragment = VaultRoomFragment().apply {
                              arguments = Bundle().apply {
                                  putString("vaultId", m.vaultId)
                              }
                          }

                          requireActivity().supportFragmentManager.beginTransaction()
                              .replace(R.id.frameLayout, fragment)
                              .addToBackStack(null)
                              .commit()
                      }

                      "share_like" ->{
                          val fragment = ExchangeShareDetailFragment().apply {
                              arguments = Bundle().apply {
                                  putString("userId", m.shareId)
                              }
                          }

                          requireActivity().supportFragmentManager.beginTransaction()
                              .replace(R.id.frameLayout, fragment)
                              .addToBackStack(null)
                              .commit()
                      }
                      "reshare" ->{
                          val fragment = ExchangeShareDetailFragment().apply {
                              arguments = Bundle().apply {
                                  putString("userId", m.shareId)
                              }
                          }

                          requireActivity().supportFragmentManager.beginTransaction()
                              .replace(R.id.frameLayout, fragment)
                              .addToBackStack(null)
                              .commit()
                      }

                      "stream_like" ->{
                          val fragment = StreamDetailFragment().apply {
                              arguments = Bundle().apply {
                                  putString("streamId", m.streamId)
                              }
                          }

                          requireActivity().supportFragmentManager.beginTransaction()
                              .replace(R.id.frameLayout, fragment)
                              .addToBackStack(null)
                              .commit()
                      }

                      "ratings_share" ->{
                          val fragment = ExchangeShareDetailFragment().apply {
                              arguments = Bundle().apply {
                                  putString("userId", m.shareId)
                              }
                          }

                          requireActivity().supportFragmentManager.beginTransaction()
                              .replace(R.id.frameLayout, fragment)
                              .addToBackStack(null)
                              .commit()
                      }
                      "ratings_stream" ->{
                          val fragment = StreamDetailFragment().apply {
                              arguments = Bundle().apply {
                                  putString("streamId", m.streamId)
                              }
                          }

                          requireActivity().supportFragmentManager.beginTransaction()
                              .replace(R.id.frameLayout, fragment)
                              .addToBackStack(null)
                              .commit()
                      }
                      "ratings_vault" ->{
                          val fragment = VaultRoomFragment().apply {
                              arguments = Bundle().apply {
                                  putString("vaultId", m.vaultId)
                              }
                          }

                          requireActivity().supportFragmentManager.beginTransaction()
                              .replace(R.id.frameLayout, fragment)
                              .addToBackStack(null)
                              .commit()
                      }
                      "ratings_user" ->{
                          val name  = m?.senderId?.firstName + " " + m?.senderId?.lastName
                          HomeActivity.userName  = name
                          val userProfileFragment = UserProfileFragment().apply {
                              arguments  = Bundle().apply {
                                  putString("from", "user_profile")
                                  putString("userId", m.senderId?._id) // assuming m._id is a String
                              }
                          }

                          requireActivity().supportFragmentManager.beginTransaction()
                              .replace(R.id.frameLayout, userProfileFragment)
                              .addToBackStack(null)
                              .commit()
                      }
                  }
              }
          }
      }
        binding.rvNotification.adapter = notificationAdapter
        notificationAdapter.notifyDataSetChanged()
    }

    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    showLoading()
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "getNotification" ->{
                           val myDataModel : NotificationApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    if (myDataModel.data!!.notifications != null){
                                    //    notificationAdapter.list = myDataModel.data!!.notifications
                                        totalPages = myDataModel.pagination?.totalPages ?: 1
                                        if (page <= totalPages!!) {
                                            isLoading = false
                                        }
                                        if (page == 1){
                                            notificationAdapter.list = myDataModel.data!!.notifications
                                            notificationAdapter.notifyDataSetChanged()
                                        } else{
                                            notificationAdapter.addToList( myDataModel.data!!.notifications)
                                            notificationAdapter.notifyDataSetChanged()

                                        }
                                    }

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

    private fun getNotification() {
        page = 1
        val data = HashMap<String, Any>()
        data["page"] = 1
        data["limit"] = 20
        viewModel.getNotification(data, Constants.NOTIFICATION)
    }

}