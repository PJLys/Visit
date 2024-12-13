package com.example.visit.visualisation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class RedDotVisualiser(
    private val context: Context,
    private val googleMap: GoogleMap
) : Visualiser {

    // Function to create a small red dot Bitmap
    private fun createRedDot(): Bitmap {
        val diameter = 40 // Diameter of the dot in pixels
        val paint = Paint()
        paint.color = Color.RED
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL

        // Create a Bitmap with the specified size
        val bitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val radius = diameter / 2f
        // Draw a red circle (dot) on the canvas
        canvas.drawCircle(radius, radius, radius, paint)

        return bitmap
    }

    override fun visualisePOIs(
        placeNames: Array<String?>,
        placeAddresses: Array<String?>?,
        placeLatLngs: Array<LatLng?>?
    ) {
        // Clear any existing markers on the map before adding new ones
        googleMap.clear()

        // Get the red dot icon
        val redDot = createRedDot()

        // Check if POI latitudes and longitudes are valid
        placeLatLngs?.forEachIndexed { index, latLng ->
            if (latLng != null) {
                // Create a marker with the custom red dot icon
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title(placeNames?.get(index) ?: "Unknown POI") // Set POI name as title
                    .snippet(placeAddresses?.get(index) ?: "No address available") // Set address as snippet
                    .icon(BitmapDescriptorFactory.fromBitmap(redDot)) // Use the custom red dot as marker icon

                // Add the marker to the map
                googleMap.addMarker(markerOptions)
            }
        }
    }
}
