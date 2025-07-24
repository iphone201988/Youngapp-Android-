package com.tech.young.ui.home

import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.tech.young.BR
import com.tech.young.R
import com.tech.young.base.BaseFragment
import com.tech.young.base.BaseViewModel
import com.tech.young.base.SimpleRecyclerViewAdapter
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.Status
import com.tech.young.base.utils.showToast
import com.tech.young.data.NewsItem
import com.tech.young.data.NewsSection
import com.tech.young.data.api.Constants
import com.tech.young.data.model.GetAdsAPiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentViewMoreBinding
import com.tech.young.databinding.ItemLayoutNewsBinding
import com.tech.young.databinding.ViewMoreItemViewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewMoreFragment : BaseFragment<FragmentViewMoreBinding>() {
    private val viewModel: HomeActivityVM by viewModels()

    // adapter
    private lateinit var newsAdapter: SimpleRecyclerViewAdapter<NewsSection, ItemLayoutNewsBinding>
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<GetAdsAPiResponse.Data.Ad, AdsItemViewBinding>

    private var newsSections = ArrayList<NewsSection>()
    override fun onCreateView(view: View) {
        getNewsList()
        // view
        initView()


        // click
        initOnClick()
        // observer
        initObserver()
    }

    private fun getNewsList() {
         newsSections = arrayListOf(
            NewsSection("Nasdaq",
                "https://www.nasdaq.com/nasdaq-RSS-Feeds",
                arrayListOf(
                NewsItem("Cryptocurrencies","https://www.nasdaq.com/feed/rssoutbound?category=Cryptocurrencies"),
                NewsItem("Markets","https://www.nasdaq.com/feed/rssoutbound?category=Markets"),
                NewsItem("Nasdaq Inc News","https://www.nasdaq.com/feed/rssoutbound?category=Nasdaq"),
                NewsItem("IPO","https://www.nasdaq.com/feed/rssoutbound?category=IPOs"),
                NewsItem("Investing","https://www.nasdaq.com/feed/rssoutbound?category=Investing"),
                NewsItem("Retirement","https://www.nasdaq.com/feed/rssoutbound?category=Retirement"),
                NewsItem("Saving Money","https://www.nasdaq.com/feed/rssoutbound?category=Saving%20Money")
            )
            ),
            NewsSection("Investing.com",
                ""
                ,arrayListOf(
                NewsItem("All News","https://www.investing.com/rss/investing_news.rss"),
                NewsItem("Stock Market News","https://www.investing.com/rss/news_25.rss"),
                NewsItem("Cryptocurrency news","https://www.investing.com/rss/news_301.rss"),
                NewsItem("SEC Filings","https://www.investing.com/rss/news_1064.rss"),

            )
            ),
             NewsSection("MarketWatch",
                 "https://www.marketwatch.com/site/rss",
                 arrayListOf(
                 NewsItem("Top Stories","https://feeds.content.dowjones.io/public/rss/mw_topstories"),
                 NewsItem("Real-time headlines","https://feeds.content.dowjones.io/public/rss/mw_realtimeheadlines"),
                 NewsItem("Breaking News","https://feeds.content.dowjones.io/public/rss/mw_bulletins"),
                 NewsItem("Market Pulse","https://feeds.content.dowjones.io/public/rss/mw_marketpulse")
             )
             ),
             NewsSection("CNBC",
                 "https://www.cnbc.com/rss-feeds/",
                 arrayListOf(
                 NewsItem("Business","https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=10001147"),
                 NewsItem("Earnings","https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=15839135"),
                 NewsItem("Economy","https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=20910258"),
                 NewsItem("Finance","https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=10000664"),
                 NewsItem("Wealth","https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=10001054"),
                 NewsItem("Small Business","https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=44877279"),
                 NewsItem("Investing","https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=15839069"),
                 NewsItem("Financial Advisors","https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=100646281"),
                 NewsItem("Personal Finance","https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=21324812"),
             )
             ),
             NewsSection("WSJ",
                 "",
                 arrayListOf(
                 NewsItem("U.S Business","https://feeds.content.dowjones.io/public/rss/WSJcomUSBusiness"),
                 NewsItem("Market News","https://feeds.content.dowjones.io/public/rss/RSSMarketsMain"),
                 NewsItem("Economy","https://feeds.content.dowjones.io/public/rss/socialeconomyfeed"),
                 NewsItem("Personal Finance","https://feeds.content.dowjones.io/public/rss/RSSPersonalFinance")
             )
             ),
             NewsSection("CNN",
                 "",
                 arrayListOf(
                 NewsItem("Business News","http://rss.cnn.com/rss/money_latest.rss"),
                 NewsItem("Top Stories","http://rss.cnn.com/rss/money_topstories.rss"),
                 NewsItem("Economy","http://rss.cnn.com/rss/money_news_economy.rss")
             )
             ),
        )
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_view_more
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    /** handle view **/
    private fun initView() {
        viewModel.getAds(Constants.GET_ADS)
        initAdapter()
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
                        "getAds" ->{
                            hideLoading()
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

    /** handle adapter **/
    private fun initAdapter() {
        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        binding.rvAds.adapter = adsAdapter

        newsAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_news, BR.bean) { v, m, pos ->
                when (v.id) {

                }

            }
        newsAdapter.list =  newsSections
        binding.rvNews.adapter = newsAdapter
    }

    private var getList = listOf(
        "", "", "", "", ""
    )

}