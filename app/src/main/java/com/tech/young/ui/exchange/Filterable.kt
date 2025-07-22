package com.tech.young.ui.exchange

import com.tech.young.data.FilterItem


interface Filterable {
    fun onFilterApplied(selectedFilter: FilterItem)
    fun onSearchQueryChanged(query: String)

}