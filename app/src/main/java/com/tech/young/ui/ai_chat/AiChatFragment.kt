package com.tech.young.ui.ai_chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.AiChatModel
import com.tech.young.data.api.Constants
import com.tech.young.databinding.FragmentAiChatBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AiChatFragment : BaseFragment<FragmentAiChatBinding>() {

    private val viewModel : AiChatVm by viewModels()

    private lateinit var adapter: AiChatAdapter
    private lateinit var list: MutableList<AiChatModel>

    override fun getLayoutResource(): Int {
        return R.layout.fragment_ai_chat
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {


        initOnClick()
        initAdapter()
        observeData()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            view.setPadding(0, 0, 0, imeHeight)
            insets
        }
        
    }

    private fun observeData() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.SUCCESS -> {
                    adapter.removeTyping()
                    if (it.message == "addChat") {
                        val response = it.data
                        if (response?.get("success")?.asBoolean == true) {
                            val chatData = response.get("data")?.asString
                            if (!chatData.isNullOrEmpty()) {
                                adapter.addMessage(AiChatModel(chatData, false))
                                binding.rvChat.scrollToPosition(list.size - 1)
                            }
                        }
                    }
                }
                Status.ERROR -> {
                    adapter.removeTyping()
                    showToast(it.message ?: "Something went wrong")
                }
                Status.LOADING -> {
                    // We don't call showProgress() here anymore because we use the typing animation
                }
                else -> {}
            }
        }
    }

    private fun initAdapter() {
        list = mutableListOf()
        adapter = AiChatAdapter(list)
        binding.rvChat.adapter = adapter
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner){
            when(it?.id){
                R.id.ivSend ->{

                    binding.ivLogo.visibility = View.GONE
                    binding.tvDes.visibility = View.GONE
                    binding.rvChat.visibility = View.VISIBLE
                    val msg = binding.etChat.text.toString()

                    if (msg.isNotEmpty()) {
                        val  data = HashMap<String,  Any>()
                        data["q"] = msg

                        viewModel.addChat(Constants.AI_CHAT,data)

                        adapter.addMessage(AiChatModel(msg, true))
                        binding.etChat.text?.clear()

                        // Add typing animation
                        adapter.addMessage(AiChatModel("typing...", false))
                        
                        binding.rvChat.scrollToPosition(list.size - 1)
                    }
                }
            }
        }
    }

}
