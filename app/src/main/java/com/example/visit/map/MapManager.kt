package com.example.visit.map

import com.google.android.gms.maps.model.LatLng

interface MapManager {
    fun moveCamera(location: LatLng, zoom: Float)
    fun enableMyLocation(enable: Boolean)
}
