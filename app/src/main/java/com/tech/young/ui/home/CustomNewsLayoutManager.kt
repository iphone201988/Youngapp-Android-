package com.tech.young.ui.home

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

//


class CustomNewsLayoutManager : RecyclerView.LayoutManager() {

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        detachAndScrapAttachedViews(recycler)
        if (itemCount == 0 || state.isPreLayout) return

        val parentWidth = width
        val leftColumnWidth = parentWidth / 2
        val rightColumnWidth = parentWidth / 2

        var leftTop = 0
        var rightTop = 0

        for (i in 0 until itemCount) {
            val view = recycler.getViewForPosition(i)
            addView(view)

            val isLeft = i % 2 == 0  // Even positions go to left (0, 2, etc.)
            val width = if (isLeft) leftColumnWidth else rightColumnWidth
            val height = if (isLeft) 500 else 250
            val left = if (isLeft) 0 else leftColumnWidth
            val top = if (isLeft) leftTop else rightTop

            // Measure view exactly
            val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            view.measure(widthSpec, heightSpec)

            // Layout the child
            layoutDecorated(view, left, top, left + width, top + height)

            // Update top offset
            if (isLeft) {
                leftTop += height
            } else {
                rightTop += height
            }
        }
    }

    override fun canScrollVertically(): Boolean = false // Optional: make true for scrollable layout
}





