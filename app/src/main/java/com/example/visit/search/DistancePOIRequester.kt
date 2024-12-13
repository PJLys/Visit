package com.example.visit.search

import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.android.libraries.places.api.net.SearchNearbyResponse
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place

class DistancePOIRequester(
    private val placesClient: PlacesClient,
) : OnlinePOIRequestInterface {

    private var trackingLocation: Location? = null
    private var callback: ((Array<String?>, Array<String?>?, Array<LatLng?>?) -> Unit)? = null

    override fun startTracking(location: Location?, radius: Double?, onUpdateCallback: (Array<String?>, Array<String?>?, Array<LatLng?>?) -> Unit) {
        if (location == null || radius == null) {
            Log.e(TAG, "Invalid location or radius for tracking.")
            return
        }

        // Set up the tracking
        trackingLocation = location
        callback = onUpdateCallback

        // Fetch POIs for the initial location
        fetchPOIs(location, radius)

        // You can set up a handler to periodically check for location updates
        // This can be done through a location listener or a timer (for simplicity, we assume it's done in the activity).
    }

    override fun stopTracking() {
        // Stop tracking the location and clear the callback
        trackingLocation = null
        callback = null
    }

    private fun fetchPOIs(location: Location, radius: Double?) {
        // Ensure location and radius are valid
        if (location.latitude == 0.0 || location.longitude == 0.0 || radius == null) {
            Log.e(TAG, "Invalid location or radius for POI fetch.")
            return
        }

        // Define the search area using CircularBounds
        val center = LatLng(location.latitude, location.longitude)
        val circle = CircularBounds.newInstance(center, radius) // radius in meters

        // Define types to include and exclude
        val includedTypes = listOf("historical_place", "monument", "museum")
        val excludedTypes = listOf("pizza_restaurant", "american_restaurant")

        // Build the SearchNearbyRequest
        val placeFields = listOf(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LOCATION)
        val searchNearbyRequest = SearchNearbyRequest.builder(circle, placeFields)
            .setIncludedTypes(includedTypes)
            .setExcludedTypes(excludedTypes)
            .setMaxResultCount(10)
            .build()

        // Perform the search with PlacesClient
        placesClient.searchNearby(searchNearbyRequest)
            .addOnSuccessListener { response: SearchNearbyResponse ->
                // Get the list of places from the response
                val places = response.places

                // Prepare the arrays for the callback
                val placeNames = Array(places.size) { places[it].displayName }
                val placeAddresses = Array(places.size) { places[it].adrFormatAddress }
                val placeLatLngs = Array(places.size) { places[it].location }

                // Pass the data to the callback
                callback?.invoke(placeNames, placeAddresses, placeLatLngs)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to get nearby places: ", exception)
            }
    }

    companion object {
        private const val TAG = "OnlinePOIRequester"
    }
}
