package com.tech.young.ui.signup_process

import android.os.Bundle
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.firebase.encoders.ObjectEncoder
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.BindingUtils.setNavigationBarStyle
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.api.Constants
import com.tech.young.data.model.DigitApiResponse
import com.tech.young.databinding.FragmentAddLicenseCameraBinding
import com.tech.young.ui.auth.AuthCommonVM
import com.tech.young.ui.consumer_stream.ConsumerStreamActiivty
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.ignoreIoExceptions

@AndroidEntryPoint
class AddLicenseCameraFragment : BaseFragment<FragmentAddLicenseCameraBinding>() {
    private val viewModel: AuthCommonVM by viewModels()

    private var userId : String ? = null
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
        initObserver()
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
                        "getDigit" -> {
                            val myDataModel: DigitApiResponse? = BindingUtils.parseJson(it.data.toString())

                            myDataModel?.data?.let { data ->
                                val isVerified = data.isDocumentVerified
                                if (!isVerified.isNullOrEmpty()) {
                                    if (isVerified.contains("reject", ignoreCase = true)) {
                                        findNavController().popBackStack()
                                    } else {
                                        findNavController().navigate(R.id.navigateToAddYourPicFragment)
                                    }
                                } else {
                                    findNavController().popBackStack()
                                }
                            } ?: findNavController().popBackStack()
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
        return R.layout.fragment_add_license_camera
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }


    private fun initView() {
        BindingUtils.statusBarStyleBlack(requireActivity())
        BindingUtils.styleSystemBars(requireActivity(), requireActivity().getColor(R.color.white))

        setNavigationBarStyle(
            activity = requireActivity(),
            navigationBarColorResId = R.color.white,
            isLightIcons = true
        )


        userId  = sharedPrefManager.getUserId()
        setupWebView()

    }
    private fun setupWebView() {
        val url = arguments?.getString("url")

        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
        }

        binding.webView.webViewClient = WebViewClient()
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                request.grant(request.resources)
            }
        }

        url?.let { binding.webView.loadUrl(it) } ?: showToast("Invalid URL")
    }

    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity(), Observer {
            when (it?.id) {
                R.id.ivBack -> {
                    if (userId !=  null){
                        val data = HashMap<String,String>()
                        data["userId"] = userId.toString()
                        viewModel.getDigit(Constants.UN_AUTH,data)
                    }
                }

                R.id.tvVerify -> {
                    val bundle = Bundle().apply {
                        putString("from", "SignUpProcess")
                    }
                }

            }
        })

    }

}