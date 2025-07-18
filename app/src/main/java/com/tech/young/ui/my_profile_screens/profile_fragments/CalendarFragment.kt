package com.tech.young.ui.my_profile_screens.profile_fragments

import android.app.Activity.RESULT_OK
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.DropDownData
import com.tech.young.data.api.Constants
import com.tech.young.data.model.GetEventsApiResponse
import com.tech.young.databinding.FragmentCalendarBinding
import com.tech.young.databinding.ItemLayoutDropDownBinding
import com.tech.young.databinding.ItemLayoutRemindersBinding
import com.tech.young.ui.my_profile_screens.YourProfileVM
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.util.FileUtil
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class CalendarFragment : BaseFragment<FragmentCalendarBinding>() {
    private val viewModel:YourProfileVM by viewModels()
    private lateinit var reminderAdapter : SimpleRecyclerViewAdapter<GetEventsApiResponse.Data.Event,ItemLayoutRemindersBinding>
    private lateinit var topicAdapter : SimpleRecyclerViewAdapter<DropDownData,ItemLayoutDropDownBinding>
    private var topicList = ArrayList<DropDownData>()
    private var imageUri : Uri ?= null
    private var userSelectedDate: Calendar? = null

    private var getList = listOf(
        "", "", "", "", ""
    )

    private lateinit var currentCalendar: Calendar
    override fun onCreateView(view: View) {
        // view
        initView()

        getEventsData()
        // click

        getTopicsList()

        initOnClick()

        initAdapter()
        // observer
        initObserver()

    }

    private fun getTopicsList() {
        topicList.add(DropDownData("Stocks"))
        topicList.add(DropDownData("Insurance"))
        topicList.add(DropDownData("Retirement"))
        topicList.add(DropDownData("Savings"))
        topicList.add(DropDownData("Investment Management"))
        topicList.add(DropDownData("Child Education"))
        topicList.add(DropDownData("Student Loan Management"))
        topicList.add(DropDownData("Debt Management"))
        topicList.add(DropDownData("Tax Planning"))
        topicList.add(DropDownData("Financial Planning"))
        topicList.add(DropDownData("Wealth Education"))
        topicList.add(DropDownData("Estate Planning"))
        topicList.add(DropDownData("Investor"))
        topicList.add(DropDownData("Venture Capitalist"))
        topicList.add(DropDownData("Small Business"))
        topicList.add(DropDownData("Grants"))
        topicList.add(DropDownData("Loans"))
        topicList.add(DropDownData("Insurance"))
        topicList.add(DropDownData("Annuities"))
    }

    private fun getEventsData() {
        val data =  HashMap<String,Any>()
        data["page"] = 1
        viewModel.getEvents(data, Constants.GET_EVENTS)
    }

    private fun initAdapter() {
        reminderAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_reminders,BR.bean){v,m,pos  ->

        }
        binding.rvReminder.adapter = reminderAdapter
        reminderAdapter.notifyDataSetChanged()

        topicAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down,BR.bean){v,m,pos ->
            when(v.id){
                R.id.consMain , R.id.title->{
                    binding.etTopic.setText(m.title)
                    binding.rvTopics.visibility = View.GONE
                }
            }
        }
        binding.rvTopics.adapter = topicAdapter
        topicAdapter.list = topicList
        topicAdapter.notifyDataSetChanged()

    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_calendar
    }

    override fun getViewModel(): BaseViewModel {
      return viewModel
    }

    /** handle view **/
    private fun initView() {
        // Step 1: Initialize with current date
        currentCalendar = Calendar.getInstance()

        // Set current month page
        binding.rangeCalenderOneTime.setDate(currentCalendar)


        binding.rangeCalenderOneTime.setOnPreviousPageChangeListener(object :
            OnCalendarPageChangeListener {
            override fun onChange() {
                currentCalendar = binding.rangeCalenderOneTime.currentPageDate
                updateMonthYearTexts()
            }
        })

        binding.rangeCalenderOneTime.setOnForwardPageChangeListener(object : OnCalendarPageChangeListener {
            override fun onChange() {
                currentCalendar = binding.rangeCalenderOneTime.currentPageDate
                updateMonthYearTexts()
            }



        })



        updateMonthYearTexts()

    }

    /** handle click **/
    private fun initOnClick() {

        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when(it?.id){
                R.id.tvLeftArrowYear ->{
                    currentCalendar.add(Calendar.YEAR, -1)
                    updateCalendar()
                }
                R.id.tvRightArrowYear ->{
                    currentCalendar.add(Calendar.YEAR, 1)
                    updateCalendar()
                }
                R.id.tvLeftArrowMonth ->{
                    currentCalendar.add(Calendar.MONTH, -1)
                    updateCalendar()
                }
                R.id.tvRightArrowMonth ->{
                    currentCalendar.add(Calendar.MONTH, 1)

                    updateCalendar()
                }
                R.id.addEvent ->{
                    binding.calendarCons.visibility = View.GONE
                    binding.consAddEvent.visibility = View.VISIBLE
                }
                R.id.tvSubmit ->{
                    binding.calendarCons.visibility = View.VISIBLE
                    binding.consAddEvent.visibility = View.GONE
                }
                R.id.etTopic ->{
                    binding.rvTopics.visibility = View.VISIBLE
                }
                R.id.etUploadFile ->{
                    ImagePicker.with(this)
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .createIntent { intent ->
                            startForImageResult.launch(intent)
                        }
                }
                R.id.tvSubmit ->{
                    if (isEmptyField()){
                        val multipartImage = imageUri?.let { convertImageToMultipart(it) }
                        val data = HashMap<String, RequestBody>()
                        data["title"] = binding.etTitle.text.toString().trim().toRequestBody()
                        data["topic"] = binding.etTopic.text.toString().trim().toRequestBody()
                        data["description"] = binding.etDescription.text.toString().trim().toRequestBody()
                        data["type"] = binding.etDescription.text.toString().trim().toRequestBody()
                        data["scheduledDate"]
                        viewModel.addEvent(data,Constants.CREATE_EVENT,multipartImage)
                    }
                }
            }
        })

    }
    private val startForImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        try {
            val resultCode = result.resultCode
            val data = result.data
            if (resultCode == RESULT_OK) {
                val fileUri = data?.data
                imageUri = fileUri

                //  Log.i("dasd", ": $imageUri")
            } else if (resultCode == ImagePicker.RESULT_ERROR) {

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    /** handle observer **/
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    showLoading()
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "getEvents" ->{
                            val myDataModel : GetEventsApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    if (myDataModel.data?.events != null){
                                        reminderAdapter.list = myDataModel.data?.events
                                    }
                                }
                            }
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

    private fun updateCalendar() {
        binding.rangeCalenderOneTime.setDate(currentCalendar)
        // Step 2: Log selected date if any
        val selectedDate = binding.rangeCalenderOneTime.selectedDate
        if (selectedDate != null) {
            Log.d("CalendarUpdate", "Selected date: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)}")
        } else {
            Log.d("CalendarUpdate", "No date is currently selected.")
        }
        updateMonthYearTexts()
    }

    private fun updateMonthYearTexts() {
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

        binding.tvMonth.text = monthFormat.format(currentCalendar.time)
        binding.tvYear.text = yearFormat.format(currentCalendar.time)
    }

    private fun isEmptyField() : Boolean {

        if (TextUtils.isEmpty(binding.etTitle.text.toString().trim())) {
            showToast("Please enter title")
            return false
        }
        if (TextUtils.isEmpty(binding.etTopic.text.toString().trim())) {
            showToast("Please select topics")
            return false

        }
        if (TextUtils.isEmpty(binding.etDescription.text.toString().trim())) {
            showToast("Please enter description")
            return false
        }
        return true
    }

    private fun convertImageToMultipart(imageUri: Uri): MultipartBody.Part {
        val file = FileUtil.getTempFile(requireContext(), imageUri)
        return MultipartBody.Part.createFormData(
            "file",
            file!!.name,
            file.asRequestBody("image/png".toMediaTypeOrNull())
        )
    }
    override fun onResume() {
        super.onResume()
        getEventsData()
    }
}