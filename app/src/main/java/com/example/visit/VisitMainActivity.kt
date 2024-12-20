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
import com.example.visit.search.DistancePOIRequester
import com.example.visit.search.OnlinePOIRequestInterface
import com.example.visit.services.location.FusedLocationProvider
import com.example.visit.services.location.LocationProvider
import com.example.visit.services.permission.ActivityPermissionHandler
import com.example.visit.services.permission.PermissionHandler
import com.example.visit.visualisation.movement.HeatmapVisualiser
import com.example.visit.visualisation.movement.MovementVisualiserI
import com.example.visit.visualisation.poi.RedDotVisualiser
import com.example.visit.visualisation.poi.POIVisualiser
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
    private lateinit var poiRequester: OnlinePOIRequestInterface
    private var poiVisualizer: POIVisualiser? = null  // POI Visualizer for POIs
    private var movementVisualizer: MovementVisualiserI? = null  // Movement Visualizer for heatmap
    private lateinit var map: GoogleMap
    private var lastKnownLocation: Location? = null
    private var isTrackingPOIs = false  // Track whether POI tracking is active
    private var isTrackingMovements = false  // Track whether movement tracking is active

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)

        // Initialize permission handler and location provider
        permissionHandler = ActivityPermissionHandler(this)
        locationProvider = FusedLocationProvider(LocationServices.getFusedLocationProviderClient(this))

        // Initialize the POI requester (DistancePOIRequester for distance-based updates)
        poiRequester = DistancePOIRequester(Places.createClient(this), locationProvider)

        // Check permissions and set up the map
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
        mapManager = GoogleMapManager(map, permissionHandler)
        mapManager.enableMyLocation(true)

        // Initialize the POI Visualizer (RedDotVisualiser) after the map is ready
        poiVisualizer = RedDotVisualiser(map)

        // Initialize the Movement Visualizer (MovementVisualiser) after the map is ready
        movementVisualizer = HeatmapVisualiser(locationProvider, map)

        // Fetch the last known location, but don't start POI or movement tracking here
        locationProvider.getLastKnownLocation { location ->
            lastKnownLocation = location
            val targetLocation = location?.let { LatLng(it.latitude, it.longitude) } ?: defaultLocation
            mapManager.moveCamera(targetLocation, DEFAULT_ZOOM)
        }
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

    // Setup the options menu with a button for fetching POIs and starting movement tracking
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.current_place_menu, menu)
        return true
    }

    // Handle options menu item selection
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.option_start -> {
                if (!isTrackingPOIs && !isTrackingMovements) {
                    // Start both POI tracking and movement heatmap visualization
                    startTracking()
                    item.setIcon(R.drawable.ic_stop)  // Replace with actual stop icon resource
                } else {
                    // Stop both POI tracking and movement heatmap visualization
                    stopTracking()
                    item.setIcon(R.drawable.ic_play)  // Replace with actual start icon resource
                }
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    // Start tracking both POIs and movements
    private fun startTracking() {
        // Start POI tracking
        isTrackingPOIs = true
        poiRequester.startTracking(lastKnownLocation, radius = 400.0) { placeNames, placeAddresses, placeLatLngs ->
            // Feed the fetched POIs into the visualizer
            poiVisualizer?.visualisePOIs(placeNames, placeAddresses, placeLatLngs)
        }

        // Start movement tracking (simulate periodic location fetches)
        isTrackingMovements = true
        movementVisualizer?.start()  // Start visualizing movements on the heatmap
    }

    // Stop tracking both POIs and movements
    private fun stopTracking() {
        // Stop POI tracking
        isTrackingPOIs = false
        poiRequester.stopTracking()
        poiVisualizer?.clearPOIs()  // Clear visualized POIs

        // Stop movement tracking
        isTrackingMovements = false
        movementVisualizer?.stop()  // Stop the movement heatmap visualization
    }

    // Stop tracking POIs and movements when the activity is stopped
    override fun onStop() {
        super.onStop()
        stopTracking()
    }
}
