package com.example.visit.search

import android.util.Log
import com.example.visit.visualisation.Visualiser
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.android.libraries.places.api.net.SearchNearbyResponse
import android.location.Location

class ButtonRequesterPOI(
    private val placesClient: PlacesClient,
    private val location: Location?,
    private val visualiser: Visualiser
) : RequestPOI {

    override fun requestNearbyPlaces() {
        if (location == null) {
            Log.e(TAG, "Location is null.")
            return
        }

        val placeFields = listOf(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.DISPLAY_NAME)
        val center = LatLng(location.latitude, location.longitude)
        val circle = com.google.android.libraries.places.api.model.CircularBounds.newInstance(center, 1000.0)  // 1km radius

        val includedTypes = listOf("restaurant", "cafe")
        val excludedTypes = listOf("pizza_restaurant", "american_restaurant")

        val searchNearbyRequest = SearchNearbyRequest.builder(circle, placeFields)
            .setIncludedTypes(includedTypes)
            .setExcludedTypes(excludedTypes)
            .setMaxResultCount(10)
            .build()

        placesClient.searchNearby(searchNearbyRequest)
            .addOnSuccessListener { response: SearchNearbyResponse ->
                val places = response.places
                val nearbyPlaceNames = Array(places.size) { places[it].displayName }
                val nearbyPlaceAddresses = Array(places.size) { places[it].adrFormatAddress }
                val nearbyPlaceLatLngs = Array(places.size) { places[it].location }

                visualiser.displayNearbyPlaces(nearbyPlaceNames, nearbyPlaceAddresses, nearbyPlaceLatLngs)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to get nearby places: ", exception)
            }
    }

    companion object {
        private const val TAG = "ButtonPOIRequester"
    }
}
