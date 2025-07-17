package com.tech.young.ui.my_profile_screens.common_ui

import android.view.View
import androidx.fragment.app.viewModels
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.databinding.FragmentFormUploadBinding
import com.tech.young.databinding.YourPhotosItemViewBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FormUploadFragment : BaseFragment<FragmentFormUploadBinding>() {
    private val viewModel: YourProfileVM by viewModels()
    // adapter
    private lateinit var yourImageAdapter: SimpleRecyclerViewAdapter<String, YourPhotosItemViewBinding>
    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_form_upload
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView(){
        // adapter
        initAdapter()

    }

    /** handle click **/
    private fun initOnClick(){
        viewModel.onClick.observe(requireActivity()){
            when(it?.id){
                R.id.ivBack->{
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                R.id.actionToggleBtn->{
                    // handle click
                }
                R.id.tvSaveAdditional->{
                    // handle click
                }
            }
        }
    }

    /** handle observer **/
    private fun initObserver(){

    }

    /** handle adapter **/
    private fun initAdapter() {
        yourImageAdapter = SimpleRecyclerViewAdapter(
            R.layout.your_photos_item_view, BR.bean
        ) { v, m, pos ->
            when (v.id) {

            }
        }
        yourImageAdapter.list = getList
        binding.rvYourPhotos.adapter = yourImageAdapter
    }

    private var getList = listOf(
        "", "", "", "", ""
    )

}