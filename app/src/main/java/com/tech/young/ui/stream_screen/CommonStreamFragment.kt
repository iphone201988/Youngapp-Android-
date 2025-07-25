package com.tech.young.ui.stream_screen

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BaseCustomBottomSheet
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.DropDownData
import com.tech.young.data.api.Constants
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.data.model.StreamData
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.BotttomSheetTopicsBinding
import com.tech.young.databinding.FragmentCommonStreamBinding
import com.tech.young.databinding.ItemLayoutDropDownBinding
import com.tech.young.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class CommonStreamFragment : BaseFragment<FragmentCommonStreamBinding>() ,BaseCustomBottomSheet.Listener {

    private val viewModel: StreamVM by viewModels()
    private var topicList = ArrayList<DropDownData>()
    private lateinit var topicBottomSheet : BaseCustomBottomSheet<BotttomSheetTopicsBinding>
    private lateinit var topicAdapter : SimpleRecyclerViewAdapter<DropDownData, ItemLayoutDropDownBinding>
    private var imageUri : Uri?= null
    private var formattedApiDate : String ? = null
    private  var scheduleDateToSend: String? = null

    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>

    private var getList = listOf(
        "", "", "", "", ""
    )
    override fun onCreateView(view: View) {
        // view
        initView()


        // click
        initOnClick()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_common_stream
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }


    /****/
    private fun initView() {
        initBottomsheet()
        viewModel.getAds(Constants.GET_ADS)
        getTopicsList()
        setDefaultDateTime()
        // adapter
        initAdapter()
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
                        "getAds" ->{
                            val myDataModel : GetAdsAPiResponse? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    adsAdapter.list = myDataModel.data?.ads
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

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {
                R.id.ivBack->{
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                R.id.etTopic ->{
                    topicBottomSheet.show()
                }
                R.id.ivUploadImage ->{
                    ImagePicker.with(this)
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .createIntent { intent ->
                            startForImageResult.launch(intent)
                        }
                }
                R.id.etDate ->{
                    calendarOpen()
                }
                R.id.tvLaunch, R.id.tvSchedule -> {
                    if (isEmptyField()) {

                        val streamData = StreamData(
                            title = binding.etTitle.text.toString().trim(),
                            topic = binding.etTopic.text.toString().trim(),
                            scheduleDate = scheduleDateToSend,
                            image = imageUri,
                            description = binding.etDescription.text.toString().trim()
                        )

                        val intent = Intent(requireContext(), CommonActivity::class.java).apply {
                            putExtra("from", "stream_confirmation")
                            putExtra("stream_data", streamData)
                        }
                        startActivity(intent)
                    }
                }



            }
        }
    }


    private val startForImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        try {
            val resultCode = result.resultCode
            val data = result.data
            if (resultCode == Activity.RESULT_OK) {
                val fileUri = data?.data
                imageUri = fileUri
                binding.ivUploadImage.setImageURI(fileUri)

                //  Log.i("dasd", ": $imageUri")
            } else if (resultCode == ImagePicker.RESULT_ERROR) {

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** handle adapter **/
    private fun initAdapter() {
        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        binding.rvAds.adapter = adsAdapter


        topicAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_drop_down,BR.bean){v,m,pos ->
            when(v.id){
                R.id.consMain , R.id.title->{
                    binding.etTopic.setText(m.title)
                    topicBottomSheet.dismiss()
                }
            }
        }
        topicBottomSheet.binding.rvTopics.adapter = topicAdapter
        topicAdapter.list = topicList
        topicAdapter.notifyDataSetChanged()

    }

    private fun initBottomsheet() {
        topicBottomSheet = BaseCustomBottomSheet(requireContext(),R.layout.botttom_sheet_topics,this)

        topicBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        topicBottomSheet.behavior.isDraggable = true

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

    override fun onViewClick(view: View?) {

    }
//    private fun calendarOpen() {
//        val c = Calendar.getInstance()
//        val year = c.get(Calendar.YEAR)
//        val month = c.get(Calendar.MONTH)
//        val day = c.get(Calendar.DAY_OF_MONTH)
//
//        val datePickerDialog = DatePickerDialog(
//            requireContext(), R.style.DialogTheme,
//            { _, selectedYear, selectedMonth, selectedDay ->
//
//                // For display (local timezone)
//                val selectedCalendar = Calendar.getInstance()
//                selectedCalendar.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0)
//
//                val displayFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
//                val formattedDisplayDate = displayFormat.format(selectedCalendar.time)
//                binding.etDate.setText(formattedDisplayDate)
//
//                // For API (UTC midnight of selected day)
//                val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
//                utcCalendar.set(Calendar.YEAR, selectedYear)
//                utcCalendar.set(Calendar.MONTH, selectedMonth)
//                utcCalendar.set(Calendar.DAY_OF_MONTH, selectedDay)
//                utcCalendar.set(Calendar.HOUR_OF_DAY, 0)
//                utcCalendar.set(Calendar.MINUTE, 0)
//                utcCalendar.set(Calendar.SECOND, 0)
//                utcCalendar.set(Calendar.MILLISECOND, 0)
//
//                val apiFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
//                apiFormat.timeZone = TimeZone.getTimeZone("UTC")
//                formattedApiDate = apiFormat.format(utcCalendar.time)
//
//                Log.d("FormattedDates", "Display: $formattedDisplayDate, API: $formattedApiDate")
//            }
//            , year, month, day
//        )
//        // Disable previous dates
//        datePickerDialog.datePicker.minDate = c.timeInMillis
//        datePickerDialog.show()
//        val positiveColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
//        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(positiveColor)
//        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(positiveColor)
//    }



    private fun setDefaultDateTime() {
        val currentCalendar = Calendar.getInstance()

        // Format and set local time in EditText
        val localDateFormat = SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault())
        binding.etDate.setText(localDateFormat.format(currentCalendar.time))

        // Format and store UTC time for API
        val utcDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        formattedApiDate = utcDateFormat.format(currentCalendar.time)

        // Compare to current UTC time to determine if it's future (always true here)
        val currentUtcTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time
        val selectedDate = utcDateFormat.parse(formattedApiDate!!)

        scheduleDateToSend = if (selectedDate != null && selectedDate.after(currentUtcTime)) {
            formattedApiDate
        } else {
            null
        }

//        // Toggle visibility of buttons
//        if (scheduleDateToSend != null) {
//            binding.tvSchedule.visibility = View.VISIBLE
//            binding.tvLaunch.visibility = View.GONE
//        } else {
//            binding.tvSchedule.visibility = View.GONE
//            binding.tvLaunch.visibility = View.VISIBLE
//        }
    }

    private fun calendarOpen() {
        val currentCalendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(), R.style.DialogTheme,
            { _, year, month, dayOfMonth ->

                val timePickerDialog = TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->

                        val localCalendar = Calendar.getInstance().apply {
                            set(year, month, dayOfMonth, hourOfDay, minute, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        // Convert to UTC string for API
                        formattedApiDate = SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US
                        ).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }.format(localCalendar.time)

                        // Update UI with local time
                        binding.etDate.setText(
                            SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault()).format(localCalendar.time)
                        )

                        // Determine if selected time is future
                        val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }

                        val selectedDate = utcFormat.parse(formattedApiDate!!)
                        val currentUtcTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time

                        scheduleDateToSend = if (selectedDate != null && selectedDate.after(currentUtcTime)) {
                            formattedApiDate // valid future time
                        } else {
                            null // treat as immediate/live
                        }

                        // ✅ Toggle visibility of buttons
                        if (scheduleDateToSend != null) {
                            binding.tvSchedule.visibility = View.VISIBLE
                            binding.tvLaunch.visibility = View.GONE
                        } else {
                            binding.tvSchedule.visibility = View.GONE
                            binding.tvLaunch.visibility = View.VISIBLE
                        }

                    },
                    currentCalendar.get(Calendar.HOUR_OF_DAY),
                    currentCalendar.get(Calendar.MINUTE),
                    false
                )

                timePickerDialog.show()
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = currentCalendar.timeInMillis
        datePickerDialog.show()
    }





    private fun isEmptyField() : Boolean{
        if (TextUtils.isEmpty(binding.etTitle.text.toString().trim())){
            showToast("Please enter title")
            return false
        }
        if (TextUtils.isEmpty(binding.etTopic.text.toString().trim())){
            showToast("Please select topic")
            return false
        }
        if (TextUtils.isEmpty(binding.etDescription.text.toString().trim())){
            showToast("Please enter description")
        return false
        }
        if (TextUtils.isEmpty(binding.etDate.text.toString().trim())){
            showToast("Please select date")
            return false
        }
        return true
    }

}