package com.tech.young.ui.vault_screen.vault_room

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
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
import com.tech.young.data.model.AddCommentApiResponseVault
import com.tech.young.data.model.AddRatingApiResponse
import com.tech.young.data.model.CommentLikeDislikeApiResponse
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.data.model.GetCommentApiResponse
import com.tech.young.data.model.JoinVaultRoomApiResponse
import com.tech.young.data.model.VaultDetailApiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentVaultRoomBinding
import com.tech.young.databinding.ItemLayoutCommentViewBinding
import com.tech.young.databinding.ItemLayoutMemberViewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VaultRoomFragment : BaseFragment<FragmentVaultRoomBinding>() {

    private lateinit var membersAdapter: SimpleRecyclerViewAdapter<VaultDetailApiResponse.Data.Vault.Member, ItemLayoutMemberViewBinding>
    private lateinit var commentAdapter: SimpleRecyclerViewAdapter<GetCommentApiResponse.Data.Comment, ItemLayoutCommentViewBinding>
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>
    private var vaultId : String ? = null

    private var getList = listOf(
        "", "", "", "", ""
    )


    private val viewModel: VaultRoomVM by viewModels()
    override fun onCreateView(view: View) {

        getVaultData()
        viewModel.getAds(Constants.GET_ADS)
        getCommentData()
        // click
        initOnClick()
        // adapter
        initAdapter()

        setObserver()

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (fromUser) {
                binding.tvAverageRating.text = "($rating)"
                Handler(Looper.getMainLooper()).postDelayed({
                    setRating(rating)
                }, 1000)
                Log.i("RatingBar", "User selected rating: $rating")
                // Handle the rating value
            }
        }
    }

    private fun setRating(rating: Float) {
        if (vaultId != null){
            val data = HashMap<String,Any>()
            data["ratings"]  = rating
            data["type"] = "vault"
            data["id"] =  vaultId.toString()

            viewModel.rating(data,Constants.RATING)
        }
    }

    private fun getCommentData() {
        val data = HashMap<String,Any>()
        if (vaultId != null){
            data["id"] = vaultId.toString()
            data ["type"] =  "vault"
            viewModel.getComment(data,Constants.GET_COMMENT)
        }

    }


    private fun setObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    showLoading()
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "joinLeaveRoom"->{
                            val myDataModel : JoinVaultRoomApiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    getVaultData()

                                }
                            }
                        }
                        "getVaultData" -> {
                            val myDataModel: VaultDetailApiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null && myDataModel.data != null) {
                                binding.bean = myDataModel.data
                                val isMember = myDataModel.data?.vault?.isMember == true

                                // Set members list
                                myDataModel.data?.vault?.members?.let {
                                    membersAdapter.list = it
                                }

                                // Update UI based on membership
                                requireActivity().runOnUiThread {
                                    binding.tvJoin.apply {
                                        if (isMember) {
                                            text = "Leave"
                                            backgroundTintList = null
                                            setBackgroundResource(R.drawable.bg_cons_stroke)
                                            setTextColor(
                                                ContextCompat.getColor(context, R.color.colorPrimary)
                                            )
                                            binding.rvComments.visibility = View.VISIBLE
                                            binding.etChat.visibility = View.VISIBLE
                                            binding.ivSendChat.visibility = View.VISIBLE

                                        } else {
                                            text = "Join"
                                            setBackgroundResource(R.drawable.corner_radius_10)
                                            backgroundTintList =
                                                ContextCompat.getColorStateList(context, R.color.colorPrimary)
                                            setTextColor(
                                                ContextCompat.getColor(context, android.R.color.white)
                                            )
                                            binding.rvComments.visibility = View.GONE
                                            binding.etChat.visibility = View.GONE
                                            binding.ivSendChat.visibility = View.GONE
                                        }
                                    }
                                }
                            }
                        }

                        "getComment" ->{
                            val myDataModel : GetCommentApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data?.comments != null){
                                    commentAdapter.list = myDataModel.data?.comments
                                }
                            }
                        }
                        "addComment" ->{
                            val  myDataModel : AddCommentApiResponseVault ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel  != null){
                                if (myDataModel.data != null){
                                    showToast(myDataModel.message.toString())
                                    getCommentData()
                                }
                            }

                        }
                        "likeDislikeComment" ->{
                            val myDataModel : CommentLikeDislikeApiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    getCommentData()
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
                        "rating" ->{
                            hideLoading()
                            val myDataModel : AddRatingApiResponse? =  BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){

                                }
                            }
                        }
                    }


                }
                Status.ERROR->{
                    hideLoading()
                }
                else ->{

                }

            }
        })
    }

    private fun getVaultData() {
        vaultId = arguments?.getString("vaultId")
        Log.i("dsadas", "getVaultData: $vaultId")
        if (vaultId != null){
            viewModel.getVaultData(Constants.GET_VAULT_DETAIL+vaultId)
        }

    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_vault_room
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {
                R.id.tvJoin -> {
                    viewModel.joinLeaveRoom(Constants.JOIN_LEAVE_VAULT+vaultId)

                }
                R.id.ivBack ->{
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                R.id.ivSendChat ->{
                    if (TextUtils.isEmpty(binding.etChat.text.toString().trim())){
                        showToast("Please enter message")
                    }
                    else{
                        val data = HashMap<String,Any>()
                        data["id"] =  vaultId.toString()
                        data["comment"] = binding.etChat.text.toString().trim()
                        data["type"] = "vault"
                        viewModel.addComment(data,Constants.ADD_COMMENT)
                        binding.etChat.setText("")

                }
                }
            }
        }
    }

    private fun initAdapter() {
        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        binding.rvAds.adapter = adsAdapter

        commentAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_comment_view, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.ivHearts -> {
                        viewModel.likeDislikeComment(Constants.LIKE_DISLIKE_COMMENT+m._id)
                    }
                }
            }
        binding.rvComments.adapter = commentAdapter


        membersAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_member_view, BR.bean) { v, m, pos ->
                when (v.id) {


                }
            }
        binding.rvMembers.adapter = membersAdapter
    }

}
