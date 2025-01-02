package com.example.drawroute.place

import android.content.Context
import android.util.Log
import com.example.drawroute.RoutesListActivity
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*

class PlacesReader(private val context: Context) {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://drawr-840b8-default-rtdb.europe-west1.firebasedatabase.app/")
    private val myRef: DatabaseReference = database.getReference("referencePoints")

    fun read(callback: (List<Place>) -> Unit) {
        val refpoints = RoutesListActivity.trackformaps.referencePoint.getOrNull(RoutesListActivity.trackformaps.id -1)
            ?: run {
                Log.e("PlacesReader", "No reference points found")
                callback(emptyList())
                return
            }

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val places = mutableListOf<Place>()

                for (referenceSnapshot in dataSnapshot.children) {
                    val key = referenceSnapshot.key ?: continue
                    println("Key: "+"$key "+ "refpoints: "+ "$refpoints")
                    if (refpoints.contains(key)) {
                        val name = referenceSnapshot.child("name").getValue(String::class.java) ?: continue
                        val latitude = referenceSnapshot.child("latitude").getValue(Double::class.java) ?: continue
                        val longitude = referenceSnapshot.child("longitude").getValue(Double::class.java) ?: continue
                        val address = referenceSnapshot.child("vicinity").getValue(String::class.java) ?: continue

                        val latLng = LatLng(latitude, longitude)
                        places.add(Place(name, latLng, address))
                    }
                }

                Log.d("PlacesReader", "Fetched ${places.size} places")
                callback(places)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PlacesReader", "Error loading data: ${error.message}")
                callback(emptyList())
            }
        })
    }
}
