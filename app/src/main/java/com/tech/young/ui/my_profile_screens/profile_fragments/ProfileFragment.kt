package com.tech.young.ui.my_profile_screens.profile_fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
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
import com.tech.young.data.model.GetProfileApiResponse
import com.tech.young.data.model.GetRatingApiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.DialogRatingBinding
import com.tech.young.databinding.FragmentProfileBinding
import com.tech.young.databinding.ShareProfileItemViewBinding
import com.tech.young.databinding.YourPhotosItemViewBinding
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.my_profile_screens.YourProfileVM
import com.tech.young.ui.my_profile_screens.common_ui.EditProfileDetailFragment
import com.tech.young.ui.my_share.MyShareFragment
import com.tech.young.ui.payment.PaymentDetailsFragment
import com.tech.young.ui.share_screen.CommonShareFragment
import com.tech.young.ui.stream_screen.CommonStreamFragment
import com.tech.young.ui.vault_screen.CommonVaultFragment
import com.tech.young.ui.vault_screen.people_screen.PeopleFragment
import com.tech.young.ui.vault_screen.vault_room.VaultRoomFragment
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.ignoreIoExceptions

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    private val viewModel: YourProfileVM by viewModels()
    // adapter
    private lateinit var yourImageAdapter:SimpleRecyclerViewAdapter<String,YourPhotosItemViewBinding>
    private lateinit var shareAdapter:SimpleRecyclerViewAdapter<String,ShareProfileItemViewBinding>
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>
    private var userId : String ? = null
    private var  role : String ? = null
    private var ratingsMap: GetRatingApiResponse.Data.RatingsCount? = null
    private var averageRating : Double = 0.0
    private var totalCount : Int = 0


    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()


    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_profile
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView() {

        binding.includeShare.tabShare.setOnClickListener {
//            val intent = Intent(requireContext(), CommonActivity::class.java).putExtra("from","common_share")
//            startActivity(intent)

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, CommonShareFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.includeShare.tabStream.setOnClickListener {
//            val intent = Intent(requireContext(), CommonActivity::class.java).putExtra("from","common_stream")
//            startActivity(intent)

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, CommonStreamFragment())
                .addToBackStack(null)
                .commit()
        }


        binding.includeShare.tabVault.setOnClickListener {
//            val intent = Intent(requireContext(), CommonActivity::class.java).putExtra("from","common_vault")
//            startActivity(intent)

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, CommonVaultFragment())
                .addToBackStack(null)
                .commit()
        }


        // adapter
        initAdapter()
    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when(it?.id){
                R.id.icRatingArrow->{
                    showRatingDialog()

                }
                R.id.icAccount, R.id.consAccountDetails->{
                    if (userId != null){
//                        val intent = Intent(requireContext(), CommonActivity::class.java)
//                        intent.putExtra("from", "payment_details")
//                        intent.putExtra("userId",userId)
//                        startActivity(intent)


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

                R.id.llShare ->{
//                    val intent = Intent(requireContext(), CommonActivity::class.java)
//                    intent.putExtra("from", "myShare")
//                    intent.putExtra("userId", userId)
//                    intent.putExtra("role", role)
//                    startActivity(intent)

                    val fragment = MyShareFragment().apply {
                        arguments = Bundle().apply {
                            putString("userId", userId)
                            putString("role", role)
                        }
                    }

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
                }

                R.id.llFollowing ->{

                    val fragment = PeopleFragment().apply {
                        arguments = Bundle().apply {
                           putString("type", "followers")
                            putString("side","profile")
                            putString("userId",userId)

                        }
                    }


                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
                }

                R.id.llFollowedBy ->{
                    val fragment = PeopleFragment().apply {
                        arguments = Bundle().apply {
                            putString("type", "followedBy")
                            putString("side","profile")
                            putString("userId",userId)

                        }
                    }


                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
                }
                R.id.llCustomers ->{
                    val fragment = PeopleFragment().apply {
                        arguments = Bundle().apply {
                            putString("type", "customers")
                            putString("side","profile")
                            putString("userId",userId)


                        }
                    }


                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit()
                }


            }
        }
    }

    /** handle observer**/
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    showLoading()
                }
                Status.SUCCESS ->{

                    when(it.message){
                        "getProfile" ->{
                            viewModel.getAds(Constants.GET_ADS)
                            val myDataModel : GetProfileApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    userId = myDataModel.data?.user?._id
                                    role  = myDataModel.data?.user?.role
                                    binding.bean = myDataModel.data
                                    yourImageAdapter.list = myDataModel.data?.user?.additionalPhotos
                                    yourImageAdapter.notifyDataSetChanged()
                                }
                            }

                            getRating()

                        }
                            "getAds" ->{
                                hideLoading()
                                val myDataModel : GetAdsAPiResponse ? = BindingUtils.parseJson(it.data.toString())
                                if (myDataModel != null){
                                    if (myDataModel.data != null){
                                        adsAdapter.list = myDataModel.data?.ads
                                    }
                                }
                            }

                        "getRating" ->{
                            hideLoading()
                            val myDataModel : GetRatingApiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data  != null){
                                    ratingsMap = myDataModel.data?.ratingsCount
                                    averageRating = myDataModel.data!!.averageRating!!
                                    totalCount = myDataModel.data!!.totalCount!!
                                }
                            }
                        }


                    }
                }
                Status.ERROR ->{
                    hideLoading()
                    showToast(it.message.toString())
                }
                else ->{}
            }
        })

    }

    private fun getRating() {
        val data =  HashMap<String,Any>()
        data["id"] = userId.toString()
        data["type"] = "user"
        viewModel.getRating(data,Constants.GET_RATING)
    }

    /** handle adapter **/
    private fun initAdapter() {
        yourImageAdapter = SimpleRecyclerViewAdapter(
            R.layout.your_photos_item_view, BR.bean
        ) { v, m, pos ->
            when (v.id) {
                R.id.ivImage1 ->{
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("from", "image")
                    intent.putExtra("url", m)
                    startActivity(intent)
                }
            }
        }
        yourImageAdapter.list = getList
        binding.rvYourPhotos.adapter = yourImageAdapter


        shareAdapter=SimpleRecyclerViewAdapter(R.layout.share_profile_item_view,BR.bean){
                v,m,pos->
            when(v.id){

            }
//            binding.rvShare.clipToPadding = false
//            binding.rvShare.clipChildren = false
//            if (pos == getList.size - 1) {
//                binding.rvShare.updatePadding(left = 0, right = 0)
//            } else {
//                binding.rvShare.updatePadding(left = 0, right = 20)
//            }
        }

        shareAdapter.list=getList
        binding.rvShare.adapter=shareAdapter


        adsAdapter=SimpleRecyclerViewAdapter(R.layout.ads_item_view,BR.bean){
                v,m,pos->
            when(v.id){

            }
        }
        binding.rvAds.adapter=adsAdapter
    }

    private var getList = listOf(
        "", "", "", "", ""
    )

    /** handle dialog **/

    private fun showRatingDialog() {
        val bindingDialog = DialogRatingBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.setContentView(bindingDialog.root)

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setDimAmount(0f)
        }

        BindingUtils.statusBarStyleBlack(requireActivity())
        BindingUtils.styleSystemBars(requireActivity(), getColor(requireContext(), R.color.colorSecondary2))

        bindingDialog.close.setOnClickListener {
            BindingUtils.statusBarStyleBlack(requireActivity())
            BindingUtils.styleSystemBars(requireActivity(), getColor(requireContext(), R.color.white))
            dialog.dismiss()
        }

        // Set average rating text
        bindingDialog.averageRating.text = String.format("%.1f", averageRating)
        // Set star rating bar
        bindingDialog.ratingBar.rating = averageRating.toFloat()

        // Set total ratings text
        bindingDialog.totalRatings.text = "$totalCount ratings"
        // ✅ Always get 5★ → 1★ order
        val ratingData = listOf(
            ratingsMap?.`5` ?: 0,
            ratingsMap?.`4` ?: 0,
            ratingsMap?.`3` ?: 0,
            ratingsMap?.`2` ?: 0,
            ratingsMap?.`1` ?: 0
        )

        val total = ratingData.sum().toFloat()
        val container = bindingDialog.ratingDistributionContainer
        container.removeAllViews()

        for (i in 0 until 5) {
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_rating_distribution, container, false)

            val starText = itemView.findViewById<TextView>(R.id.star_text)
            val starCount = itemView.findViewById<TextView>(R.id.star_count)
            val starProgress = itemView.findViewById<ProgressBar>(R.id.star_progress)

            // ✅ If total = 0, percent will be 0
            val percent = if (total > 0) {
                (ratingData[i] / total * 100).toInt()
            } else {
                0
            }

            starText.text = "${5 - i}★"
            starCount.text = ratingData[i].toString()
            starProgress.progress = percent

            container.addView(itemView)
        }

        dialog.show()
    }



    override fun onResume() {
        super.onResume()
        viewModel.getProfile(Constants.GET_USER_PROFILE)
    }
}