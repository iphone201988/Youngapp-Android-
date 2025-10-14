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
import com.tech.young.data.SelectionItem
import com.tech.young.databinding.BottomSheetMultipleSelectionBinding
import com.tech.young.databinding.ItemLayoutMultipleSelectionBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectYourInterestFragment : BaseFragment<FragmentSelectYourInterestBinding>() {
    private val viewModel: AuthCommonVM by viewModels()

    private val selectedItems = mutableSetOf<String>()



    override fun onCreateView(view: View) {
        initView()
        initOnClick()
        editCategoryBottomSheet()
        Log.i("ewgweg", "initOnClick: " + RegistrationDataHolder.industry)
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
                    bottomSheetMultipleSelection.show()
                }

                R.id.ivDropServicesInterested -> {
                    initAdapter("ServicesInterested")
                    bottomSheetMultipleSelection.show()
                }


            }
        })

    }

    private fun initView() {

    }

    private lateinit var bottomSheetCommon: BaseCustomBottomSheet<CommonBottomLayoutBinding>
    private lateinit var bottomSheetMultipleSelection: BaseCustomBottomSheet<BottomSheetMultipleSelectionBinding>
    private fun editCategoryBottomSheet() {
        bottomSheetCommon =
            BaseCustomBottomSheet(requireContext(), R.layout.common_bottom_layout) {}
        bottomSheetCommon.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetCommon.behavior.isDraggable = true
        bottomSheetCommon.setCancelable(true)
        bottomSheetCommon.create()


        bottomSheetMultipleSelection =
            BaseCustomBottomSheet(requireContext(), R.layout.bottom_sheet_multiple_selection) {}
        bottomSheetMultipleSelection.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetMultipleSelection.behavior.isDraggable = true
        bottomSheetMultipleSelection.setCancelable(true)
        bottomSheetMultipleSelection.create()

    }

    private lateinit var adapter: SimpleRecyclerViewAdapter<String, ItemLayoutAgeBinding>
    private lateinit var multiSelectAdapter: SimpleRecyclerViewAdapter<SelectionItem, ItemLayoutMultipleSelectionBinding>
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
            "Investments" to DummyLists.generalMemberInvestments.map { SelectionItem(it) }, // List<SelectionItem>
            "ServicesInterested" to DummyLists.generalMemberServicesInterested.map {
                SelectionItem(
                    it
                )
            } // List<SelectionItem>

        )

        when (from) {
            "Objective", "FinancialExperience" -> {
                adapter =
                    SimpleRecyclerViewAdapter(R.layout.item_layout_age, BR.bean) { view, value, _ ->
                        if (view.id == R.id.consMain) {
                            fieldMap[from]?.setText(value as String)
                            bottomSheetCommon.dismiss()
                        }
                    }
                adapter.list = listMap[from] as List<String>
                bottomSheetCommon.binding.rvCommonSelection.adapter = adapter
            }

            "Investments", "ServicesInterested" -> {
                multiSelectAdapter = SimpleRecyclerViewAdapter(
                    R.layout.item_layout_multiple_selection,
                    BR.bean
                ) { view, value, pos ->
                    if (view.id == R.id.consMain) {
                        val item = value as SelectionItem
                        item.isSelected = !item.isSelected

                        val selected = (multiSelectAdapter.list as List<SelectionItem>)
                            .filter { it.isSelected }
                            .joinToString(", ") { it.name }

                        fieldMap[from]?.setText(selected)
                        multiSelectAdapter.notifyItemChanged(pos)
                    }
                }
                multiSelectAdapter.list = listMap[from] as List<SelectionItem>
                bottomSheetMultipleSelection.binding.rvCommonSelection.adapter = multiSelectAdapter
            }
        }
    }
}
