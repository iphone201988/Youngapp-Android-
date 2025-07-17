package com.tech.young.ui.signup_process

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.BindingUtils.setNavigationBarStyle
import com.tech.young.base.utils.showToast
import com.tech.young.data.DiditItems
import com.tech.young.databinding.FragmentAddLicenseBinding
import com.tech.young.databinding.ItemLayoutDeditBinding
import com.tech.young.ui.auth.AuthCommonVM
import com.tech.young.utils.DiditService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class AddLicenseFragment : BaseFragment<FragmentAddLicenseBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    private var imageUri : Uri?= null
    private lateinit var diditAdapter : SimpleRecyclerViewAdapter<DiditItems, ItemLayoutDeditBinding>
    private var deditList = arrayListOf<DiditItems>()
    override fun onCreateView(view: View) {
        initView()
        getDiditList()
        initOnClick()
        initAdapter()
    }

    private fun getDiditList() {
        deditList.add(DiditItems("Instant verification in under 2 minutes"))
        deditList.add(DiditItems("End-to-end encrypted data protection"))
        deditList.add(DiditItems("Mobile-optimized verification flow"))
        deditList.add(DiditItems("No data stored after verification"))
    }

    private fun initAdapter() {
        diditAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_dedit,BR.bean){v,m,pos ->

        }
        binding.rvPoints.adapter =diditAdapter
        diditAdapter.list = deditList
        diditAdapter.notifyDataSetChanged()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_add_license
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
    }

    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity(), Observer {
            when (it?.id) {
                R.id.ivBack -> {
                    findNavController().popBackStack()
                }

                R.id.tvContinueToDidit -> {
                       userVerification()
                }
                R.id.tvCancel ->{
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }


            }
        })

    }

    private fun userVerification() {
        // Show loading (optional)
        showLoading()
        Log.d("Didit", "Verification process started")

        // Grab vendor/user ID
        val vendorId = getLoggedInUserIdOrFallback()
        Log.d("Didit", "Using vendor ID: $vendorId")

        // Launch coroutine
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d("Didit", "Calling createVerificationSession API")

                val response: Map<String, Any> = withContext(Dispatchers.IO) {
                    DiditService.createVerificationSession(vendorId)
                }

                Log.d("Didit", "API response: $response")

                val url = (response["url"] as? String).orEmpty()
                Log.d("Didit", "Extracted URL: $url")

                if (url.isBlank()) {
                    Log.d("Didit", "Verification URL missing!")
                    showToast("Verification URL missing!")
                    return@launch
                }


                // Log successful retrieval
                Log.d("Didit", "Opening verification URL: $url")

                if (url != null){
                    val bundle = Bundle().apply {
                        putString("url", url)
                    }
                    findNavController().navigate(R.id.navigateToAddLicenseCameraFragment, bundle)
                }
                // openVerificationPage(url)

            } catch (t: Throwable) {
                Log.e("Didit", "Error during verification session creation: ${t.message}", t)
                showToast("Verification failed: ${t.localizedMessage ?: "Unknown error"}")
            } finally {
                hideLoading()
                Log.d("Didit", "Verification process finished")
            }
        }
    }


    private fun getLoggedInUserIdOrFallback(): String {
        // Replace with your real user session / SharedPreferences / DataStore fetch.
        // The Swift code used UserDefaults.standard[.loggedUserDetails]?._id ?? "123"
        return sharedPrefManager.getUserId()?: "123"
    }


}