package com.tech.young.ui.news

import android.content.Intent
import android.util.Log
import android.util.Xml
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.data.NewsItem
import com.tech.young.data.RSSItem
import com.tech.young.databinding.FragmentNewsDetailBinding
import com.tech.young.databinding.ItemLayoutNewsDataBinding
import com.tech.young.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import org.jsoup.Jsoup
import org.xmlpull.v1.XmlPullParser
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class NewsDetailFragment : BaseFragment<FragmentNewsDetailBinding>() {

    private val viewModel: NewsViewModel by viewModels()
    private lateinit var newsAdapter: SimpleRecyclerViewAdapter<RSSItem, ItemLayoutNewsDataBinding>
    private val rssItems = mutableListOf<RSSItem>()

    override fun onCreateView(view: View) {
        val url = arguments?.getString("url")
        initAdapter()
        lifecycleScope.launch {
            try {
                val items = parseFeed("$url")
                Log.e("xxx", "${items.size}")
                val updatedItems = items.map { item ->
                    val metadata = fetchUrlMetadata(item.link ?: "")
                    val imageUrlMeta = metadata["imageUrl"]
                    val imageUrl = resolveImageUrl("${url}", imageUrlMeta)
                    item.copy(imageURL = imageUrl)
                }
                rssItems.clear()
                rssItems.addAll(updatedItems)
                newsAdapter.list = rssItems
                newsAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("RSS", "Error parsing feed", e)
            }
        }

        // click
        initOnClick()

    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_news_detail
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle click **/
    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    /** handle adapter **/
    private fun initAdapter() {
        newsAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_news_data, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.consMain->{
                        val intent= Intent(requireContext(),CommonActivity::class.java).apply {
                            putExtra("from","single_news")
                            putExtra("linkUrl",m.link)
                        }
                        startActivity(intent)
                    }
                }
            }

        newsAdapter.list = rssItems
        binding.rvNewsSources.adapter = newsAdapter
    }

    suspend fun parseFeed(feedUrl: String): List<RSSItem> = withContext(Dispatchers.IO) {
        val url = URL(feedUrl)
        val stream = url.openConnection().getInputStream()
        parse(stream)
    }

    private fun parse(inputStream: InputStream): List<RSSItem> {
        val items = mutableListOf<RSSItem>()
        var currentItem: RSSItem? = null
        var text = ""

        val parser = Xml.newPullParser()
        parser.setInput(inputStream, null)
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (tagName == "item") {
                        currentItem = RSSItem(null, null, null, null)
                    }
                }

                XmlPullParser.TEXT -> {
                    text = parser.text
                }

                XmlPullParser.END_TAG -> {
                    if (currentItem != null) {
                        when (tagName) {
                            "title" -> currentItem = currentItem.copy(title = text.trim())
                            "link" -> currentItem = currentItem.copy(link = text.trim())
                            "pubDate" -> currentItem = currentItem.copy(pubDate = text.trim())
                            "description" -> currentItem =
                                currentItem.copy(description = text.trim())

                            "item" -> {
                                items.add(currentItem)
                                currentItem = null
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return items
    }

    suspend fun fetchUrlMetadata(url: String): Map<String, String?> {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .protocols(listOf(Protocol.HTTP_1_1))
            .build()

        val request = Request.Builder()
            .url(url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
            )
            .header("Accept", "text/html")
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val html = response.body?.string() ?: return@withContext emptyMap()
                val doc = Jsoup.parse(html)

                val title = doc.select("meta[property=og:title]").attr("content")
                    .takeIf { it.isNotEmpty() } ?: doc.select("title").text()
                val description = doc.select("meta[property=og:description]").attr("content")
                    .takeIf { it.isNotEmpty() } ?: doc.select("meta[name=description]").attr("content")

                var imageUrl = doc.select("meta[property=og:image]").attr("content")
                if (imageUrl.isNullOrEmpty()) {
                    imageUrl = doc.select("meta[name=image]").attr("content")
                }
                if (imageUrl.isNullOrEmpty()) {
                    imageUrl = doc.select("img[src]").firstOrNull()?.attr("src")
                }
                if (imageUrl.isNullOrEmpty()) {
                    imageUrl = doc.select("link[rel=shortcut icon]").attr("href")
                }

                mapOf("title" to title, "description" to description, "imageUrl" to imageUrl)
            } catch (e: IOException) {
                Log.e("fetchUrlMetadata", "IOException: ${e.message}")
                emptyMap()
            } catch (e: Exception) {
                Log.e("fetchUrlMetadata", "General Exception: ${e.message}")
                emptyMap()
            }
        }
    }

    private fun resolveImageUrl(baseUrl: String, imageUrl: String?): String? {
        if (imageUrl.isNullOrEmpty()) return null
        return if (imageUrl.startsWith("http")||imageUrl.startsWith("https")) {
            imageUrl
        } else {
            try {
                URL(URL(baseUrl), imageUrl).toString()
            } catch (e: MalformedURLException) {
                null
            }
        }
    }


}