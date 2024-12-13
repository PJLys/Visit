package com.example.visit.search

import android.location.Location
import com.google.android.gms.maps.model.LatLng

interface OnlinePOIRequestInterface {

    // Start continuously fetching POIs when the user moves
    fun startTracking(location: Location?, radius: Double?, callback: (Array<String?>, Array<String?>?, Array<LatLng?>?) -> Unit)

    // Stop the continuous fetching of POIs
    fun stopTracking()
}
