package com.tech.young.ui.news

import com.tech.young.data.NewsItem

interface NewsItemClickListener {
    fun onNewsClicked(newsItem: NewsItem)

}