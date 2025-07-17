package com.tech.young.ui.signup_process

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.BindingUtils.setNavigationBarStyle
import com.tech.young.data.api.Constants
import com.tech.young.data.model.ChooseAccountType
import com.tech.young.databinding.FragmentSignupMainBinding
import com.tech.young.databinding.HolderChooseAccountBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignupMainFragment : BaseFragment<FragmentSignupMainBinding>() {
    private val viewModel: SignUpVm by viewModels()
    private var accountType: String? = null
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_signup_main
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

        initAdapter()
    }

    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity(), Observer {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                R.id.tvNext -> {
                    val bundle = Bundle().apply {
                        putString("accountType", accountType ?: "General Member")
                    }
                    findNavController().navigate(R.id.navigateToEnterYourDetailsFragment, bundle)
                }


                R.id.tvLogin -> {
                    findNavController().navigate(R.id.navigateToLoginFragment)
                }
            }
        })

    }

    private lateinit var adapter: SimpleRecyclerViewAdapter<ChooseAccountType, HolderChooseAccountBinding>
    private fun initAdapter() {
        adapter =
            SimpleRecyclerViewAdapter(R.layout.holder_choose_account, BR.bean) { _v, m, pos_ ->
                when (_v.id) {
                    R.id.consMain -> {
                        adapter.list.forEach {
                            it.isSelected = false
                        }
                        m.isSelected = true
                        accountType = m.name
                        Constants.chooseAccountType = m.name
                        adapter.notifyDataSetChanged()
                    }
                }

            }
        binding.rvAccountType.adapter = adapter
        adapter.list = listChooseAccountType()
    }

    private fun listChooseAccountType(): ArrayList<ChooseAccountType> {
        val list = ArrayList<ChooseAccountType>()
        list.add(ChooseAccountType(true, "General Member"))
        list.add(ChooseAccountType(false, "Financial Advisor"))
        list.add(ChooseAccountType(false, "Financial Firm"))
        list.add(ChooseAccountType(false, "Small Business"))
        list.add(ChooseAccountType(false, "Startup"))
        list.add(ChooseAccountType(false, "Investor/ VC"))
        return list

    }

    override fun onResume() {
        super.onResume()
        accountType = "General Member"
        Constants.chooseAccountType = accountType.toString()
    }

}