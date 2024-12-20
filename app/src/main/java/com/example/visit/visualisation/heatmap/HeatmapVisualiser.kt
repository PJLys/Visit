package com.example.visit.visualisation.heatmap

import com.google.android.gms.maps.model.LatLng

interface HeatmapVisualiser {
    fun visualiseMovements(movementLatLngs: List<LatLng>)
    fun clearMovements()
}
