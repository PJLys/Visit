package com.example.visit

import android.Manifest
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.visit.map.GoogleMapManager
import com.example.visit.map.MapManager
import com.example.visit.search.ButtonRequesterPOI
import com.example.visit.search.DistancePOIRequester
import com.example.visit.search.OnlinePOIRequestInterface
import com.example.visit.search.RequestPOIInterface
import com.example.visit.services.location.FusedLocationProvider
import com.example.visit.services.location.LocationProvider
import com.example.visit.services.permission.ActivityPermissionHandler
import com.example.visit.services.permission.PermissionHandler
import com.example.visit.visualisation.RedDotVisualiser
import com.example.visit.visualisation.Visualiser
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places

const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
const val DEFAULT_ZOOM = 15f
val defaultLocation = LatLng(37.7749, -122.4194) // Example, San Francisco

class VisitMainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var locationProvider: LocationProvider
    private lateinit var permissionHandler: PermissionHandler
    private lateinit var mapManager: MapManager
    private lateinit var visualizer: Visualiser
    private lateinit var poiRequester: RequestPOIInterface
    private lateinit var onlinePOIRequester: OnlinePOIRequestInterface
    private lateinit var map: GoogleMap
    private var lastKnownLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_maps)
        Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)

        // Initialize the permission handler and location provider
        permissionHandler = ActivityPermissionHandler(this)
        locationProvider = FusedLocationProvider(LocationServices.getFusedLocationProviderClient(this))

        // Initialize POI requester and online POI requester
        //poiRequester = ButtonRequesterPOI(Places.createClient(this))
        onlinePOIRequester = DistancePOIRequester(Places.createClient(this))

        // Request permissions if not granted yet
        if (permissionHandler.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            setupMap()
        } else {
            permissionHandler.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onStart() {
        super.onStart()
        // Initialize the map after the activity has started, avoiding early access to map fragments
        if (!::map.isInitialized) {
            setupMap()
        }
    }

    // Map setup function to load the map
    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    // Callback when the map is ready
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Initialize MapManager with the map and permissionHandler
        mapManager = GoogleMapManager(map, permissionHandler)
        mapManager.enableMyLocation(true)

        // Initialize the visualizer (Red Dot Visualiser)
        visualizer = RedDotVisualiser(this, map)

        // Fetch the current location and move the camera to it
        locationProvider.getLastKnownLocation { location ->
            lastKnownLocation = location
            val targetLocation = location?.let { LatLng(it.latitude, it.longitude) } ?: defaultLocation
            mapManager.moveCamera(targetLocation, DEFAULT_ZOOM)

            // Start tracking the movement and update POIs when the user moves
            onlinePOIRequester.startTracking(location, radius = 400.0) { names, addresses, latLngs ->
                // Visualize POIs dynamically based on the movement
                (visualizer as RedDotVisualiser).visualisePOIs(names, addresses, latLngs)
            }
        }
    }

    // Fetch nearby POIs after pressing the button in the options menu
    private fun fetchNearbyPOIs() {
        lastKnownLocation?.let {
            val location = LatLng(it.latitude, it.longitude)
            poiRequester.fetchPOIs(Location("").apply {
                latitude = location.latitude
                longitude = location.longitude
            }, radius = 400.0) { names, addresses, latLngs ->
                // Handle POI data here, e.g., log or display it on the map
                if (addresses != null && latLngs != null) {
                    for (i in names.indices) {
                        Log.d("POI", "Name: ${names[i]}, Address: ${addresses[i]}, Location: ${latLngs[i]}")
                    }

                    // Visualize POIs using RedDotVisualiser
                    (visualizer as RedDotVisualiser).visualisePOIs(names, addresses, latLngs)
                } else {
                    Log.e("POI", "Failed to fetch POI data or received null values.")
                }
            }
        } ?: Log.e("POI", "Last known location is null, cannot fetch POIs.")
    }

    // Handle permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                setupMap()
            } else {
                Log.e("Permissions", "Location permission denied.")
            }
        }
    }

    // Setup the options menu with a button for fetching POIs
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.current_place_menu, menu)
        return true
    }

    // Handle options menu item selection
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.option_get_place) {
            fetchNearbyPOIs()
        }
        return super.onOptionsItemSelected(item)
    }

    // Stop tracking POIs when the activity is stopped
    override fun onStop() {
        super.onStop()
        // Stop tracking POIs when the activity is stopped
        onlinePOIRequester.stopTracking()
    }
}
