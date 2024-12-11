package com.example.visit

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.android.libraries.places.api.net.SearchNearbyResponse
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

import com.google.android.gms.maps.model.CameraPosition
import com.example.visit.visualisation.PopupVisualiser
import com.example.visit.visualisation.Visualiser

class VisitMainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var map: GoogleMap? = null
    private var cameraPosition: CameraPosition? = null

    // The entry point to the Places API.
    private lateinit var placesClient: PlacesClient

    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var locationPermissionGranted = false
    private var lastKnownLocation: Location? = null
    private var nearbyPlaceNames: Array<String?> = arrayOfNulls(0)
    private var nearbyPlaceAddresses: Array<String?> = arrayOfNulls(0)
    private var nearbyPlaceLatLngs: Array<LatLng?> = arrayOfNulls(0)
    private lateinit var visualiser: Visualiser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION, Location::class.java)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION, CameraPosition::class.java)
        }

        setContentView(R.layout.activity_maps)

        // Initialize the Places SDK
        Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(this)

        // Initialize the FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        map?.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.current_place_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.option_get_place) {
            searchNearbyPlaces()
        }
        return true
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map

        // Initialize the Visualiser (PopupVisualiser in this case)
        visualiser = PopupVisualiser(this, map)

        getLocationPermission()
        updateLocationUI()

        getDeviceLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        lastKnownLocation = task.result
                        lastKnownLocation?.let {
                            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(it.latitude, it.longitude), DEFAULT_ZOOM.toFloat()))
                        }
                    } else {
                        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true
            }
        }
        updateLocationUI()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun searchNearbyPlaces() {
        if (map == null) {
            Log.e(TAG, "Map is not ready.")
            return
        }

        if (locationPermissionGranted) {
            lastKnownLocation?.let { location ->
                val placeFields = listOf(Place.Field.ID, Place.Field.DISPLAY_NAME)
                val center = LatLng(location.latitude, location.longitude)
                val circle = CircularBounds.newInstance(center, 1000.0)  // Search within 1000m radius

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
                        nearbyPlaceNames = Array(places.size) { places[it].displayName }
                        nearbyPlaceAddresses = Array(places.size) { places[it].adrFormatAddress }
                        nearbyPlaceLatLngs = Array(places.size) { places[it].location }

                        visualiser.displayNearbyPlaces(nearbyPlaceNames, nearbyPlaceAddresses, nearbyPlaceLatLngs)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Failed to get nearby places: ", exception)
                    }
            }
        } else {
            getLocationPermission()
        }
    }

    private fun updateLocationUI() {
        if (map == null) return

        try {
            if (locationPermissionGranted) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    companion object {
        private const val TAG = "VisitMainActivity"
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
    }
}
