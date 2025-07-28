package com.tech.young.ui.home

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Xml
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.RSSItem
import com.tech.young.data.api.Constants
import com.tech.young.data.api.StockQuoteService
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.data.model.TrendingTopicApiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentHomeBinding
import com.tech.young.databinding.HolderTrendingTopicBinding
import com.tech.young.databinding.ItemLayoutHomeNewsBinding
import com.tech.young.databinding.ItemLayoutNewsDataBinding
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.consumer_stream.ConsumerStreamActiivty
import com.tech.young.ui.streaming_activity.StreamActivity
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
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private val viewModel:HomeActivityVM by viewModels()
    private var tickerScrollHandler: Handler? = null
    private var tickerScrollRunnable: Runnable? = null
    private val rssItems = mutableListOf<RSSItem>()
    private lateinit var newsAdapter: SimpleRecyclerViewAdapter<RSSItem, ItemLayoutHomeNewsBinding>


    private lateinit var adapterTrending: SimpleRecyclerViewAdapter<TrendingTopicApiResponse.Data.Topic, HolderTrendingTopicBinding>
    override fun onCreateView(view: View) {
        // view
        initView()
        viewModel.getTrendingTopics(Constants.GET_TRENDING_TOPICS)

        // click
        initOnClick()


        // observer
        initObserver()
    }

    private suspend fun getQuotes() {
        val quotes = StockQuoteService.fetchQuotes(listOf("AAPL", "TSLA", "BTC"))
    }

    private fun loadRSS() {
        lifecycleScope.launch {
            try {
                val url = "https://feeds.content.dowjones.io/public/rss/mw_topstories"
                val items = parseFeed(url)

                // Only take the top 5 items
                val topItems = items.take(5)

                val updatedItems = topItems.map { item ->
                    val metadata = fetchUrlMetadata(item.link ?: "")
                    val imageUrlMeta = metadata["imageUrl"]
                    val finalImageUrl = imageUrlMeta?.let { resolveImageUrl(item.link ?: "", it) } ?: item.imageURL
                    item.copy(imageURL = finalImageUrl)
                }



                rssItems.clear()
                Log.i("dsaasd", "loadRSS: $updatedItems")

                rssItems.addAll(updatedItems)
                newsAdapter.list = rssItems
                newsAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("RSS", "Error parsing feed", e)
            }
        }
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
    suspend fun parseFeed(feedUrl: String): List<RSSItem> = withContext(Dispatchers.IO) {
        val url = URL(feedUrl)
        val stream = url.openConnection().getInputStream()
        parse(stream)
    }

    private fun parse(inputStream: InputStream): List<RSSItem> {
        val items = mutableListOf<RSSItem>()
        var currentItem: RSSItem? = null
        var text = ""
        var imageUrl: String? = null
        var defaultImageUrl: String? = null
        var insideImageTag = false

        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name ?: ""

            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when {
                        tagName.equals("item", ignoreCase = true) -> {
                            currentItem = RSSItem(null, null, null, null)
                            imageUrl = null
                        }
                        tagName.equals("media:content", ignoreCase = true) ||
                                (tagName == "content" && parser.prefix == "media") -> {
                            // Detect namespaced media:content
                            imageUrl = parser.getAttributeValue(null, "url")
                            if (imageUrl.isNullOrEmpty()) {
                                for (i in 0 until parser.attributeCount) {
                                    if (parser.getAttributeName(i).contains("url")) {
                                        imageUrl = parser.getAttributeValue(i)
                                        break
                                    }
                                }
                            }
                        }
                        tagName.equals("image", ignoreCase = true) -> {
                            insideImageTag = true
                        }
                    }
                }

                XmlPullParser.TEXT -> {
                    text = parser.text ?: ""
                }

                XmlPullParser.END_TAG -> {
                    when {
                        currentItem != null -> {
                            when (tagName) {
                                "title" -> currentItem = currentItem.copy(title = text.trim())
                                "link" -> currentItem = currentItem.copy(link = text.trim())
                                "pubDate" -> currentItem = currentItem.copy(pubDate = text.trim())
                                "description" -> currentItem = currentItem.copy(description = text.trim())
                                "item" -> {
                                    currentItem = currentItem.copy(
                                        imageURL = imageUrl ?: defaultImageUrl
                                    )
                                    items.add(currentItem)
                                }
                            }
                        }

                        insideImageTag && tagName.equals("url", ignoreCase = true) -> {
                            defaultImageUrl = text.trim()
                        }

                        tagName.equals("image", ignoreCase = true) -> {
                            insideImageTag = false
                        }
                    }
                }
            }

            eventType = parser.next()
        }

        return items
    }





    private fun resolveImageUrl(baseUrl: String, imageUrl: String?): String? {
        if (imageUrl.isNullOrEmpty()) return null
        return try {
            URL(URL(baseUrl), imageUrl).toString()
        } catch (e: MalformedURLException) {
            null
        }
    }

    private fun getAds() {
        viewModel.getAds(Constants.GET_ADS)
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_home
    }

    override fun getViewModel(): BaseViewModel {
       return viewModel
    }

    private fun initView(){
        binding.tabShare.tabShare.setOnClickListener {
         val intent = Intent(requireContext(),CommonActivity::class.java).putExtra("from","common_share")
            startActivity(intent)
        }

        binding.tabShare.tabStream.setOnClickListener {
         val intent = Intent(requireContext(),CommonActivity::class.java).putExtra("from","common_stream")
            startActivity(intent)
        }


        binding.tabShare.tabVault.setOnClickListener {
         val intent = Intent(requireContext(),CommonActivity::class.java).putExtra("from","common_vault")
            startActivity(intent)
        }

        binding.tabLayoutBottom.tabExchange.setOnClickListener {
            val intent = Intent(requireContext(),CommonActivity::class.java).putExtra("from","exchange")
            startActivity(intent)
        }

        binding.tabLayoutBottom.tabEcosystem.setOnClickListener {
            val intent = Intent(requireContext(),CommonActivity::class.java).putExtra("from","ecosystem")
            startActivity(intent)
        }
        setupTickerRecycler()


        loadRSS()

        initAdapterTrending()

        initAdapterNews()

        initAdapter()


    }



    private fun initOnClick(){
        viewModel.onClick.observe(requireActivity()){
            when(it?.id){
                R.id.tvNewsView->{
//                    val intent = Intent(requireContext(), StreamActivity::class.java)
//                    startActivity(intent)
                    val intent=Intent(requireContext(),CommonActivity::class.java)
                    intent.putExtra("from","view_more")
                    startActivity(intent)
                }
                R.id.tvMembersView->{


//                    val intent=Intent(requireContext(),CommonActivity::class.java)
//                    intent.putExtra("from","view_more")
//                    startActivity(intent)
                }
                R.id.tvSmallView->{

//                    val intent=Intent(requireContext(),CommonActivity::class.java)
//                    intent.putExtra("from","view_more")
//                    startActivity(intent)
                }
            }
        }

    }


    private fun initObserver(){
        viewModel.observeCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    showLoading()
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "getTrendingTopic" ->{
                            getAds()
                            val myDataModel : TrendingTopicApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    if (myDataModel.data?.topics != null){
                                        adapterTrending.list = myDataModel.data?.topics
                                    }

                                }
                            }
                        }
                        "getAds" ->{
                            val myDataModel : GetAdsAPiResponse ? = BindingUtils.parseJson(it.data.toString())
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

//    fun setupTickerRecycler() {
//        val rvTicker = binding.rvTicker
//        val stockList = listOf(
//            StockItem("AAPL", "+1.23%", true),
//            StockItem("TSLA", "-2.45%", false),
//            StockItem("BTC", "-2.45%", false),
//            StockItem("TSLA", "-2.45%", false),
//            StockItem("BTC", "+0.78%", true),
//            StockItem("BTC", "-2.45%", false),
//            StockItem("AAPL", "-2.45%", false),
//
//            )
//        val adapter = TickerAdapter(stockList)
//        rvTicker.adapter = adapter
//        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
//        rvTicker.layoutManager = layoutManager
//
//        // Auto-scroll continuously
//        val scrollSpeed = 2
//        val handler = Handler(Looper.getMainLooper())
//        val runnable = object : Runnable {
//            override fun run() {
//                rvTicker.scrollBy(scrollSpeed, 0)
//                handler.postDelayed(this, 30)
//            }
//        }
//        handler.post(runnable)
//    }



    fun setupTickerRecycler() {
        val rvTicker = binding.rvTicker
        val adapter = TickerAdapter(emptyList())
        rvTicker.adapter = adapter
        rvTicker.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        val symbols = listOf("AAPL", "TSLA", "BTC")

        viewLifecycleOwner.lifecycleScope.launch {
            // Fetch data from API
            val quotes = StockQuoteService.fetchQuotes(symbols)

            Log.i("hgfhghg", "setupTickerRecycler: $quotes")

            // Convert to StockItem list
            val stockItems = quotes.map { (symbol, quote) ->
                val isUp = (quote.d ?: 0.0) >= 0
                val percentChange = String.format("%.2f%%", quote.dp ?: 0.0)
                StockItem(symbol, percentChange, isUp)
            }
            Log.i("hgfhghg", "setupTickerRecycler: $stockItems")



            // Tripled list for infinite effect
            val loopedList = stockItems + stockItems + stockItems
            adapter.setItems(loopedList)

            // Scroll to middle
            rvTicker.scrollToPosition(loopedList.size / 2)

            // Start auto-scroll
            startTickerAutoScroll(rvTicker)
        }
    }


    private fun startTickerAutoScroll(recyclerView: RecyclerView) {
        val scrollSpeed = 2
        tickerScrollHandler = Handler(Looper.getMainLooper())
        tickerScrollRunnable = object : Runnable {
            override fun run() {
                recyclerView.scrollBy(scrollSpeed, 0)
                tickerScrollHandler?.postDelayed(this, 30)
            }
        }
        tickerScrollHandler?.post(tickerScrollRunnable!!)
    }

    private fun initAdapterTrending() {
        adapterTrending =
            SimpleRecyclerViewAdapter(R.layout.holder_trending_topic, BR.bean) { view, bean, pos ->
                when (view.id) {
                    R.id.consMain -> {
                        // Handle click
                    }
                }
            }

        binding.rvTrendingTopic.adapter = adapterTrending
        binding.rvTrendingTopic.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }



    /**** news adapter ***/
    private fun initAdapterNews() {
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val layoutManager2 = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val layoutManager3 = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val customOrder = listOf("1", "3", "2", "4", "", "5")
        val sortedNews = reorderNewsList(newsList(), customOrder)
        val adapter = NewsCustomAdapter(requireContext(), sortedNews)
        adapter.setHasStableIds(true)


//        binding.rvNews.layoutManager = layoutManager
//        binding.rvNews.setHasFixedSize(true)
//        binding.rvNews.adapter = newsAdapter
//        binding.rvNews.itemAnimator = null

        newsAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_home_news, BR.bean) { v, m, pos ->
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
        binding.rvNews.adapter = newsAdapter
        binding.rvNews.layoutManager = layoutManager

        binding.rvMembers.layoutManager = layoutManager2
        binding.rvMembers.setHasFixedSize(true)
        binding.rvMembers.adapter = adapter
        binding.rvMembers.itemAnimator = null

        binding.rvSmallBusiness.layoutManager = layoutManager3
        binding.rvSmallBusiness.setHasFixedSize(true)
        binding.rvSmallBusiness.adapter = adapter
        binding.rvSmallBusiness.itemAnimator = null
    }

    private fun newsList(): ArrayList<String> {
        return arrayListOf(
            "1 News Title",
            "2 News Title",
            "3 News Title",
            "4 News Title",
            "",
            "5 News Title",
        )
    }

    fun reorderNewsList(
        list: List<String>,
        order: List<String>,
    ): List<String> {
        return order.mapNotNull { key ->
            list.find { item ->
                (key == "" && item == "") || item.trimStart().startsWith("$key ")
            }
        }
    }

    /** handle ads adapter **/
    // adapter
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>
    private fun initAdapter() {
        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        binding.rvAds.adapter = adsAdapter
    }

    private var getList = listOf(
        "", "", "", "", ""
    )

}