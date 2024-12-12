package com.example.visit.visualisation

import com.google.android.gms.maps.model.LatLng

interface Visualiser {
    fun visualisePOIs(
        placeNames: Array<String?>,
        placeAddresses: Array<String?>?,
        placeLatLngs: Array<LatLng?>?
    )
}