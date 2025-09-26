package com.tech.young.ui.my_profile_screens.profile_fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.applandeo.materialcalendarview.utils.calendar
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
import com.tech.young.data.api.SimpleApiResponse
import com.tech.young.ui.ecosystem.EcosystemFragment
import com.tech.young.ui.exchange.ExchangeFragment
import com.tech.young.ui.share_screen.CommonShareFragment
import com.tech.young.ui.stream_screen.CommonStreamFragment
import com.tech.young.ui.vault_screen.CommonVaultFragment
import com.tech.young.utils.VerticalPagination
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class CalendarFragment : BaseFragment<FragmentCalendarBinding>() {
    private val viewModel:YourProfileVM by viewModels()
    private lateinit var reminderAdapter : SimpleRecyclerViewAdapter<GetEventsApiResponse.Data.Event,ItemLayoutRemindersBinding>
    private lateinit var topicAdapter : SimpleRecyclerViewAdapter<DropDownData,ItemLayoutDropDownBinding>
    private var topicList = ArrayList<DropDownData>()
    private var imageUri : Uri ?= null
    private var userSelectedDate: String? = null
    private var pagination: VerticalPagination? = null
    var eventsList = listOf<GetEventsApiResponse.Data.Event?>()

    private var page  = 1
    private var isLoading = false
    private var isLastPage = false
    private var totalPages : Int ? = null
    private var getList = listOf(
        "", "", "", "", ""
    )
    private var visibilityMode  = true


    private lateinit var currentCalendar: Calendar
    override fun onCreateView(view: View) {
        // view
        initView()

        //getEventsData()
        // click

        getTopicsList()

        initOnClick()

        initAdapter()
        // observer
        initObserver()


// Keyboard insets listener to avoid send button getting hidden
        ViewCompat.setOnApplyWindowInsetsListener(binding.nestedScrollView) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            // Only adjust padding if keyboard is visible
            view.setPadding(0, 0, 0, imeHeight)
            insets
        }
//       requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)






        binding.setPublic.setOnCheckedChangeListener { _, isChecked ->
            val visibilityMode = isChecked  // true if checked, false if not
            Log.d("SwitchValue", "Visibility mode: $visibilityMode")

            // Optionally store it somewhere
            // myViewModel.visibilityMode.value = visibilityMode
        }



        binding.etDescription.setOnTouchListener { v, event ->
            val parent = v.parent ?: return@setOnTouchListener false  // Safely accessing the parent
            parent.requestDisallowInterceptTouchEvent(true)
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_UP -> parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }


        binding.rvReminder.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // 👇 Only continue if scrolling down
                if (dy <= 0) return

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && page < totalPages!!) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3 &&
                        firstVisibleItemPosition >= 0
                    ) {
                        isLoading = true // ✅ Lock before load
                        loadMoreData()
                    }
                }
            }
        })

    }

    private fun loadMoreData() {
        isLoading = true
        page++
        Log.i("dsadasd", "onLoadMore: $page")
        val data =  HashMap<String,Any>()
        data["page"] = page
        viewModel.getEvents(data, Constants.GET_EVENTS)
    }

    private fun getTopicsList() {
        topicList.add(DropDownData("Stocks"))
        topicList.add(DropDownData("Crypto"))
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

        binding.rangeCalenderOneTime.clearSelectedDays()

        binding.rangeCalenderOneTime.post {
            val calendarLayout = binding.rangeCalenderOneTime.getChildAt(0) as? LinearLayout
            val header = calendarLayout?.getChildAt(0)
            header?.visibility = View.GONE
            calendarLayout?.setPadding(0, 0, 0, 0)
            updateMonthYearTexts()

        }

        binding.rangeCalenderOneTime.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val selectedDate = eventDay.calendar.time

                // Format date to UTC ISO 8601 format: yyyy-MM-dd'T'HH:mm:ss'Z'
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")

                userSelectedDate = sdf.format(selectedDate)

                Log.i("SelectedDate", "onDayClick: $userSelectedDate")
            }
        })





        binding.shareLayout.tabShare.setOnClickListener {


//            val intent = Intent(requireContext(), CommonActivity::class.java).putExtra("from","common_share")
//            startActivity(intent)

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, CommonShareFragment())
                .addToBackStack(null)
                .commit()

        }
        binding.shareLayout.tabStream.setOnClickListener {
//            val intent = Intent(requireContext(), CommonActivity::class.java).putExtra("from","common_stream")
//            startActivity(intent)

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, CommonStreamFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.shareLayout.tabVault.setOnClickListener {
//            val intent = Intent(requireContext(), CommonActivity::class.java).putExtra("from","common_vault")
//            startActivity(intent)

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, CommonVaultFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.tabLayoutBottom.tabExchange.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, ExchangeFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.tabLayoutBottom.tabEcosystem.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, EcosystemFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.rangeCalenderOneTime.setOnPreviousPageChangeListener(object :
            OnCalendarPageChangeListener {
            override fun onChange() {
                updateMonthYearTexts()
            }
        })

        binding.rangeCalenderOneTime.setOnForwardPageChangeListener(object : OnCalendarPageChangeListener {
            override fun onChange() {
                updateMonthYearTexts()
            }
        })




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
                    if (isEmptyField()){
                        val multipartImage = imageUri?.let { convertImageToMultipart(it) }
                        val data = HashMap<String, RequestBody>()
                        data["title"] = binding.etTitle.text.toString().trim().toRequestBody()
                        data["topic"] = binding.etTopic.text.toString().trim().toRequestBody()
                        data["description"] = binding.etDescription.text.toString().trim().toRequestBody()
                        data["type"] = "own_events".toRequestBody()
                        data["scheduledDate"] = userSelectedDate.toString().toRequestBody()
                        data["public"] = visibilityMode.toString().toRequestBody()
                        viewModel.addEvent(data,Constants.CREATE_EVENT,multipartImage)
                    }
                }
                R.id.tvCancel ->{
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

                fileUri?.let { uri ->
                    val fileName = getFileNameFromUri(requireContext(), uri)
                    binding.etUploadFile.setText(fileName)
                }

            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                // Handle error if needed
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }

        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }

        return result ?: "unknown_file"
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
                        "getEvents" -> {
                            val myDataModel: GetEventsApiResponse? = BindingUtils.parseJson(it.data.toString())
                            try {
                                myDataModel?.data?.let { eventData ->
                                    totalPages = myDataModel.data!!.pagination?.total?: 1

                                    eventsList = myDataModel.data!!.events!!

                                    Log.i("dasdasdasdasd", "initObserver: $eventsList")
                                    eventData.pagination?.total?.let { total ->
                                        if (page <= totalPages!!) {
                                            isLoading = false
                                        }
                                    }

                                    val events = eventData.events
                                    if (page == 1) {
                                        reminderAdapter.list = events
                                    } else {
                                        reminderAdapter.addToList(events)
                                    }
                                    if (events != null) {
                                        markUnderlineEvents(events)

                                    }
                                }
                            } catch (e : Exception){
                                e.printStackTrace()
                            }

                        }

                        //  if (myDataModel.data?.events != null){
//                                        reminderAdapter.list = myDataModel.data?.events
//                                    }
                        "addEvent" ->{
                            val myDataModel : SimpleApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                showToast(myDataModel.message.toString())
                                getEventsData()
                                binding.etDescription.setText("")
                                binding.etTitle.setText("")
                                binding.etTopic.setText("")
                                binding.etUploadFile.setText("")
                                binding.calendarCons.visibility = View.VISIBLE
                                binding.consAddEvent.visibility = View.GONE
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
        val calendarDate: Calendar? = binding.rangeCalenderOneTime.currentPageDate

        if (calendarDate != null) {
            val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
            val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

            binding.tvMonth.text = monthFormat.format(calendarDate.time)
            binding.tvYear.text = yearFormat.format(calendarDate.time)
        } else {
            binding.tvMonth.text = ""
            binding.tvYear.text = ""
        }
    }

    private fun markUnderlineEvents(events: List<GetEventsApiResponse.Data.Event?>) {
        val underlineEvents = mutableListOf<EventDay>()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        events.forEach { event ->
            event?.let {
                try {
                    val date = sdf.parse(it.scheduledDate)
                    val calendar = Calendar.getInstance()
                    calendar.time = date!!

                    val color = when (it.type) {
                        "scheduled_lives" -> Color.parseColor("#BF9000")
                        "general_events_by_admin" -> Color.parseColor("#AB8BC3")
                        "own_events" -> Color.parseColor("#00B050")
                        else -> Color.BLACK // Default fallback
                    }

                    val drawable = createInsetLineDrawable(color, bottomInset = 25)
                    underlineEvents.add(EventDay(calendar, drawable))

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }


        binding.rangeCalenderOneTime.setEvents(underlineEvents)
    }


    fun createInsetLineDrawable(color: Int, bottomInset: Int = 8): InsetDrawable {
        val line = GradientDrawable()
        line.shape = GradientDrawable.RECTANGLE
        line.setColor(color)
        line.setSize(100, 10) // Width and height of the underline

        // Apply inset at bottom only (left, top, right, bottom)
        return InsetDrawable(line, 0, 0, 0, bottomInset)
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