package com.example.visit.visualisation.heatmap

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng

class MovementVisualiser(private val context: Context, private val map: GoogleMap) : HeatmapVisualiser {

    private var heatmapProvider: HeatmapTileProvider? = null
    private var heatmapOverlay: TileOverlay? = null

    override fun visualiseMovements(movementLatLngs: List<LatLng>) {
        val weightedLatLngs = movementLatLngs.map { WeightedLatLng(it) }

        if (heatmapProvider == null) {
            heatmapProvider = HeatmapTileProvider.Builder()
                .weightedData(weightedLatLngs)
                .build()
        } else {
            heatmapProvider?.setWeightedData(weightedLatLngs)
        }

        if (heatmapOverlay == null) {
            heatmapOverlay = heatmapProvider?.let {
                TileOverlayOptions().tileProvider(
                    it
                )
            }?.let { map.addTileOverlay(it) }
        } else {
            heatmapOverlay?.clearTileCache()
        }
    }

    override fun clearMovements() {
        heatmapOverlay?.remove()
        heatmapOverlay = null
        heatmapProvider = null
    }
}
