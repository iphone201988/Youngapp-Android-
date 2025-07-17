package com.tech.young.data

import android.os.Parcelable
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



@Parcelize
data class RssItem(
    val title: String,
    val link: String,
    val imageUrl: String?,
    val source: String
) : Parcelable


data class SortingItem(
    val isSelected: Boolean = false,
    val title: String
)

