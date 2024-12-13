package com.example.visit.visualisation

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class RedDotVisualiser(
    private val context: Context,
    private val googleMap: GoogleMap
) : Visualiser {

    override fun visualisePOIs(
        placeNames: Array<String?>,
        placeAddresses: Array<String?>?,
        placeLatLngs: Array<LatLng?>?
    ) {
        // Clear any existing markers on the map before adding new ones
        googleMap.clear()

        // Check if POI latitudes and longitudes are valid
        placeLatLngs?.forEachIndexed { index, latLng ->
            if (latLng != null) {
                // Create red marker options for each POI location
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title(placeNames?.get(index) ?: "Unknown POI") // Set POI name as title
                    .snippet(placeAddresses?.get(index) ?: "No address available") // Set address as snippet
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) // Red color

                // Add the marker to the map
                googleMap.addMarker(markerOptions)
            }
        }
    }
}
