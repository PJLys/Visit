package com.example.visit.visualisation

import com.google.android.gms.maps.model.LatLng

public interface Visualiser {
    fun displayNearbyPlaces(
        placeNames: Array<String?>?,
        placeAddresses: Array<String?>?,
        placeLatLngs: Array<LatLng?>?
    )
}