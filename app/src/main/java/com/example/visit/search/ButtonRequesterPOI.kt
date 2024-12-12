package com.example.visit.search

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.android.libraries.places.api.net.SearchNearbyResponse
import android.location.Location
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.CircularBounds

class ButtonRequesterPOI(
    private val placesClient: PlacesClient,
) : RequestPOIInterface {

    override fun fetchPOIs(location: Location?, radius: Double?, callback: (Array<String?>, Array<String?>?, Array<LatLng?>?) -> Unit) {
        // Ensure that location and radius are valid
        if (location == null || radius == null) {
            Log.e(TAG, "Invalid location or radius.")
            return
        }

        // Define the fields to include in the response for each returned place.
        val placeFields = listOf(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LOCATION)

        // Define the search area using CircularBounds.
        val center = LatLng(location.latitude, location.longitude)
        val circle = CircularBounds.newInstance(center, radius) // radius in meters

        // Define types to include and exclude.
        val includedTypes = listOf("restaurant", "cafe")
        val excludedTypes = listOf("pizza_restaurant", "american_restaurant")

        // Build the SearchNearbyRequest.
        val searchNearbyRequest = SearchNearbyRequest.builder(circle, placeFields)
            .setIncludedTypes(includedTypes)
            .setExcludedTypes(excludedTypes)
            .setMaxResultCount(10)
            .build()

        // Perform the search with PlacesClient.
        placesClient.searchNearby(searchNearbyRequest)
            .addOnSuccessListener { response: SearchNearbyResponse ->
                // Get the list of places from the response.
                val places = response.places

                // Prepare the arrays for the callback.
                val placeNames = Array(places.size) { places[it].displayName }
                val placeAddresses = Array(places.size) { places[it].adrFormatAddress }
                val placeLatLngs = Array(places.size) { places[it].location }

                // Pass the data to the callback.
                callback(placeNames, placeAddresses, placeLatLngs)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to get nearby places: ", exception)
            }
    }

    companion object {
        private const val TAG = "ButtonRequesterPOI"
    }
}
