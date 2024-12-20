package com.example.visit.visualisation.poi

import com.google.android.gms.maps.model.LatLng

interface POIVisualiser {
    fun visualisePOIs(
        placeNames: Array<String?>,
        placeAddresses: Array<String?>?,
        placeLatLngs: Array<LatLng?>?
    )

    fun clearPOIs()
}