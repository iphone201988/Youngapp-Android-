package com.tech.young.ui.signup_process

import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BaseCustomBottomSheet
import com.tech.young.base.utils.showToast
import com.tech.young.data.model.DummyLists
import com.tech.young.databinding.CommonBottomLayoutBinding
import com.tech.young.databinding.FragmentSelectYourInterestBinding
import com.tech.young.databinding.ItemLayoutAgeBinding
import com.tech.young.ui.auth.AuthCommonVM
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectYourInterestFragment : BaseFragment<FragmentSelectYourInterestBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
        editCategoryBottomSheet()
        Log.i("ewgweg", "initOnClick: "+ RegistrationDataHolder.industry)
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_select_your_interest
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

                /* R.id.tvNext -> {
                    // findNavController().navigate(R.id.navigateToSelectYourAccountPackageFragment)
                     findNavController().navigate(R.id.navigateToTermAndConditionsFragment)
                 }*/

                R.id.tvNext -> {
                    val objective = binding.edtObjective.text.toString().trim()
                    val financialExperience = binding.edtFinancialExperience.text.toString().trim()
                    val investments = binding.edtInvestments.text.toString().trim()
                    val servicesInterested = binding.edtServicesInterested.text.toString().trim()

                    when {
                        objective.isEmpty() -> {
                            showToast("Please select your objective")
                        }

                        financialExperience.isEmpty() -> {
                            showToast("Please select your financial experience")
                        }

                        investments.isEmpty() -> {
                            showToast("Please select your investments")
                        }

                        servicesInterested.isEmpty() -> {
                            showToast("Please select services you're interested in")
                        }

                        else -> {
                            // All fields are valid — update ViewModel or proceed
                            RegistrationDataHolder.objective = objective
                            RegistrationDataHolder.financialExperience = financialExperience
                            RegistrationDataHolder.investments = investments
                            RegistrationDataHolder.servicesInterested = servicesInterested

                            // Navigate to next screen
                            findNavController().navigate(R.id.navigateToTermAndConditionsFragment)
                        }
                    }
                }


                R.id.ivDropObjective -> {
                    initAdapter("Objective")
                    bottomSheetCommon.show()
                }

                R.id.ivDropFinancialExperience -> {
                    initAdapter("FinancialExperience")
                    bottomSheetCommon.show()
                }

                R.id.ivDropInvestments -> {
                    initAdapter("Investments")
                    bottomSheetCommon.show()
                }

                R.id.ivDropServicesInterested -> {
                    initAdapter("ServicesInterested")
                    bottomSheetCommon.show()
                }


            }
        })

    }

    private fun initView() {

    }

    private lateinit var bottomSheetCommon: BaseCustomBottomSheet<CommonBottomLayoutBinding>
    private fun editCategoryBottomSheet() {
        bottomSheetCommon =
            BaseCustomBottomSheet(requireContext(), R.layout.common_bottom_layout) {}
        bottomSheetCommon.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetCommon.behavior.isDraggable = true
        bottomSheetCommon.setCancelable(true)
        bottomSheetCommon.create()


    }

    private lateinit var adapter: SimpleRecyclerViewAdapter<String, ItemLayoutAgeBinding>
    private fun initAdapter(from: String) {
        val fieldMap = mapOf(
            "Objective" to binding.edtObjective,
            "FinancialExperience" to binding.edtFinancialExperience,
            "Investments" to binding.edtInvestments,
            "ServicesInterested" to binding.edtServicesInterested

        )

        val listMap = mapOf(
            "Objective" to DummyLists.generalMemberObjective,
            "FinancialExperience" to DummyLists.generalMemberFinancialExperience,
            "Investments" to DummyLists.generalMemberInvestments,
            "ServicesInterested" to DummyLists.generalMemberServicesInterested

        )

        adapter = SimpleRecyclerViewAdapter(R.layout.item_layout_age, BR.bean) { view, value, _ ->
            if (view.id == R.id.consMain) {
                fieldMap[from]?.setText(value)
                bottomSheetCommon.dismiss()
            }
        }
        bottomSheetCommon.binding.rvCommonSelection.adapter = adapter
        adapter.list = listMap[from] ?: emptyList()
    }
}
