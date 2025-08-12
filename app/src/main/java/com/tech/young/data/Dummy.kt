package com.tech.young.data

import android.net.Uri
import android.os.Parcelable
import android.view.View
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

    data class CalendarDay(
        val date: Date,
        val isCurrentMonth: Boolean,
        var isSelected: Boolean = false
    )

@Parcelize
data class DropDownData(
    val title : String,
    val actualValue: String = "",
    var isSelected: Boolean = false

) : Parcelable


data class SubViewClickBean(
    var v : View,
    var bean : NewsItem,
    var position : Int
)

// Parent item - Section
data class NewsSection(
    val heading: String,
    val headingLink: String? = null,
    val items: List<NewsItem>
)

// Child item - Sub-category
data class NewsItem(
    val title: String,
    val link: String
)



@Parcelize
data class UserData(
    val _id: String?,
    val profileImage: String?,
    val role: String?,
    val username: String?
) : Parcelable



data class RSSFeed(
    val title: String?,
    val url: String?
)

data class RSSSection(
    val title: String?,
    val url: String?,
    val feeds: List<RSSFeed>?
)

@Parcelize
data class RSSItem(
    val title: String?,
    val link: String?,
    val pubDate: String?,
    val description: String?,
    var imageURL: String? = null
):Parcelable


data class SortingItem(
    val title: String,
    val key : String,
    var isSelected: Boolean = false,
)


data class FilterItem(
    val title: String,
    val isHeader: Boolean = false,
    var isSelected: Boolean = false,
    val key: String = "",        // e.g., "distance", "rating"
    val value: Any? = null       // e.g., 1, 2, true, etc.
)


data class DiditItems(
    var title : String
)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: Long
)



data class StockInfo(
    val symbol: String,
    val value: String,
    val isDown: Boolean
)


data class IndexQuote(
    val c: Double?,  // current price
    val d: Double?,  // change
    val dp: Double?, // percent change
    val h: Double?,  // high
    val l: Double?,  // low
    val o: Double?,  // open
    val pc: Double?, // previous close
    val t: Long?     // timestamp
)

data class ImageModel(
    var image_Url: String?,
    var image_Uri: Uri? = null,
    var type:String?
)


data class AdditionalPhotos(
    var image: Uri?
)

data class SubscriptionFeatureList(
    var title: String
)