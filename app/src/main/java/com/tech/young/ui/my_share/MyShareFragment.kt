package com.tech.young.ui.my_share

import android.os.Bundle
import android.util.Log
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
import com.tech.young.data.model.GetSavedPostApiResponse
import com.tech.young.databinding.FragmentMyShareBinding
import com.tech.young.databinding.ItemLayoutMyShareBinding
import com.tech.young.databinding.ShareItemViewBinding
import com.tech.young.ui.exchange.exchange_share_detail.ExchangeShareDetailFragment
import com.tech.young.ui.home.HomeActivity
import com.tech.young.ui.user_profile.UserProfileFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyShareFragment : BaseFragment<FragmentMyShareBinding>() {


    private val viewModel : MyShareFragmentVm  by viewModels()
    private var role : String ? = null
    private var id : String ? = null

    private lateinit var shareAdapter: SimpleRecyclerViewAdapter<GetSavedPostApiResponse.Data.Post, ItemLayoutMyShareBinding>


    override fun onCreateView(view: View) {
        getSavedData()
        initObserver()
           initAdapter()
    }



    private fun getSavedData() {
        role= arguments?.getString("role").toString()
        id = arguments?.getString("userId").toString()

        Log.i("dsdsd", "getSavedData: $id , $role ")
        if(role != null && id != null){
            val data = HashMap<String,Any>()
      //     data["userType"] = role!!
        //    data["type"] = "share"
            data["id"] = id!!
            data["page"] = 1
            viewModel.savedShare(data, Constants.GET_SAVED_POST)
        }



    }



    private fun initAdapter() {


        shareAdapter=SimpleRecyclerViewAdapter(R.layout.item_layout_my_share, BR.bean){
                v,m,pos->
            when(v.id){
                R.id.consMain ->{
//                    val intent = Intent(requireContext(), CommonActivity::class.java)
//                    intent.putExtra("from", "exchange_share_details")
//                    intent.putExtra("userId", m._id)
//                    startActivity(intent)

                    val fragment = ExchangeShareDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString("userId", m._id)
                        }
                    }

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
                }
                R.id.ivUserImage , R.id.tvUserName->{
                    val bundle = Bundle().apply {
                        putString("from", "user_profile")
                        putString("userId", m?.userId?._id) // assuming m._id is a String
                    }
//                    val name = m.firstName + " " + m.lastName  // ← add space here
                    val name  = m?.userId?.firstName + " " + m?.userId?.lastName
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
        binding.rvShare.adapter=shareAdapter
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
                        "savedShare" ->{
                            var myDataModel : GetSavedPostApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data!= null){
                                    shareAdapter.list = myDataModel.data!!.posts
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


    override fun getLayoutResource(): Int {
        return R.layout.fragment_my_share
    }

    override fun getViewModel(): BaseViewModel {
          return viewModel
    }

}