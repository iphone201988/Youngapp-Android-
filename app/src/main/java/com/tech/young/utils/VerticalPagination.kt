package com.tech.young.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class VerticalPagination(
    private val linearLayoutManager: LinearLayoutManager,
    private val VISIBLE_THRESHOLD: Int
) : RecyclerView.OnScrollListener() {
    private var totalItemCount = 0
    var visibleItemCount = 0
    private var pastVisiblesItems = 0
    var isLoading = false
    private var onEndLessScrollListener: VerticalScrollListener? = null
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy > 0) {//check for scroll down{
            visibleItemCount = linearLayoutManager.childCount // return no of visible item in rv
            totalItemCount =
                linearLayoutManager.itemCount  // returns no of item avialable in adpater
            pastVisiblesItems =
                linearLayoutManager.findFirstVisibleItemPosition()  // return current top Most visible item position
            if (!isLoading) {
                if (visibleItemCount + pastVisiblesItems >= totalItemCount - VISIBLE_THRESHOLD) {
                    isLoading = true
                    if (onEndLessScrollListener != null)
                        onEndLessScrollListener!!.onLoadMore()
                }
            }
        }
    }

    fun setListener(onEndLessScroll: VerticalScrollListener?) {
        onEndLessScrollListener = onEndLessScroll
    }

    interface VerticalScrollListener {
        fun onLoadMore()
    }
}