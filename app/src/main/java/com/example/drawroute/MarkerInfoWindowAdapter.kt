package com.example.drawroute

import com.example.drawroute.place.Place


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker


class MarkerInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(marker: Marker): View? {
        // 1. Get tag
        val place = marker.tag as? Place ?: return null

        // 2. Inflate view and set title, address and rating
        val view = LayoutInflater.from(context).inflate(R.layout.marker_info_contents, null)
        view.findViewById<TextView>(R.id.text_view_title).text = place.name
        view.findViewById<TextView>(R.id.text_view_address).text = place.address


        return view
    }

    override fun getInfoWindow(marker: Marker): View? {
        // Return null to indicate that the default window (white bubble) should be used
        return null
    }
}