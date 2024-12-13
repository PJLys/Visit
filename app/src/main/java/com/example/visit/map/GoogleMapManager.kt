package com.example.visit.map

import android.util.Log
import com.example.visit.services.permission.PermissionHandler
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

class GoogleMapManager(private val map: GoogleMap, private val permissionHandler: PermissionHandler) : MapManager {

    override fun moveCamera(location: LatLng, zoom: Float) {
        Log.d("GoogleMapManager", "Moving camera to $location with zoom $zoom")
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom))
    }

    override fun enableMyLocation(enable: Boolean) {
        try {
            if (enable) {
                if (permissionHandler.isPermissionGranted(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    map.isMyLocationEnabled = true
                    Log.d("GoogleMapManager", "MyLocation enabled")
                } else {
                    Log.e("GoogleMapManager", "Location permission not granted")
                }
            } else {
                map.isMyLocationEnabled = false
                Log.d("GoogleMapManager", "MyLocation disabled")
            }
        } catch (e: SecurityException) {
            Log.e("GoogleMapManager", "SecurityException when enabling MyLocation: ${e.message}", e)
        } catch (e: Exception) {
            Log.e("GoogleMapManager", "Unexpected error when toggling MyLocation: ${e.message}", e)
        }
    }
}
