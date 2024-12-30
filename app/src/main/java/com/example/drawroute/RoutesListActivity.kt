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
import com.google.firebase.database.*

class RoutesListActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference

    private val imageList = mutableListOf<ImageData>()

    object trackformaps {
        var name: String = ""
        var id: Int = 0
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.routes_list_activity)

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        database = FirebaseDatabase.getInstance("https://drawr-840b8-default-rtdb.europe-west1.firebasedatabase.app/")
        auth = FirebaseAuth.getInstance()

        // Inicializar botões e imagem
        val routeButton = findViewById<Button>(R.id.buttonCreateRoute)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val radioButton1 = findViewById<Button>(R.id.radioButton1)
        val radioButton2 = findViewById<Button>(R.id.radioButton2)
        val radioButton3 = findViewById<Button>(R.id.radioButton3)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val logText = findViewById<TextView>(R.id.textViewLog)
        logText.visibility = View.INVISIBLE

        val defaultImageUrl = "https://webcolours.ca/wp-content/uploads/2020/10/webcolours-unknown.png"

        Glide.with(this)
            .load(defaultImageUrl)
            .into(imageView)

        // Listener para carregar tracks
        carregarTracks(radioButton1, radioButton2, radioButton3)

        // Listener para carregar imagens
        carregarImagens()

        // Configurar botões para exibir imagens
        configurarBotoesParaImagens(radioButton1, radioButton2, radioButton3, routeButton, imageView, logText)

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
        myRef = database.getReference("tracks")
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
                    trackformaps.name = tracks.getOrNull(0) ?: "Track 1 não disponível"
                    radioButton2.text = tracks.getOrNull(1) ?: "Track 2 não disponível"
                    trackformaps.name = tracks.getOrNull(1) ?: "Track 2 não disponível"
                    radioButton3.text = tracks.getOrNull(2) ?: "Track 3 não disponível"
                    trackformaps.name = tracks.getOrNull(2) ?: "Track 3 não disponível"

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
        myRef = database.getReference("referencePoints")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    imageList.clear()
                    for (referenceSnapshot in dataSnapshot.children) {
                        val name = referenceSnapshot.child("name").getValue(String::class.java) ?: "Sem Nome"
                        var fotopath = referenceSnapshot.child("fotoPath").getValue(String::class.java) ?: ""

                        // Verificar se fotopath é vazio e definir um valor padrão
                        if (fotopath.isEmpty()) {
                            fotopath = "https://static.vecteezy.com/ti/fotos-gratis/t1/22653879-fantasia-ilha-com-cachoeiras-3d-ilustracao-elementos-do-isto-imagem-mobiliado-de-nasa-generativo-ai-gratis-foto.jpg"
                        }

                        imageList.add(ImageData(name, fotopath))

                        // Log para verificar os dados carregados
                        println("Imagem carregada: $name, URL: $fotopath")
                    }
                } else {
                    println("Nenhum dado encontrado em 'referencePoints'.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Erro ao carregar imagens: ${error.message}")
            }
        })
    }

    private fun configurarBotoesParaImagens(
        radioButton1: Button,
        radioButton2: Button,
        radioButton3: Button,
        routeButton: Button,
        imageView: ImageView,
        logText: TextView
    ) {
        radioButton1.setOnClickListener {
            trackformaps.id = 1
            val imageUrl = imageList.getOrNull(0)?.fotopath ?: ""
            logText.text = imageUrl
            exibirImagem(imageUrl, routeButton, imageView)
        }

        radioButton2.setOnClickListener {
            trackformaps.id = 2
            val imageUrl = imageList.getOrNull(1)?.fotopath ?: ""
            logText.text = imageUrl
            exibirImagem(imageUrl, routeButton, imageView)
        }

        radioButton3.setOnClickListener {
            trackformaps.id = 3
            val imageUrl = imageList.getOrNull(0)?.fotopath ?: ""
            logText.text = imageUrl
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
