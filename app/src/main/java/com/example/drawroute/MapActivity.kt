package com.example.drawroute

import com.example.drawroute.RoutesListActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.drawroute.place.Place
import com.example.drawroute.place.PlacesReader
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.ktx.addCircle
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import org.json.JSONObject
import kotlinx.coroutines.launch

class MapActivity : AppCompatActivity() {

    private val places: List<Place> by lazy {
        PlacesReader(this).read()
    }

    // [START maps_android_add_map_codelab_ktx_coroutines]
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val buttonMenu = findViewById<Button>(R.id.buttonMenu)
        val routeName = findViewById<TextView>(R.id.routeName)
        routeName.text = RoutesListActivity.trackformaps.name
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        lifecycleScope.launchWhenCreated {
            // Get map
            val googleMap = mapFragment.awaitMap()

            addClusteredMarkers(googleMap)

            // Wait for map to finish loading
            googleMap.awaitMapLoad()

            // Carregar e extrair os pontos das tracks do JSON
            val jsonString = loadDatabaseJson()
            val tracks = extractPointsFromDatabase(jsonString)
            val referencePoints = extractReferencePointsFromDatabase(jsonString)

            // Adicionar polilinhas para todas as tracks

            val tracksID = RoutesListActivity.trackformaps.id
            processTracksAndAddPolylines(googleMap, tracks)

            // Esperar o carregamento completo do mapa
            googleMap.awaitMapLoad()

            // Ensure all places are visible in the map
            val bounds = LatLngBounds.builder()
            places.forEach { bounds.include(it.latLng) }
            tracks.values.flatten().forEach { bounds.include(it) }
            referencePoints.forEach { bounds.include(it.latLng) }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))
        }

        buttonMenu.setOnClickListener {
            val intent = Intent(this, RoutesListActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    // [END maps_android_add_map_codelab_ktx_coroutines]

    /**
     * Adds markers to the map with clustering support.
     */
    private fun addClusteredMarkers(googleMap: GoogleMap) {
        // Create the ClusterManager class and set the custom renderer
        val clusterManager = ClusterManager<Place>(this, googleMap)
        clusterManager.renderer =
            PlaceRenderer(
                this,
                googleMap,
                clusterManager
            )

        // Set custom info window adapter
        clusterManager.markerCollection.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))

        // Add the places to the ClusterManager
        clusterManager.addItems(places)
        clusterManager.cluster()

        // Show polygon
        clusterManager.setOnClusterItemClickListener { item ->
            addCircle(googleMap, item)
            return@setOnClusterItemClickListener false
        }

        // When the camera starts moving, change the alpha value of the marker to translucent
        googleMap.setOnCameraMoveStartedListener {
            clusterManager.markerCollection.markers.forEach { it.alpha = 0.3f }
            clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 0.3f }
        }

        googleMap.setOnCameraIdleListener {
            // When the camera stops moving, change the alpha value back to opaque
            clusterManager.markerCollection.markers.forEach { it.alpha = 1.0f }
            clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 1.0f }

            // Call clusterManager.onCameraIdle() when the camera stops moving so that re-clustering
            // can be performed when the camera stops moving
            clusterManager.onCameraIdle()
        }
    }

    private var circle: Circle? = null

    // [START maps_android_add_map_codelab_ktx_add_circle]
    /**
     * Adds a [Circle] around the provided [item]
     */
    private fun addCircle(googleMap: GoogleMap, item: Place) {
        circle?.remove()
        circle = googleMap.addCircle {
            center(item.latLng)
            radius(70.0)
            fillColor(ContextCompat.getColor(this@MapActivity, R.color.colorPrimaryTranslucent))
            strokeColor(ContextCompat.getColor(this@MapActivity, R.color.colorPrimary))
        }
    }
    // [END maps_android_add_map_codelab_ktx_add_circle]

    private val bicycleIcon: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(this, R.color.colorPrimary)
        BitmapHelper.vectorToBitmap(this, R.drawable.ic_map_pin_24dp, color)
    }

    // [START maps_android_add_map_codelab_ktx_add_markers]
    /**
     * Adds markers to the map. These markers won't be clustered.
     */
    private fun addMarkers(googleMap: GoogleMap) {
        places.forEach { place ->
            val marker = googleMap.addMarker {
                title(place.name)
                position(place.latLng)
                icon(bicycleIcon)
            }
            // Set place as the tag on the marker object so it can be referenced within
            // MarkerInfoWindowAdapter
            marker?.tag = place
        }
    }
    // [END maps_android_add_map_codelab_ktx_add_markers]

    private fun extractPointsFromDatabase(jsonString: String): Map<Int, List<LatLng>> {


        val tracks = mutableMapOf<Int, MutableList<LatLng>>()

        // Parse o JSON fornecido
        val jsonObject = JSONObject(jsonString)
        val pointsArray = jsonObject.getJSONArray("points")

        // Itera pelos pontos na base de dados
        for (i in 0 until pointsArray.length()) {
            val point = pointsArray.optJSONObject(i) ?: continue
            val trackId = point.optInt("track", -1)
            val latitude = point.optDouble("latitude", Double.NaN)
            val longitude = point.optDouble("longitude", Double.NaN)

            if (!latitude.isNaN() && !longitude.isNaN() && trackId != -1) {
                val latLng = LatLng(latitude, longitude)
                tracks.putIfAbsent(trackId, mutableListOf())
                tracks[trackId]?.add(latLng)
            }
        }

        return tracks
    }


    private fun extractReferencePointsFromDatabase(jsonString: String): List<Place> {
        val referencePoints = mutableListOf<Place>()
        val jsonObject = JSONObject(jsonString)
        val referenceArray = jsonObject.optJSONArray("referencePoints") ?: return emptyList()

        for (i in 0 until referenceArray.length()) {
            val refPoint = referenceArray.optJSONObject(i) ?: continue
            val name = refPoint.optString("name", "Sem Nome")
            val latitude = refPoint.optDouble("latitude", Double.NaN)
            val longitude = refPoint.optDouble("longitude", Double.NaN)

            if (!latitude.isNaN() && !longitude.isNaN()) {
                referencePoints.add(Place(name, LatLng(latitude, longitude), ""))
            }
        }

        return referencePoints
    }

    private fun loadDatabaseJson(): String {
        return assets.open("drawr-840b8-default-rtdb-export.json").bufferedReader().use { it.readText() }
    }

    private fun processTracksAndAddPolylines(googleMap: GoogleMap, tracks: Map<Int, List<LatLng>>) {
        /*val colors = listOf(
            R.color.red_line,
            R.color.green_line,
            R.color.blue_line,
            R.color.colorPrimary,
            R.color.yellow_line
        )*/

        val trackID = RoutesListActivity.trackformaps.id // Obtém o ID da rota selecionada

        // Verifica se a rota com o trackID existe nos dados carregados
        val points = tracks[trackID]

        if (points != null && points.isNotEmpty()) {
            // Configura as opções da polilinha
            val polylineOptions = PolylineOptions()
                .addAll(points)
                .width(10f) // Espessura da linha
                .color(ContextCompat.getColor(this, R.color.colorPrimary))
                .geodesic(true)

            // Adiciona a polilinha no mapa
            googleMap.addPolyline(polylineOptions)
        } else {
            // Log ou mensagem de erro caso o trackID não seja encontrado
            println("Track ID $trackID não encontrado ou não possui pontos!")
        }
    }

    private fun addReferenceMarkers(googleMap: GoogleMap, referencePoints: List<Place>) {
        referencePoints.forEach { place ->
            googleMap.addMarker {
                position(place.latLng)
                title(place.name)
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            }
        }
    }

    companion object {
        val TAG = MapActivity::class.java.simpleName
    }


}
