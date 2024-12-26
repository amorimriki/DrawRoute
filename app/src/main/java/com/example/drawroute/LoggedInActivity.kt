package com.example.drawroute

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoggedInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.routes_list_activity)

        auth = FirebaseAuth.getInstance()

        val createRouteButton = findViewById<Button>(R.id.buttonRoute)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val radioButton1 = findViewById<Button>(R.id.radioButton1)
        val radioButton2 = findViewById<Button>(R.id.radioButton2)
        val radioButton3 = findViewById<Button>(R.id.radioButton3)

        radioButton1.text = "rota 1"
        radioButton2.text = "rota 2"
        radioButton3.text = "rota 3"

        createRouteButton.setOnClickListener {
            try {
                val intent = Intent(this, MapViewActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        logoutButton.setOnClickListener {
            auth.signOut() // Sign out the user
            try {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

//        setContentView(R.layout.logged_in_activity)
//
//        auth = FirebaseAuth.getInstance()
//
//        val welcomeText = findViewById<TextView>(R.id.welcomeText)
//        val logoutButton = findViewById<Button>(R.id.logoutButton)
//
//        val user = auth.currentUser
//        welcomeText.text = "Welcome, ${user?.email}!"
//
//        // Logout functionality
//        logoutButton.setOnClickListener {

//        }
    }
}
