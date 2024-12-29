package com.example.drawroute

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.logging.Log
import com.google.firebase.database.*

class RoutesListActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference

    private val imageList = mutableListOf<ImageData>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.routes_list_activity)

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        database = FirebaseDatabase.getInstance("https://drawr-840b8-default-rtdb.europe-west1.firebasedatabase.app/")
        myRef = database.getReference("tracks")

        auth = FirebaseAuth.getInstance()

        // Inicializar botões e imagem
        val routeButton = findViewById<Button>(R.id.buttonCreateRoute)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val radioButton1 = findViewById<Button>(R.id.radioButton1)
        val radioButton2 = findViewById<Button>(R.id.radioButton2)
        val radioButton3 = findViewById<Button>(R.id.radioButton3)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val logtex = findViewById<TextView>(R.id.textViewLog)
        var imageUrl = "https://drive.google.com/uc?id=1DyBMZnKF0DVEWABfzzB6CtqPJaHVJ4om"
        Glide.with(this)
            .load(imageUrl)
            .into(imageView)
        // Listener para carregar tracks
        carregarTracks(radioButton1, radioButton2, radioButton3)

        // Listener para carregar imagens
        carregarImagens()

        // Configurar botões para exibir imagens
        configurarBotoesParaImagens(radioButton1, radioButton2, radioButton3, routeButton, imageView)

        // Botão para abrir o MapActivity
        routeButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        // Botão para logout
        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
    data class ImageData(val name: String, val fotopath: String)
    private fun carregarTracks(radioButton1: Button, radioButton2: Button, radioButton3: Button) {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val tracks = mutableListOf<String>()
                    for (trackSnapshot in dataSnapshot.children) {
                        val name = trackSnapshot.child("name").getValue(String::class.java) ?: "Sem Nome"
                        val timestamp = trackSnapshot.child("timestamp").getValue(String::class.java) ?: "Sem Timestamp"
                        tracks.add("$name - $timestamp")
                    }
                    // Atualizar botões com os tracks
                    radioButton1.text = tracks.getOrNull(0) ?: "Track 1 não disponível"
                    radioButton2.text = tracks.getOrNull(1) ?: "Track 2 não disponível"
                    radioButton3.text = tracks.getOrNull(2) ?: "Track 3 não disponível"
                } else {
                    radioButton1.text = "Nenhuma track encontrada"
                    radioButton2.text = "Nenhuma track encontrada"
                    radioButton3.text = "Nenhuma track encontrada"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                radioButton1.text = "Erro ao carregar tracks"
                radioButton2.text = "Erro ao carregar tracks"
                radioButton3.text = "Erro ao carregar tracks"
            }
        })
    }

    private fun carregarImagens() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    imageList.clear()
                    for (referenceSnapshot in dataSnapshot.child("referencePoints").children) {
                        val name = referenceSnapshot.child("name").getValue(String::class.java) ?: "Sem Nome"
                        var fotopath = referenceSnapshot.child("fotoPath").getValue(String::class.java) ?: "Sem fotoPath"

                        // Verificar se fotopath é "NULL" ou vazio e definir um valor padrão
                        if (fotopath == "NULL" || fotopath.isEmpty()) {
                            fotopath = "https://static.vecteezy.com/ti/fotos-gratis/t1/22653879-fantasia-ilha-com-cachoeiras-3d-ilustracao-elementos-do-isto-imagem-mobiliado-de-nasa-generativo-ai-gratis-foto.jpg" // Substitua por uma URL de imagem padrão
                        }

                        imageList.add(ImageData(name, fotopath))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log de erro ou tratamento

            }
        })
    }

    private fun configurarBotoesParaImagens(
        radioButton1: Button,
        radioButton2: Button,
        radioButton3: Button,
        routeButton: Button,
        imageView: ImageView
    ) {
        radioButton1.setOnClickListener {
            val imageUrl = imageList.getOrNull(0)?.fotopath ?: ""
            exibirImagem(imageUrl, routeButton, imageView)
        }

        radioButton2.setOnClickListener {
            val imageUrl = imageList.getOrNull(1)?.fotopath ?: ""
            exibirImagem(imageUrl, routeButton, imageView)
        }

        radioButton3.setOnClickListener {
            val imageUrl = imageList.getOrNull(0)?.fotopath ?: ""
            exibirImagem(imageUrl, routeButton, imageView)
        }
    }

    private fun exibirImagem(imageUrl: String, routeButton: Button, imageView: ImageView) {
        routeButton.visibility = View.VISIBLE
        Glide.with(this)
            .load(imageUrl)
            .into(imageView)
    }


}
