package com.tech.young.ui.notification_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.tech.young.data.api.Constants
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.data.model.NotificationApiResponse
import com.tech.young.data.model.TrendingTopicApiResponse
import com.tech.young.databinding.FragmentNotificationBinding
import com.tech.young.databinding.ItemLayoutNotificationBinding
import com.tech.young.ui.home.HomeActivity
import com.tech.young.ui.user_profile.UserProfileFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationFragment : BaseFragment<FragmentNotificationBinding>() {


    private lateinit var notificationAdapter : SimpleRecyclerViewAdapter<NotificationApiResponse.Data.Notification, ItemLayoutNotificationBinding>
    private val viewModel : NotificationFragmentVm by viewModels()
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
                                        notificationAdapter.list = myDataModel.data!!.notifications
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
        val data = HashMap<String, Any>()
        data["page"] = 1
        viewModel.getNotification(data, Constants.NOTIFICATION)
    }

}