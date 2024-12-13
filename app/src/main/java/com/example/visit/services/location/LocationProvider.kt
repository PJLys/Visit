package com.example.visit.services.location

import android.location.Location

interface LocationProvider {
    fun getLastKnownLocation(callback: (Location?) -> Unit)
}