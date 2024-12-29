package com.example.drawroute

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button

import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.drawroute.R.id
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class RoutesListActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar o Firebase Realtime Database
        setContentView(R.layout.routes_list_activity)

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        database = FirebaseDatabase.getInstance("https://drawr-840b8-default-rtdb.europe-west1.firebasedatabase.app/")
        myRef = database.getReference("tracks")
        //database.setPersistenceEnabled(false)

        // Inicializar autenticação Firebase
        auth = FirebaseAuth.getInstance()

        // Buttons

        val routeButton = findViewById<Button>(id.buttonCreateRoute)
        val logoutButton = findViewById<Button>(id.logoutButton)
        val radioButton1 = findViewById<Button>(id.radioButton1)
        val radioButton2 = findViewById<Button>(id.radioButton2)
        val radioButton3 = findViewById<Button>(id.radioButton3)

        // Images
        val imageView = findViewById<ImageView>(id.imageView)




        imageView.setImageResource(R.drawable.xmap)
        // Listener para carregar tracks
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Iterar por todas as tracks
                    val tracks = mutableListOf<String>()
                    for (trackSnapshot in dataSnapshot.children) {
                        val name = trackSnapshot.child("name").getValue(String::class.java) ?: "Sem Nome"
                        val timestamp = trackSnapshot.child("timestamp").getValue(String::class.java) ?: "Sem Timestamp"
                        tracks.add("$name - $timestamp")
                    }

                    // Atualizar os botões com as tracks disponíveis
                    radioButton1.text = tracks.getOrNull(0) ?: "Track 1 não disponível"
                    radioButton2.text = tracks.getOrNull(1) ?: "Track 2 não disponível"
                    radioButton3.text = tracks.getOrNull(2) ?: "Track 3 não disponível"
                } else {
                    // Caso não existam tracks
                    radioButton1.text = "Nenhuma track encontrada"
                    radioButton2.text = "Nenhuma track encontrada"
                    radioButton3.text = "Nenhuma track encontrada"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Mensagem de erro
                radioButton1.text = "Erro ao carregar tracks"
                radioButton2.text = "Erro ao carregar tracks"
                radioButton3.text = "Erro ao carregar tracks"
            }
        })

        radioButton1.setOnClickListener {imageView.setImageResource(R.drawable.xcnb)
            routeButton.visibility = View.VISIBLE}
        radioButton2.setOnClickListener {imageView.setImageResource(R.drawable.xulp)
            routeButton.visibility = View.VISIBLE}
        radioButton3.setOnClickListener {imageView.setImageResource(R.drawable.xcnb)
            routeButton.visibility = View.VISIBLE}



        // Change to MapViewer Layout
        routeButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        // Sign out the user
        logoutButton.setOnClickListener {
            auth.signOut()
            try {
                val intent = Intent(this, AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }

    }
}