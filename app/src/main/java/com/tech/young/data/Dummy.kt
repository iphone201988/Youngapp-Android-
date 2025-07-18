package com.tech.young.data

import android.os.Parcelable
import androidx.test.espresso.Root
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


//@Root(name = "channel", strict = false)
//data class NewsFeedList(
//    @ElementList(name = "item", inline = true)
//    val articleList: List<NewsFeed>
//)
//
//@Root(name = "item", strict = false)
//data class NewsFeed(
//    @Element(name="title")
//    val title: String,
//    @Element(name="link")
//    val link:String
//)


data class SortingItem(
    val isSelected: Boolean = false,
    val title: String
)

