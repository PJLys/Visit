package com.example.visit.visualisation.movement

import com.google.android.gms.maps.model.LatLng

interface MovementVisualiserI {
    fun visualiseMovements(movementPoints: List<LatLng>)
    fun clearMovements()
    fun start()
    fun stop()
}
