package com.tech.young.ui.signup_process

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showErrorToast
import com.tech.young.base.utils.showSuccessToast
import com.tech.young.data.api.Constants
import com.tech.young.data.model.RegistrationCompleted
import com.tech.young.databinding.FragmentAddPaymentBinding
import com.tech.young.ui.auth.AuthCommonVM
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class AddPaymentFragment : BaseFragment<FragmentAddPaymentBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
        initObserver()
    }

    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    hideLoading()
                    val result: RegistrationCompleted? = BindingUtils.parseJson(it.data.toString())
                    if (result != null) {
                        showSuccessToast(result.message.toString())
                        val bundle = Bundle()
                        bundle.putString("Form", "AddPayment")
                        bundle.putString("qrCodeUrl", result.data?.qrCodeUrl.toString())
                        bundle.putString("secret", result.data?.secret.toString())
                        findNavController().navigate(R.id.navigateToPasswordChangedFragment, bundle)
                    }

                }

                Status.ERROR -> {
                    hideLoading()
                    showErrorToast(it.message.toString())
                }

                else -> {}
            }
        })
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_add_payment
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity(), Observer {
            when (it?.id) {
                R.id.ivBack -> {
                    findNavController().popBackStack()
                }

                R.id.tvAdd ,R.id.tvSkip-> {


                    val request = HashMap<String, RequestBody>()
                    fun String.toBody(): RequestBody =
                        this.toRequestBody("text/plain".toMediaTypeOrNull())

                    fun add(key: String, value: String?) {
                        if (!value.isNullOrBlank()) {
                            request[key] = value.toBody()
                            Log.d("RequestMap", "$key = $value")
                        }
                    }

                    add("userId", RegistrationDataHolder.userId)
                    add("role", RegistrationDataHolder.role)
                    add("crdNumber", RegistrationDataHolder.crdNumber)

                    add("age", RegistrationDataHolder.age)
                    add("gender", RegistrationDataHolder.gender)
                    add("maritalStatus", RegistrationDataHolder.maritalStatus)
                    add("children", RegistrationDataHolder.children)
                    add("homeOwnerShip", RegistrationDataHolder.homeOwnership)

                    add("objective", RegistrationDataHolder.objective)
                    add("financialExperience", RegistrationDataHolder.financialExperience)
                    add("investments", RegistrationDataHolder.investments)
                    add("servicesInterested", RegistrationDataHolder.servicesInterested)

                    add("productsOffered", RegistrationDataHolder.productsServicesOffered)
                    add("areaOfExpertise", RegistrationDataHolder.areasOfExpertise)

                    add("industry", RegistrationDataHolder.industry)
                    add("interestedIn", RegistrationDataHolder.interestedIn)

                    add("packageName", "standard")

                    viewModel.completeRegistration(
                        request,
                        Constants.COMPLETE_REGISTRATION,
                        RegistrationDataHolder.profileImage
                    )



//                    val bundle = Bundle()
//                    bundle.putString("Form", "AddPayment")
//                    findNavController().navigate(R.id.navigateToPasswordChangedFragment, bundle)
                }

//                R.id.tvSkip -> {
////                    val bundle = Bundle()
////                    bundle.putString("Form", "AddPayment")
////                    findNavController().navigate(R.id.navigateToPasswordChangedFragment, bundle)
//                }

            }
        })

    }

    private fun initView() {

    }
}
