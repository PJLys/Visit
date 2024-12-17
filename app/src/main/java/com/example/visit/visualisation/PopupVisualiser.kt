package com.example.visit.visualisation

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class PopupVisualiser(private val context: Context, private val map: GoogleMap) : Visualiser {

    override fun visualisePOIs(
        placeNames: Array<String?>,
        placeAddresses: Array<String?>?,
        placeLatLngs: Array<LatLng?>?
    ) {
        // Convert placeNames to a non-nullable array of CharSequence
        val nonNullPlaceNames: Array<CharSequence> = placeNames
            .map { it ?: "Unknown Place" } // Replace null values with "Unknown Place"
            .toTypedArray()

        // Explicitly define the lambda type for the listener
        val listener: (DialogInterface, Int) -> Unit = { _, which ->
            val markerLatLng = placeLatLngs?.get(which)
            val markerSnippet = placeAddresses?.get(which)

            markerLatLng?.let {
                map.addMarker(
                    MarkerOptions()
                        .title(placeNames[which])
                        .position(it)
                        .snippet(markerSnippet)
                )

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
            }
        }

        AlertDialog.Builder(context)
            .setTitle("Pick a place")
            .setItems(nonNullPlaceNames, listener) // Pass the non-nullable array here
            .show()
    }

    override fun clearPOIs() {
        TODO("Not yet implemented")
    }
}
