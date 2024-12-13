package com.example.visit.services.location

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient

class FusedLocationProvider(private val fusedClient: FusedLocationProviderClient) : LocationProvider {
    @SuppressLint("MissingPermission")
    override fun getLastKnownLocation(callback: (Location?) -> Unit) {
        fusedClient.lastLocation.addOnCompleteListener { task ->
            callback(task.result)
        }
    }
}
