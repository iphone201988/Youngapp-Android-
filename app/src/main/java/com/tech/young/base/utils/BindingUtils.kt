package com.tech.young.base.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.data.api.Constants
import com.tech.young.data.model.ChooseAccountType
import com.tech.young.data.model.GetVaultApiResponse
import com.tech.young.data.model.SideMenuBar
import com.tech.young.data.model.VaultExchangeApiResponse
import com.tech.young.databinding.ItemLayoutRvExchangeMemberBinding
import com.tech.young.databinding.ItemLayoutRvMembersBinding
import com.github.dhaval2404.imagepicker.util.FileUtil.getTempFile
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.tech.young.data.NewsItem
import com.tech.young.data.NewsSection
import com.tech.young.databinding.ItemLayoutSubNewsBinding
import com.tech.young.ui.common.CommonActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object BindingUtils {

    @BindingAdapter("setImageFromUrl")
    @JvmStatic
    fun setImageFromUrl(image: ShapeableImageView, url: String?) {
        if (url != null) {
            Glide.with(image.context).load(url).placeholder(R.drawable.user).error(R.drawable.user)
                .into(image)
        }
    }

    @BindingAdapter("setUserImageFromBaseUrl")
    @JvmStatic
    fun setUserImageFromBaseUrl(image: ShapeableImageView, url: String?) {
        if (url != null) {
            Glide.with(image.context).load(Constants.BASE_URL_IMAGE+url).placeholder(R.drawable.user)
                .error(R.drawable.user).into(image)
        }
    }

    @BindingAdapter("setRating")
    @JvmStatic
    fun setRating(view: per.wsj.library.AndRatingBar, ratingValue: Double?) {
        view.rating = (ratingValue ?: 0.0).toFloat()
    }



    @BindingAdapter("setAccountName")
    @JvmStatic
    fun setAccountName(textView: AppCompatTextView, data: ChooseAccountType) {
        val context = textView.context
        if (data.isSelected) {
            textView.setTextColor(context.getColor(R.color.white))
            textView.setBackgroundResource(R.drawable.radius_border_purple)
        } else {
            textView.setTextColor(context.getColor(R.color.text_gray))
            textView.setBackgroundResource(R.drawable.radius_border_gray)
        }

        textView.text = data.name
    }

    @BindingAdapter("sideMenuName")
    @JvmStatic
    fun sideMenuName(textView: AppCompatTextView, data: SideMenuBar) {
        val context = textView.context
        if (data.isSelected) {
            textView.setTextColor(context.getColor(R.color.white))
            textView.setBackgroundResource(R.drawable.selected_side_menu_bg)
        } else {
            textView.setTextColor(context.getColor(R.color.text_black))
            textView.setBackgroundResource(R.drawable.unselected_side_menu_bg)
        }

        textView.text = data.name
    }
     @BindingAdapter("setMenuHeading")
    @JvmStatic
    fun setMenuHeading(textView: AppCompatTextView, data: SideMenuBar) {
         if(data.heading!=null && data.headingShow){
             textView.visibility=View.VISIBLE
             textView.text = data.heading
         }
         else{
             textView.visibility=View.GONE
         }


    }


    @BindingAdapter("setTintColor")
    @JvmStatic
    fun setTintColor(view: AppCompatImageView, isSelected: Boolean) {
        val color = if (isSelected) ContextCompat.getColor(view.context, R.color.white)
        else ContextCompat.getColor(view.context, R.color.colorPrimary)

        view.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }


    fun styleSystemBars(activity: Activity, color: Int) {
        activity.window.navigationBarColor = color
    }

    fun statusBarStyleWhite(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            activity.window.statusBarColor = Color.TRANSPARENT
        }
    }

    fun statusBarStyleBlack(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // Ensures black text/icons
            activity.window.statusBarColor = Color.TRANSPARENT // Transparent status bar
        }


    }


    fun setNavigationBarStyle(
        activity: Activity, navigationBarColorResId: Int,   // Background color of nav bar
        isLightIcons: Boolean           // True = dark icons, False = light icons
    ) {
        val color = ContextCompat.getColor(activity, navigationBarColorResId)
        activity.window.navigationBarColor = color

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.setSystemBarsAppearance(
                if (isLightIcons) WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        } else {
            @Suppress("DEPRECATION") val decorView = activity.window.decorView
            decorView.systemUiVisibility = if (isLightIcons) {
                decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
        }
    }

    @JvmStatic
    inline fun <reified T> parseJson(json: String): T? {
        return try {
            val gson = Gson()
            gson.fromJson(json, T::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    @BindingAdapter("setEditProfileLogo")
    @JvmStatic
    fun setEditProfileLogo(imageView: AppCompatImageView, resouceId: Int) {
        imageView.setImageResource(resouceId)
    }

    fun hasPermissions(context: Context?, permissions: Array<String>?): Boolean {
        if (context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context, permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.CAMERA,
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
    }

    /**** Convert user image to multipart function  ***/
    fun convertImageToMultipart(imageUri: Uri, context: Context): MultipartBody.Part {
        val file = getTempFile(context, imageUri)
        val fileName =
            "${file!!.nameWithoutExtension}_${System.currentTimeMillis()}.${file.extension}"
        val newFile = File(file.parent, fileName)
        file.renameTo(newFile)
        return MultipartBody.Part.createFormData(
            "profileImage", newFile.name, newFile.asRequestBody("image/*".toMediaTypeOrNull())
        )
    }

    fun createImageMultipartFromUri(
        context: Context,
        uri: Uri,
        uploadType: String
    ): MultipartBody.Part? {
        val file = getTempFile(context, uri) ?: return null

        val timestamp = System.currentTimeMillis()
        val fileName = "${file.nameWithoutExtension}_$timestamp.${file.extension}"
        val renamedFile = File(file.parent, fileName)
        file.renameTo(renamedFile)

        return MultipartBody.Part.createFormData(
            uploadType,
            renamedFile.name,
            renamedFile.asRequestBody("image/*".toMediaTypeOrNull())
        )
    }

    @BindingAdapter("setFormattedDate")
    @JvmStatic
    fun setFormattedDate(textView: TextView, dateString: String?) {
        if (dateString.isNullOrBlank()) {
            textView.text = ""
            return
        }

        try {
            // Parse the ISO 8601 date (Z means UTC)
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)

            // Format to "12 January 2025"
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            textView.text = date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            // If parsing fails, avoid crashing and show blank or fallback
            textView.text = ""
            e.printStackTrace()
        }
    }


    @BindingAdapter("dayFromDate")
    @JvmStatic
    fun dayFromDate(textView: TextView, dateString: String?) {
        if (dateString.isNullOrBlank()) {
            textView.text = ""
            return
        }

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)

            val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
            textView.text = date?.let { dayFormat.format(it) } ?: ""
        } catch (e: Exception) {
            textView.text = ""
            e.printStackTrace()
        }
    }


    @BindingAdapter("setDayOfWeek")
    @JvmStatic
    fun setDayOfWeek(textView: TextView, dateString: String?) {
        if (dateString.isNullOrBlank()) {
            textView.text = ""
            return
        }

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)

            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault()) // EEE gives short weekday: Mon, Tue, etc.
            textView.text = date?.let { dayFormat.format(it) } ?: ""
        } catch (e: Exception) {
            textView.text = ""
            e.printStackTrace()
        }
    }


    @BindingAdapter("memberView")
    @JvmStatic
    fun memberView(view : RecyclerView, memberList : List<GetVaultApiResponse.Data.Vault.Member?>?) {
        val limitedList = memberList?.take(3)
        val memberAdapter = SimpleRecyclerViewAdapter<GetVaultApiResponse.Data.Vault.Member, ItemLayoutRvMembersBinding>(R.layout.item_layout_rv_members, BR.bean) { v, m, pos ->

            }
            view.adapter = memberAdapter
            memberAdapter.list  = limitedList

    }

    @BindingAdapter("exchangeMemberView")
    @JvmStatic
    fun exchangeMemberView(view : RecyclerView, memberList : List<VaultExchangeApiResponse.Data.Vault.Member?>?) {
        val limitedList = memberList?.take(3)
        val memberAdapter = SimpleRecyclerViewAdapter<VaultExchangeApiResponse.Data.Vault.Member, ItemLayoutRvExchangeMemberBinding>(R.layout.item_layout_rv_exchange_member, BR.bean) { v, m, pos ->

        }
        view.adapter = memberAdapter
        memberAdapter.list  = limitedList

    }


    @BindingAdapter("setFormattedCreatedAt")
    @JvmStatic
    fun setFormattedCreatedAt(textView: TextView, createdAt: String?) {
        if (createdAt.isNullOrEmpty()) return

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val date = inputFormat.parse(createdAt)

            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val formattedDate = outputFormat.format(date ?: Date())

            textView.text = formattedDate
        } catch (e: Exception) {
            e.printStackTrace()
            textView.text = ""
        }
    }

    @BindingAdapter("setLikeIcon")
    @JvmStatic
    fun setLikeIcon(view: AppCompatImageView, isLiked: Boolean) {
        val drawableRes = if (isLiked) R.drawable.iv_filled_heart else R.drawable.ic_heart
        view.setImageResource(drawableRes)
    }


    @BindingAdapter("setSaveIcon")
    @JvmStatic
    fun setSaveIcon(view: AppCompatImageView, isLiked: Boolean) {
        val drawableRes = if (isLiked) R.drawable.iv_filled_save else R.drawable.iv_saves_icon
        view.setImageResource(drawableRes)
    }


    /** Compress and Rotate Image **/
    fun compressImage(imageUri: Uri, context: Context): Uri? {
        return try {
            val bitmap = decodeSampledBitmapFromUri(
                imageUri, context
            ) // Decode with proper sampling
            val rotatedBitmap =
                bitmap?.let { rotateImageIfRequired(context, it, imageUri) } // Fix orientation

            val outputStream = ByteArrayOutputStream()
            rotatedBitmap?.compress(
                Bitmap.CompressFormat.JPEG, 50, outputStream
            ) // Compress to 50% quality
            val byteArray = outputStream.toByteArray()
            val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            file.writeBytes(byteArray)

            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Decode Bitmap
    private fun decodeSampledBitmapFromUri(
        imageUri: Uri,
        context: Context,
    ): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(imageUri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
            options.inSampleSize = calculateInSampleSize(options)
            options.inJustDecodeBounds = false
            context.contentResolver.openInputStream(imageUri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Calculate its sample size
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
    ): Int {

        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > 800 || width > 800) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= 800 && halfWidth / inSampleSize >= 800) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /** Fix Image Orientation: Rotate Landscape to Portrait Only **/
    private fun rotateImageIfRequired(context: Context, bitmap: Bitmap, imageUri: Uri): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val exif = ExifInterface(inputStream!!)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()

            val rotationAngle = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }

            if (rotationAngle != 0) {
                val matrix = Matrix()
                matrix.postRotate(rotationAngle.toFloat())
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            bitmap
        }
    }

    fun formatDate(dateString: String?): Pair<String, String> {
        if (dateString.isNullOrBlank()) return Pair("", "")

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val outputFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getDefault()

            val outputFormatTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
            outputFormatTime.timeZone = TimeZone.getDefault()

            val date = inputFormat.parse(dateString)
            if (date == null) return Pair("", "")

            val calendar = Calendar.getInstance().apply { time = date }
            val currentDate = Calendar.getInstance()

            return when {
                isToday(calendar, currentDate) -> Pair("Today", outputFormatTime.format(date))
                isYesterday(calendar, currentDate) -> Pair("Yesterday", outputFormatTime.format(date))
                else -> Pair(outputFormat.format(date), outputFormatTime.format(date))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair("", "")
        }
    }

    fun isToday(messageDate: Calendar, currentDate: Calendar): Boolean {
        return messageDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
                messageDate.get(Calendar.DAY_OF_YEAR) == currentDate.get(Calendar.DAY_OF_YEAR)
    }

    fun isYesterday(messageDate: Calendar, currentDate: Calendar): Boolean {
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return messageDate.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                messageDate.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
    }

    fun formatDateTimeForChat(dateString: String?): String {
        if (dateString.isNullOrBlank()) return ""

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val outputFormat = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getDefault() // converts to local time

            val date = inputFormat.parse(dateString)
            if (date != null) outputFormat.format(date) else ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }



    @BindingAdapter("childNewsAdapter")
    @JvmStatic
    fun childNewsAdapter(view : RecyclerView , subNews : List<NewsItem>?){

        // Create and set a LayoutManager for the inner RecyclerView
        val layoutManager = LinearLayoutManager(view.context)
        view.layoutManager = layoutManager
        val context = view.context
        val eventAdapter = SimpleRecyclerViewAdapter<NewsItem, ItemLayoutSubNewsBinding>(R.layout.item_layout_sub_news,BR.bean){ v, m, pos ->
            when(v.id){
                R.id.consMain ->{
                    val intent=Intent(context, CommonActivity::class.java)
                    intent.putExtra("from","news_details")
                    intent.putExtra("url",m.link)
                    context.startActivity(intent)
                }
            }
        }
        view.adapter = eventAdapter
        eventAdapter.list = subNews
        eventAdapter.notifyDataSetChanged()

    }


    fun convertUtcToLocalTime(utcDate: String?): String {
        if (utcDate.isNullOrEmpty()) return ""

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            val outputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }

            val date = inputFormat.parse(utcDate)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }


    fun utcStringToDate(utcString: String?): Date {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            formatter.parse(utcString ?: "") ?: Date()
        } catch (e: Exception) {
            e.printStackTrace()
            Date()
        }
    }


    @JvmStatic
    @BindingAdapter("convertUtcToLocalTimeText")
    fun convertUtcToLocalTimeText(textView: TextView, utcDate: String?) {
        if (utcDate.isNullOrEmpty()) {
            textView.text = ""
            return
        }

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            val outputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }

            val date = inputFormat.parse(utcDate)
            textView.text = outputFormat.format(date!!)
        } catch (e: Exception) {
            e.printStackTrace()
            textView.text = ""
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["scheduleDate", "streamUrl"], requireAll = false)
    fun setStreamStatusIcon(imageView: ImageView, scheduleDate: String?, streamUrl: String?) {
        try {
            val drawableResId = when {
                !scheduleDate.isNullOrEmpty() -> R.drawable.iv_schedule
                !streamUrl.isNullOrEmpty() -> R.drawable.iv_recorded
                else -> R.drawable.iv_live
            }

            imageView.setImageResource(drawableResId)
        } catch (e: Exception) {
            e.printStackTrace()
            // Optional fallback icon to avoid crash
            imageView.setImageResource(R.drawable.dummy_profile)
        }
    }

    /** create image file j**/
    fun createImageFile(context: Context): File? {
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss", Locale.US
        ).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName, ".png", storageDir
        )
        return image
    }




}


