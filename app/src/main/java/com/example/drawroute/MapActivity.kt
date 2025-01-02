package com.example.drawroute

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.drawroute.place.Place
import com.example.drawroute.place.PlaceRenderer
import com.example.drawroute.place.PlacesReader
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.ktx.addCircle
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import kotlinx.coroutines.launch

class MapActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference

    private var tracks = mutableMapOf<Int, MutableList<LatLng>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val buttonMenu = findViewById<Button>(R.id.buttonMenu)
        val routeName = findViewById<TextView>(R.id.routeName)
        routeName.text = RoutesListActivity.trackformaps.name.getOrNull(RoutesListActivity.trackformaps.id - 1) ?: "Sem Nome"

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        database = FirebaseDatabase.getInstance("https://drawr-840b8-default-rtdb.europe-west1.firebasedatabase.app/")

        lifecycleScope.launch {
            val googleMap = mapFragment.awaitMap()
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.isIndoorEnabled = false
            googleMap.isTrafficEnabled = false
            googleMap.awaitMapLoad()
            addClusteredMarkers(googleMap)
            extractPointsFromDatabase { points ->
                processTracksAndAddPolylines(googleMap, points)
            }
        }

        buttonMenu.setOnClickListener {
            val intent = Intent(this, RoutesListActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun extractPointsFromDatabase(callback: (Map<Int, MutableList<LatLng>>) -> Unit) {
        myRef = database.getReference("points")
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val extractedTracks = mutableMapOf<Int, MutableList<LatLng>>()
                if (dataSnapshot.exists()) {
                    for (referenceSnapshot in dataSnapshot.children) {
                        val latitude = referenceSnapshot.child("latitude").getValue(Double::class.java) ?: continue
                        val longitude = referenceSnapshot.child("longitude").getValue(Double::class.java) ?: continue
                        val trackId = referenceSnapshot.child("track").getValue(Int::class.java) ?: continue

                        val latLng = LatLng(latitude, longitude)
                        extractedTracks.putIfAbsent(trackId, mutableListOf())
                        extractedTracks[trackId]?.add(latLng)
                    }
                }
                callback(extractedTracks)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Erro ao carregar os dados: ${error.message}")
                callback(emptyMap())
            }
        })
    }

    private fun processTracksAndAddPolylines(
        googleMap: GoogleMap,
        points: Map<Int, MutableList<LatLng>>
    ) {
        tracks.clear()
        tracks.putAll(points)

        val trackID = RoutesListActivity.trackformaps.id
        val trackPoints = tracks[trackID]

        if (!trackPoints.isNullOrEmpty()) {


            for (i in 0 until trackPoints.size -1) {
                if (i == trackPoints.lastIndex -3) {
                    break
                }
                googleMap.addPolyline(
                    PolylineOptions()
                        .add(trackPoints[i], trackPoints[i + 1])
                        .width(10f)
                        .color(ContextCompat.getColor(this, R.color.colorPrimary))
                        .geodesic(true)
                )
            }




            // Mover a câmera para o primeiro ponto
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(trackPoints.first(), 15f))
        } else {
            println("Track ID $trackID não encontrado ou não possui pontos!")
        }
    }



    private fun addClusteredMarkers(googleMap: GoogleMap) {
        val clusterManager = ClusterManager<Place>(this, googleMap)
        clusterManager.renderer = PlaceRenderer(this, googleMap, clusterManager)

        clusterManager.markerCollection.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))

        // Busca os dados da Firebase e adiciona ao cluster
        PlacesReader(this).read { places ->
            clusterManager.addItems(places)
            clusterManager.cluster()

            clusterManager.setOnClusterItemClickListener { item ->
                addCircle(googleMap, item)
                return@setOnClusterItemClickListener false
            }

            googleMap.setOnCameraMoveStartedListener {
                clusterManager.markerCollection.markers.forEach { it.alpha = 0.3f }
                clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 0.3f }
            }

            googleMap.setOnCameraIdleListener {
                clusterManager.markerCollection.markers.forEach { it.alpha = 1.0f }
                clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 1.0f }
                clusterManager.onCameraIdle()
            }
        }
    }

   /* private fun addClusteredMarkers(googleMap: GoogleMap) {
        val clusterManager = ClusterManager<Place>(this, googleMap)
        clusterManager.renderer =
            PlaceRenderer(
                this,
                googleMap,
                clusterManager
            )

        clusterManager.markerCollection.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))

//        clusterManager.addItems(places)
        clusterManager.cluster()

        clusterManager.setOnClusterItemClickListener { item ->
            addCircle(googleMap, item)
            return@setOnClusterItemClickListener false
        }

        googleMap.setOnCameraMoveStartedListener {
            clusterManager.markerCollection.markers.forEach { it.alpha = 0.3f }
            clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 0.3f }
        }

        googleMap.setOnCameraIdleListener {
            clusterManager.markerCollection.markers.forEach { it.alpha = 1.0f }
            clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 1.0f }
            clusterManager.onCameraIdle()
        }
    }*/

    private var circle: Circle? = null

    private fun addCircle(googleMap: GoogleMap, item: Place) {
        circle?.remove()
        circle = googleMap.addCircle {
            center(item.latLng)
            radius(70.0)
            fillColor(ContextCompat.getColor(this@MapActivity, R.color.colorPrimaryTranslucent))
            strokeColor(ContextCompat.getColor(this@MapActivity, R.color.colorPrimary))
        }
    }

    companion object {
        val TAG = MapActivity::class.java.simpleName
    }
}
