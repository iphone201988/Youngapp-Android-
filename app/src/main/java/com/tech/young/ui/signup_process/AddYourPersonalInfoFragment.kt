package com.tech.young.ui.signup_process

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
import com.tech.young.data.api.Constants
import com.tech.young.data.model.DummyLists
import com.tech.young.databinding.CommonBottomLayoutBinding
import com.tech.young.databinding.FragmentAddYourPersonalInfoBinding
import com.tech.young.databinding.ItemLayoutAgeBinding
import com.tech.young.ui.auth.AuthCommonVM
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddYourPersonalInfoFragment : BaseFragment<FragmentAddYourPersonalInfoBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
        editCategoryBottomSheet()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_add_your_personal_info
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity(), Observer {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                R.id.tvNext -> {
                    val age = binding.edtAge.text.toString().trim()
                    val gender = binding.edtGender.text.toString().trim()
                    val marital = binding.edtMarital.text.toString().trim()
                    val children = binding.edtChildren.text.toString().trim()
                    val homeOwnership = binding.edtHomeOwnership.text.toString().trim()

                    when {
                        age.isEmpty() -> showToast("Please enter your age")
                        gender.isEmpty() -> showToast("Please select your gender")
                        marital.isEmpty() -> showToast("Please select your marital status")
                        children.isEmpty() -> showToast("Please enter number of children")
                        homeOwnership.isEmpty() -> showToast("Please select home ownership")
                        else -> {
                            RegistrationDataHolder.age = age
                            RegistrationDataHolder.gender = gender
                            RegistrationDataHolder.maritalStatus = marital
                            RegistrationDataHolder.children = children
                            RegistrationDataHolder.homeOwnership = homeOwnership

                            findNavController().navigate(R.id.navigateToSelectYourInterestFragment)
                        }
                    }
                    //
                }

                R.id.ivDropAge -> {
                    initAdapter("Age")
                    bottomSheetCommon.show()

                }

                R.id.ivDropGender -> {
                    initAdapter("Gender")
                    bottomSheetCommon.show()


                }

                R.id.ivDropMarital -> {
                    initAdapter("Marital")
                    bottomSheetCommon.show()
                }

                R.id.ivDropChildren -> {
                    initAdapter("Children")
                    bottomSheetCommon.show()
                }

                R.id.ivDropHomeOwnership -> {
                    initAdapter("HomeOwnership")
                    bottomSheetCommon.show()
                }

                R.id.ivDropProductsServicesOffered -> {
                    initAdapter("ProductsServicesOffered")
                    bottomSheetCommon.show()
                }

                R.id.ivDropAreasOfExpertise -> {
                    initAdapter("AreasOfExpertise")
                    bottomSheetCommon.show()
                }

                R.id.ivDropIndustry -> {
                    initAdapter("Industry")
                    bottomSheetCommon.show()
                }

                R.id.ivDropInterestedIn -> {
                    initAdapter("InterestedIn")
                    bottomSheetCommon.show()
                }

                R.id.ivDropVCIndustryInterestedIn -> {
                    initAdapter("VCIndustryInterestedIn")
                    bottomSheetCommon.show()
                }

                R.id.ivDropVCAreasExpertise -> {
                    initAdapter("VCAreasExpertise")
                    bottomSheetCommon.show()
                }

                R.id.tvNextFinancialAdvisor, R.id.consVCAccountRegistration -> {
                    val productsServicesOffered =
                        binding.edtProductsServicesOffered.text.toString().trim()
                    val areasOfExpertise = binding.edtAreasOfExpertise.text.toString().trim()

                    when {
                        productsServicesOffered.isEmpty() -> {
                            showToast("Please enter products/services offered")
                        }

                        areasOfExpertise.isEmpty() -> {
                            showToast("Please enter areas of expertise")
                        }

                        else -> {
                            RegistrationDataHolder.productsServicesOffered = productsServicesOffered
                            RegistrationDataHolder.areasOfExpertise = areasOfExpertise
                            findNavController().navigate(R.id.navigateToSelectYourInterestFragment)
                            //  findNavController().navigate(R.id.navigateToSelectYourAccountPackageFragment)
                        }
                    }
                }

                R.id.tvNextStartupSmall -> {
                    val industry = binding.edtIndustry.text.toString().trim()
                    val interestedIn = binding.edtInterestedIn.text.toString().trim()
                    when {
                        industry.isEmpty() -> {
                            showToast("Please enter your industry")
                        }

                        interestedIn.isEmpty() -> {
                            showToast("Please enter what you're interested in")
                        }

                        else -> {
                            RegistrationDataHolder.industry = industry
                            RegistrationDataHolder.interestedIn = interestedIn
                            findNavController().navigate(R.id.navigateToSelectYourInterestFragment)
                        }
                    }
                }

            }
        })

    }

    private fun initView() {
        when (Constants.chooseAccountType) {
            "General Member" -> {
                binding.consGeneralMember.visibility = View.VISIBLE
                binding.consFinancialAdvisorFirm.visibility = View.GONE
                binding.consStartupSmall.visibility = View.GONE
                binding.consVCAccountRegistration.visibility = View.GONE
            }

            "Financial Advisor", "Financial Firm" -> {
                binding.consGeneralMember.visibility = View.GONE
                binding.consFinancialAdvisorFirm.visibility = View.VISIBLE
                binding.consStartupSmall.visibility = View.GONE
                binding.consVCAccountRegistration.visibility = View.GONE

            }

            "Investor/ VC" -> {
                binding.consGeneralMember.visibility = View.GONE
                binding.consFinancialAdvisorFirm.visibility = View.GONE
                binding.consStartupSmall.visibility = View.GONE
                binding.consVCAccountRegistration.visibility = View.VISIBLE

            }

        }
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
            "Age" to binding.edtAge,
            "Gender" to binding.edtGender,
            "Marital" to binding.edtMarital,
            "Children" to binding.edtChildren,
            "HomeOwnership" to binding.edtHomeOwnership,
            "ProductsServicesOffered" to binding.edtProductsServicesOffered,
            "AreasOfExpertise" to binding.edtAreasOfExpertise,
            "Industry" to binding.edtIndustry,
            "InterestedIn" to binding.edtInterestedIn,
            "VCIndustryInterestedIn" to binding.edtIndustryInterestedIn,
            "VCAreasExpertise" to binding.edtAreasExpertise
        )

        val listMap = mapOf(
            "Age" to DummyLists.ageRanges,
            "Gender" to DummyLists.genders,
            "Marital" to DummyLists.martialStatus,
            "Children" to DummyLists.children,
            "HomeOwnership" to DummyLists.homeOwnershipStatus,
            "ProductsServicesOffered" to DummyLists.productsOrServicesOffered,
            "AreasOfExpertise" to DummyLists.financialAdvisorAreasOfExpertise,
            "Industry" to DummyLists.startupAndSmallBusinessIndustry,
            "InterestedIn" to DummyLists.startupAndSmallBusinessInterestedIn,
            "VCIndustryInterestedIn" to DummyLists.investorVCIndustryInterestedIn,
            "VCAreasExpertise" to DummyLists.investorVCAreasOfExpertise
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
