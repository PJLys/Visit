package com.example.visit.visualisation.movement

import android.os.Handler
import android.os.Looper
import com.example.visit.services.location.LocationProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.HeatmapTileProvider

class HeatmapVisualiser(
    private val locationProvider: LocationProvider,
    private val map: GoogleMap
): MovementVisualiserI {

    private var isTracking = false
    private val handler = Handler(Looper.getMainLooper())  // Use Looper.getMainLooper() to avoid deprecation
    private var heatmapProvider: HeatmapTileProvider? = null
    private var heatmapOverlay: com.google.android.gms.maps.model.TileOverlay? = null  // TileOverlay should be used here

    private val movementPoints: MutableList<LatLng> = mutableListOf()

    override fun start() {
        if (!isTracking) {
            isTracking = true
            simulateMovementTracking()  // Start periodic location fetches
        }
    }

    override fun stop() {
        isTracking = false
        handler.removeCallbacksAndMessages(null)  // Stop periodic location fetches
        clearMovements()  // Clear movements when stopping tracking
    }

    private fun simulateMovementTracking() {
        val updateInterval = 5000L // 5 seconds

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isTracking) {
                    locationProvider.getLastKnownLocation { location ->
                        location?.let {
                            val movementPoint = LatLng(it.latitude, it.longitude)
                            visualiseMovements(listOf(movementPoint))  // Add the movement point to the heatmap
                        }
                    }
                    handler.postDelayed(this, updateInterval)
                }
            }
        }, updateInterval)
    }

    override fun visualiseMovements(movementPoints: List<LatLng>) {
        this.movementPoints.addAll(movementPoints)

        // Update the heatmap with the new points
        if (heatmapProvider == null) {
            // Create a new HeatmapTileProvider and add it to the map
            heatmapProvider = HeatmapTileProvider.Builder()
                .data(this.movementPoints)
                .build()

            val overlayOptions = TileOverlayOptions().tileProvider(heatmapProvider!!)
            heatmapOverlay = map.addTileOverlay(overlayOptions)
        } else {
            // Safely set data for the heatmapProvider
            heatmapProvider?.setData(this.movementPoints)
        }
    }

    override fun clearMovements() {
        // Clear movement points and remove the heatmap overlay
        this.movementPoints.clear()
        heatmapOverlay?.remove()  // Remove the heatmap overlay from the map
        heatmapProvider = null
        heatmapOverlay = null
    }
}
