package com.example.drawroute.place

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*

class PlacesReader(private val context: Context) {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://drawr-840b8-default-rtdb.europe-west1.firebasedatabase.app/")
    private val myRef: DatabaseReference = database.getReference("referencePoints")

    fun read(callback: (List<Place>) -> Unit) {
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val places = mutableListOf<Place>()
                if (dataSnapshot.exists()) {
                    for (referenceSnapshot in dataSnapshot.children) {
                        val name = referenceSnapshot.child("name").getValue(String::class.java) ?: continue
                        val latitude = referenceSnapshot.child("latitude").getValue(Double::class.java) ?: continue
                        val longitude = referenceSnapshot.child("longitude").getValue(Double::class.java) ?: continue
                        val address = referenceSnapshot.child("vicinity").getValue(String::class.java) ?: continue

                        val latLng = LatLng(latitude, longitude)
                        places.add(Place(name, latLng, address))
                    }
                }
                callback(places)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Erro ao carregar dados: ${error.message}")
                callback(emptyList())
            }
        })
    }
}
