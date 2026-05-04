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
import com.tech.young.data.RSSItem
import com.tech.young.databinding.FragmentNewsDetailBinding
import com.tech.young.databinding.ItemLayoutNewsDataBinding
import com.tech.young.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import org.jsoup.Jsoup
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.collections.map

@AndroidEntryPoint
class NewsDetailFragment : BaseFragment<FragmentNewsDetailBinding>() {

    private val viewModel: NewsViewModel by viewModels()
    private var headingLink : String ? =null
    private lateinit var newsAdapter: SimpleRecyclerViewAdapter<RSSItem, ItemLayoutNewsDataBinding>
    private val rssItems = mutableListOf<RSSItem>()

    override fun onCreateView(view: View) {
        val url = arguments?.getString("url")
        val heading = arguments?.getString("heading")
        val subHeading = arguments?.getString("subHeading")
        headingLink = arguments?.getString("headingLink")


        binding.title.text = "Source: ${heading ?: "Unknown"}"
        binding.subHeading.text = subHeading ?: ""
        initAdapter()

        lifecycleScope.launch {

            binding.progressBar.visibility = View.VISIBLE

            try {
                val items = parseFeed("$url")
                Log.e("RSS_DEBUG", "Feed parsed. Total items: ${items.size}")

                val updatedItems = withContext(Dispatchers.IO) {

                    items.map { item ->

                        async {

                            var finalImageUrl: String? = item.imageURL

                            //  Step 1: If RSS already has image → skip API
                            if (finalImageUrl.isNullOrEmpty()) {

                                try {
                                    val metadata = fetchUrlMetadata(item.link ?: "")
                                    val imageUrlMeta = metadata["imageUrl"]

                                    finalImageUrl = resolveImageUrl(url ?: "", imageUrlMeta)

                                } catch (e: Exception) {
                                    Log.e("RSS_DEBUG", "Metadata fetch failed", e)
                                }
                            }

                            //  Step 2: Fallback → description image
                            if (finalImageUrl.isNullOrEmpty()) {
                                finalImageUrl = extractImageFromDescription(item.description)
                            }

                            //  Step 3: Final fallback → placeholder
                            if (finalImageUrl.isNullOrEmpty()) {
                                finalImageUrl = "https://via.placeholder.com/300x200?text=No+Image"
                            }

                            item.copy(imageURL = finalImageUrl)
                        }
                    }.map { it.await() }
                }

                rssItems.clear()
                rssItems.addAll(updatedItems)

                newsAdapter.list = rssItems
                newsAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Log.e("RSS_DEBUG", "Error parsing feed", e)
            }

            binding.progressBar.visibility = View.GONE
        }

        initOnClick()
    }

    override fun getLayoutResource(): Int = R.layout.fragment_news_detail

    override fun getViewModel(): BaseViewModel = viewModel

    private fun initOnClick() {
        viewModel.onClick.observe(requireActivity()) {
            when(it?.id){
                R.id.ivBack ->{
                    requireActivity().onBackPressedDispatcher.onBackPressed()

                }

                R.id.subHeading ->{
                    val intent = Intent(requireContext(), CommonActivity::class.java).apply {
                        putExtra("from", "single_news")
                        putExtra("linkUrl", headingLink)
                    }
                    startActivity(intent)
                }
            }




        }
    }

    private fun initAdapter() {
        newsAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_news_data, BR.bean) { v, m, _ ->
                if (v.id == R.id.consMain) {
                    val intent = Intent(requireContext(), CommonActivity::class.java).apply {
                        putExtra("from", "single_news")
                        putExtra("linkUrl", m.link)
                    }
                    startActivity(intent)
                }
            }

        newsAdapter.list = rssItems
        binding.rvNewsSources.adapter = newsAdapter
    }

    suspend fun parseFeed(feedUrl: String): List<RSSItem> = withContext(Dispatchers.IO) {
        val stream = URL(feedUrl).openConnection().apply {
            connectTimeout = 60000
            readTimeout = 60000
        }.getInputStream()
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

                    //media:content
                    if (tagName == "media:content") {
                        val imageUrl = parser.getAttributeValue(null, "url")
                        if (!imageUrl.isNullOrEmpty()) {
                            currentItem = currentItem?.copy(imageURL = imageUrl)
                            Log.e("RSS_DEBUG", "media:content image: $imageUrl")
                        }
                    }

                    // media:thumbnail
                    if (tagName == "media:thumbnail") {
                        val imageUrl = parser.getAttributeValue(null, "url")
                        if (!imageUrl.isNullOrEmpty()) {
                            currentItem = currentItem?.copy(imageURL = imageUrl)
                            Log.e("RSS_DEBUG", "media:thumbnail image: $imageUrl")
                        }
                    }

                    // enclosure
                    if (tagName == "enclosure") {
                        val imageUrl = parser.getAttributeValue(null, "url")
                        val type = parser.getAttributeValue(null, "type")

                        if (type?.startsWith("image") == true) {
                            currentItem = currentItem?.copy(imageURL = imageUrl)
                            Log.e("RSS_DEBUG", "enclosure image: $imageUrl")
                        }
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

                            "description" -> {
                                currentItem = currentItem.copy(description = text.trim())

                                val descImage = extractImageFromDescription(text)
                                if (!descImage.isNullOrEmpty()) {
                                    currentItem = currentItem.copy(imageURL = descImage)
                                    Log.e("RSS_DEBUG", "description image: $descImage")
                                }
                            }

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

    private fun extractImageFromDescription(description: String?): String? {
        val regex = Regex("<img[^>]+src=[\"']([^\"']+)[\"']")
        return regex.find(description ?: "")?.groups?.get(1)?.value
    }

    suspend fun fetchUrlMetadata(url: String): Map<String, String?> {

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .protocols(listOf(Protocol.HTTP_1_1))
            .build()

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0")
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) return@withContext emptyMap()

                val html = response.body?.string() ?: return@withContext emptyMap()
                val doc = Jsoup.parse(html)

                var imageUrl = doc.select("meta[property=og:image]").attr("content")

                if (imageUrl.isNullOrEmpty()) {
                    imageUrl = doc.select("img[src]").firstOrNull()?.attr("src")
                }

                mapOf("imageUrl" to imageUrl)

            } catch (e: Exception) {
                emptyMap()
            }
        }
    }

    private fun resolveImageUrl(baseUrl: String, imageUrl: String?): String? {
        if (imageUrl.isNullOrEmpty()) return null
        return if (imageUrl.startsWith("http")) {
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