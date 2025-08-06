package com.tech.young.ui.my_profile_screens.profile_fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.DialogRatingBinding
import com.tech.young.databinding.FragmentProfileBinding
import com.tech.young.databinding.ShareProfileItemViewBinding
import com.tech.young.databinding.YourPhotosItemViewBinding
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.my_profile_screens.YourProfileVM
import com.tech.young.ui.share_screen.CommonShareFragment
import com.tech.young.ui.stream_screen.CommonStreamFragment
import com.tech.young.ui.vault_screen.CommonVaultFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    private val viewModel: YourProfileVM by viewModels()
    // adapter
    private lateinit var yourImageAdapter:SimpleRecyclerViewAdapter<String,YourPhotosItemViewBinding>
    private lateinit var shareAdapter:SimpleRecyclerViewAdapter<String,ShareProfileItemViewBinding>
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>
    private var userId : String ? = null
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
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("from", "payment_details")
                        intent.putExtra("userId",userId)
                        startActivity(intent)
                    }

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
                                    binding.bean = myDataModel.data
                                    yourImageAdapter.list = myDataModel.data?.user?.additionalPhotos
                                    yourImageAdapter.notifyDataSetChanged()
                                }
                            }

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

    /** handle adapter **/
    private fun initAdapter() {
        yourImageAdapter = SimpleRecyclerViewAdapter(
            R.layout.your_photos_item_view, BR.bean
        ) { v, m, pos ->
            when (v.id) {

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
            setDimAmount(0f) // We use purple background in the layout instead
        }
        BindingUtils.statusBarStyleBlack(requireActivity())
        BindingUtils.styleSystemBars(requireActivity(), getColor(requireContext(), R.color.colorSecondary2))

        bindingDialog.close.setOnClickListener {
            BindingUtils.statusBarStyleBlack(requireActivity())
            BindingUtils.styleSystemBars(requireActivity(), getColor(requireContext(), R.color.white))
            dialog.dismiss()
        }

        // Example rating data: 5★ to 1★
        val ratingData = listOf(1985, 356, 130, 90, 33)
        val total = ratingData.sum().toFloat()

        val container = bindingDialog.ratingDistributionContainer
        container.removeAllViews()

        for (i in 0 until 5) {
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_rating_distribution, container, false)

            val starText = itemView.findViewById<TextView>(R.id.star_text)
            val starCount = itemView.findViewById<TextView>(R.id.star_count)
            val starProgress = itemView.findViewById<ProgressBar>(R.id.star_progress)

            val percent = (ratingData[i] / total * 100).toInt()

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