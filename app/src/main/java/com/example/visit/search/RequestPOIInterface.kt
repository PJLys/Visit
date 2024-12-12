package com.example.visit.search

import android.location.Location
import com.google.android.gms.maps.model.LatLng

interface RequestPOIInterface {
    fun fetchPOIs(location: Location?, radius: Double?, callback: (Array<String>, Array<String?>?, Array<LatLng?>?) -> Unit)
}


