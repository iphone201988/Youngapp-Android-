package com.tech.young.ui.vault_screen.vault_room

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.SocketManager
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
import com.tech.young.data.model.ReceivedSocketComment
import com.tech.young.data.model.VaultDetailApiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentVaultRoomBinding
import com.tech.young.databinding.ItemLayoutCommentViewBinding
import com.tech.young.databinding.ItemLayoutMemberViewBinding
import com.tech.young.ui.ecosystem.EcosystemFragment
import com.tech.young.ui.exchange.ExchangeFragment
import com.tech.young.ui.home.HomeActivity
import com.tech.young.ui.user_profile.UserProfileFragment
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import org.json.JSONObject

@AndroidEntryPoint
class VaultRoomFragment : BaseFragment<FragmentVaultRoomBinding>() {

    private lateinit var membersAdapter: SimpleRecyclerViewAdapter<VaultDetailApiResponse.Data.Vault.Member, ItemLayoutMemberViewBinding>
    private lateinit var commentAdapter: SimpleRecyclerViewAdapter<GetCommentApiResponse.Data.Comment, ItemLayoutCommentViewBinding>
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>
    private var vaultId : String ? = null
    private lateinit var handler: Handler
    private lateinit var mSocket: Socket
    private var chatId : String ? = null
    private var getList = listOf(
        "", "", "", "", ""
    )
    private var id : String ? = null
    private var name : String ? = null

    private val viewModel: VaultRoomVM by viewModels()
    override fun onCreateView(view: View) {
        getVaultData()
        socketHandler()
        joinVault()

        receivedMessage()
        viewModel.getAds(Constants.GET_ADS)
        getCommentData()
        // click
        initOnClick()
        // adapter
        initAdapter()

        setObserver()


        binding.tabLayoutBottom.tabExchange.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, ExchangeFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.tabLayoutBottom.tabEcosystem.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, EcosystemFragment())
                .addToBackStack(null)
                .commit()
        }


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


        ViewCompat.setOnApplyWindowInsetsListener(binding.nestedScrollView) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            // Only adjust padding if keyboard is visible
            view.setPadding(0, 0, 0, imeHeight)
            insets
        }
    }

    private fun receivedMessage() {
        mSocket.on("vaultMessage") { data ->
            if (data.isNotEmpty() && data[0] is JSONObject) {
                val jsonData = data[0] as JSONObject
                Log.i("SocketHandler", "📩 Received new vault message: $jsonData")

                try {
                    val gson = Gson()
                    val socketComment = gson.fromJson(
                        jsonData.toString(),
                        ReceivedSocketComment::class.java
                    )

                    val apiComment = GetCommentApiResponse.Data.Comment(
                        _id = socketComment._id,
                        comment = socketComment.comment,
                        createdAt = socketComment.createdAt,
                        isLiked = false,
                        likesCount = 0,
                        type = socketComment.type,
                        vaultId = socketComment.vaultId,
                        userId = socketComment.userId?.let {
                            GetCommentApiResponse.Data.Comment.UserId(
                                _id = it._id,
                                firstName = it.firstName,
                                lastName = it.lastName,
                                profileImage = it.profileImage,
                                role = it.role,
                                username = it.username
                            )
                        }
                    )

                    Handler(Looper.getMainLooper()).post {
                        val newList = commentAdapter.list?.toMutableList() ?: mutableListOf()
                        newList.add(0, apiComment)
                      //  newList.add(apiComment)
                        commentAdapter.list = newList
                        commentAdapter.notifyItemInserted(0)


                    //    binding.rvComments.scrollToPosition(newList.size - 1)
                    }
                } catch (e: Exception) {
                    Log.e("SocketHandler", "❌ Failed to parse vaultMessage: ${e.message}", e)
                }
            } else {
                Log.w("SocketHandler", "⚠️ vaultMessage event received with empty or invalid data")
            }
        }
    }



    fun joinVault() {
            if (vaultId != null){
                val params = JSONObject().apply {
                    put("vaultId", vaultId)
                }
                // Emit the joinVault event
                mSocket.emit("joinVault", params)
                Log.i("SocketHandler", "Emitted joinRoom event: $params")

            }
            else {
                Log.i("SocketHandler", "joinVault: failed to join")

            }

        }


    private fun socketHandler() {
        val token = sharedPrefManager.getLoginData()?.token
        Log.i("dasdasd", "socketHandler: $token")
        try {
            if (!token.isNullOrEmpty()) {
                SocketManager.setSocket(token)
                SocketManager.establishConnection()
                mSocket = SocketManager.getSocket()!!

                mSocket?.on(Socket.EVENT_CONNECT) {
                    if (!isAdded) {
                        Log.w("VaultFragment", "⚠️ Fragment not attached, skipping joinVault")
                        return@on
                    }

                    activity?.runOnUiThread {
                        Log.i("VaultFragment", "✅ Socket connected/reconnected")

                        vaultId?.let {
                            joinVault()
                        }
                    }
                }



                mSocket?.on(Socket.EVENT_DISCONNECT) {
                    if (!isAdded) {
                        Log.w("VaultFragment", "⚠️ Fragment not attached, skipping disconnect UI update")
                        return@on
                    }

                    activity?.runOnUiThread {
                        Log.w("VaultFragment", "⚠️ Socket disconnected")
                    }
                }

            } else {
                Log.e("VaultFragment", "Token missing. Cannot connect socket.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
                                var userId = sharedPrefManager.getUserId()
                                binding.bean = myDataModel.data
                                chatId = myDataModel.data?.vault?.chatId

                                if (myDataModel.data!!.vault?.admin != null){
                                    id = myDataModel.data!!.vault?.admin!!._id
                                    name = myDataModel.data!!.vault?.admin?.firstName + " " + myDataModel.data!!.vault?.admin?.lastName

                                }
                                if (myDataModel.data?.vault?.admin?._id.equals(userId)){
                                    binding.tvJoin.visibility = View.GONE
                                    binding.etChat.visibility = View.VISIBLE
                                    binding.rvComments.visibility = View.VISIBLE
                                    binding.ivSendChat.visibility = View.VISIBLE
                                }else{
                                    binding.tvJoin.visibility = View.VISIBLE
                                    binding.etChat.visibility = View.GONE
                                    binding.rvComments.visibility = View.GONE
                                    binding.ivSendChat.visibility = View.GONE


                                    val isMember = myDataModel.data?.vault?.isMember == true
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


                                // Set members list
                                myDataModel?.data?.vault?.members?.let {
                                    membersAdapter.list = it
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
                    if (binding.tvJoin.text.equals("Leave")){
                        if (vaultId != null){
                            val params = JSONObject().apply {
                                put("vaultId", vaultId)
                            }
                            // Emit the joinVault event
                            mSocket.emit("leaveVault", params)
                            Log.i("SocketHandler", "Emitted joinRoom event: $params")

                        }
                    }
                    viewModel.joinLeaveRoom(Constants.JOIN_LEAVE_VAULT + vaultId)

                }

                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                R.id.ivSendChat -> {
                    if (TextUtils.isEmpty(binding.etChat.text.toString().trim())) {
                        showToast("Please enter message")
                    } else {
                        val params = JSONObject().apply {
                            put("chatId", chatId ?: "")
                            put("id", vaultId.toString())
                            put("type", "vault")
                            put("comment", binding.etChat.text.toString().trim())
                        }

                        if (mSocket.connected()) {
                            Log.i("SocketHandler", "📤 Emitting messageInVault: $params")
                            mSocket.emit("messageInVault", params)
                            Log.i("SocketHandler", "✅ messageInVault event emitted")
                        } else {
                            Log.e("SocketHandler", "❌ Socket not connected. Message not sent.")
                        }

                        // clear input after sending
                        binding.etChat.setText("")
                    }

                }

                R.id.profileImage, R.id.tvUserName ->{
                    val bundle = Bundle().apply {
                        putString("from", "user_profile")
                        putString("userId", id) // assuming m._id is a String
                    }
//                    val name = m.firstName + " " + m.lastName  // ← add space here
//                    val name  = m?.userId?.firstName + " " + m?.userId?.lastName
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

                    R.id.profileImage , R.id.tvUserName ->{
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
        binding.rvComments.adapter = commentAdapter


        membersAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_member_view, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.consMain ->{
                        val bundle = Bundle().apply {
                            putString("from", "user_profile")
                            putString("userId", m._id) // assuming m._id is a String
                        }
//                    val name = m.firstName + " " + m.lastName  // ← add space here
                        val name  = m.username
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
        binding.rvMembers.adapter = membersAdapter
    }


        override fun onDestroyView() {
            super.onDestroyView()
            mSocket.off(Socket.EVENT_CONNECT)
            mSocket.off(Socket.EVENT_DISCONNECT)
            mSocket.off("vaultMessage")
        }
}
