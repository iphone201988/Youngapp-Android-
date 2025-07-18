package com.tech.young.data

import android.os.Parcelable
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


// Parent item - Section
data class NewsSection(
    val heading: String,
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
    val isSelected: Boolean = false,
)


data class DiditItems(
    var title : String
)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: Long
)