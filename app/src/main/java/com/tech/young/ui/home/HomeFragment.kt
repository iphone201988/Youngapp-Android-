package com.tech.young.ui.home

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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
import com.tech.young.data.api.Constants
import com.tech.young.data.model.TrendingTopicApiResponse
import com.tech.young.databinding.AdsItemViewBinding
import com.tech.young.databinding.FragmentHomeBinding
import com.tech.young.databinding.HolderTrendingTopicBinding
import com.tech.young.ui.common.CommonActivity
import com.tech.young.ui.consumer_stream.ConsumerStreamActiivty
import com.tech.young.ui.streaming_activity.StreamActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private val viewModel:HomeActivityVM by viewModels()


    private lateinit var adapterTrending: SimpleRecyclerViewAdapter<TrendingTopicApiResponse.Data.Topic, HolderTrendingTopicBinding>
    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()

        viewModel.getTrendingTopics(Constants.GET_TRENDING_TOPICS)
        // observer
        initObserver()
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
        setupTickerRecycler()

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
                    val intent = Intent(requireContext(), StreamActivity::class.java)
                    startActivity(intent)

//                    val intent=Intent(requireContext(),CommonActivity::class.java)
//                    intent.putExtra("from","view_more")
//                    startActivity(intent)
                }
                R.id.tvSmallView->{
                    val intent = Intent(requireContext(), ConsumerStreamActiivty::class.java)
                    startActivity(intent)
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
                            val myDataModel : TrendingTopicApiResponse ? = BindingUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.data != null){
                                    if (myDataModel.data?.topics != null){
                                        adapterTrending.list = myDataModel.data?.topics
                                    }

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

    fun setupTickerRecycler() {
        val rvTicker = binding.rvTicker
        val stockList = listOf(
            StockItem("AAPL", "+1.23%", true),
            StockItem("TSLA", "-2.45%", false),
            StockItem("BTC", "-2.45%", false),
            StockItem("TSLA", "-2.45%", false),
            StockItem("BTC", "+0.78%", true),
            StockItem("BTC", "-2.45%", false),
            StockItem("AAPL", "-2.45%", false),

            )
        val adapter = TickerAdapter(stockList)
        rvTicker.adapter = adapter
        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        rvTicker.layoutManager = layoutManager

        // Auto-scroll continuously
        val scrollSpeed = 2
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                rvTicker.scrollBy(scrollSpeed, 0)
                handler.postDelayed(this, 30)
            }
        }
        handler.post(runnable)
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


        binding.rvNews.layoutManager = layoutManager
        binding.rvNews.setHasFixedSize(true)
        binding.rvNews.adapter = adapter
        binding.rvNews.itemAnimator = null

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
    private lateinit var adsAdapter: SimpleRecyclerViewAdapter<String, AdsItemViewBinding>
    private fun initAdapter() {
        adsAdapter = SimpleRecyclerViewAdapter(R.layout.ads_item_view, BR.bean) { v, m, pos ->
            when (v.id) {

            }
        }
        adsAdapter.list = getList
        binding.rvAds.adapter = adsAdapter
    }

    private var getList = listOf(
        "", "", "", "", ""
    )

}