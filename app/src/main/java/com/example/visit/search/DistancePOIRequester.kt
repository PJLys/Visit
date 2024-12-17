package com.example.visit.search

import android.location.Location
import android.util.Log
import com.example.visit.services.location.LocationProvider
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.android.libraries.places.api.net.SearchNearbyResponse

class DistancePOIRequester(
    private val placesClient: PlacesClient,
    private val locationProvider: LocationProvider // Injected LocationProvider
) : OnlinePOIRequestInterface {

    private var lastKnownLocation: Location? = null
    private var trackingRadius: Double = 0.0
    private var trackingCallback: ((Array<String?>, Array<String?>?, Array<LatLng?>?) -> Unit)? = null

    private var isTracking = false // Flag to manage start/stop state

    override fun startTracking(
        location: Location?,
        radius: Double?,
        callback: (Array<String?>, Array<String?>?, Array<LatLng?>?) -> Unit
    ) {
        if (location == null || radius == null) {
            Log.e(TAG, "Invalid location or radius for tracking.")
            return
        }

        // Initialize tracking state
        lastKnownLocation = location
        trackingRadius = radius
        trackingCallback = callback
        isTracking = true

        Log.d(TAG, "Tracking started. Radius: $radius meters.")

        // Immediately fetch POIs for the initial location
        fetchPOIs(location)

        // Start location updates
        updateLocation()
    }

    override fun stopTracking() {
        isTracking = false
        trackingCallback = null
        Log.d(TAG, "Tracking stopped.")
    }

    // Private method to handle location updates and check the distance threshold
    private fun updateLocation() {
        // Fetch the last known location
        locationProvider.getLastKnownLocation { newLocation ->
            if (isTracking) {
                newLocation?.let {
                    handleLocationUpdate(it)
                }
            }
        }
    }

    // Handle location update and distance check
    private fun handleLocationUpdate(newLocation: Location) {
        val previousLocation = lastKnownLocation ?: return
        val distance = previousLocation.distanceTo(newLocation)

        Log.d(TAG, "Distance moved: $distance meters.")

        if (distance >= DISTANCE_THRESHOLD_METERS) {
            Log.d(TAG, "Threshold reached ($DISTANCE_THRESHOLD_METERS meters). Fetching POIs...")
            lastKnownLocation = newLocation
            fetchPOIs(newLocation)
        }
    }

    // Fetch POIs for the current location
    private fun fetchPOIs(location: Location) {
        val center = LatLng(location.latitude, location.longitude)
        val circle = CircularBounds.newInstance(center, trackingRadius)

        val includedTypes = listOf("historical_place", "monument", "museum")
        val excludedTypes = listOf("pizza_restaurant", "american_restaurant")

        val placeFields = listOf(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LOCATION)
        val request = SearchNearbyRequest.builder(circle, placeFields)
            .setIncludedTypes(includedTypes)
            .setExcludedTypes(excludedTypes)
            .setMaxResultCount(10)
            .build()

        placesClient.searchNearby(request)
            .addOnSuccessListener { response: SearchNearbyResponse ->
                val places = response.places
                val placeNames = Array(places.size) { places[it].displayName }
                val placeAddresses = Array(places.size) { places[it].adrFormatAddress }
                val placeLatLngs = Array(places.size) { places[it].location }

                trackingCallback?.invoke(placeNames, placeAddresses, placeLatLngs)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to fetch POIs", exception)
            }
    }

    companion object {
        private const val TAG = "DistancePOIRequester"
        private const val DISTANCE_THRESHOLD_METERS = 20.0 // Movement threshold in meters
    }
}
