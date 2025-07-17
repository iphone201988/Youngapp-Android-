package com.tech.young.base.location

import android.location.Location

interface LocationResultListener {
    fun getLocation(location: Location)
}