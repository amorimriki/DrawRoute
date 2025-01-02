package com.example.drawroute

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AuthActivity : AppCompatActivity() {
    private var auth: FirebaseAuth? = null
    private var userRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        val database =
            FirebaseDatabase.getInstance("https://drawr-840b8-default-rtdb.europe-west1.firebasedatabase.app/")
        auth = FirebaseAuth.getInstance()
        userRef = database.getReference("users")

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val fastLoginButton = findViewById<Button>(R.id.buttonFastLogin)

        // Quick login action
        fastLoginButton.setOnClickListener { v: View? ->
            loginUser(
                "a22007546@alunos.ulht.pt",
                "123456"
            )
        }

        // Register button action
        registerButton.setOnClickListener { v: View? ->
            val email = emailInput.text.toString().trim { it <= ' ' }
            val password = passwordInput.text.toString().trim { it <= ' ' }
            registerUser(email, password)
        }

        // Login button action
        loginButton.setOnClickListener { v: View? ->
            val email = emailInput.text.toString().trim { it <= ' ' }
            val password = passwordInput.text.toString().trim { it <= ' ' }
            loginUser(email, password)
        }
    }

    private fun registerUser(email: String, password: String) {
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isValidPassword(password)) {
            Toast.makeText(
                this,
                "Password must be at least 6 characters, include letters, numbers, and special characters",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        auth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
            this
        ) { task: Task<AuthResult?> ->
            if (task.isSuccessful) {
                FirebaseAuth.getInstance().currentUser!!
                    .reload() // Ensure currentUser is updated
                val userId = auth!!.currentUser!!.uid
                showNameInputDialog(userId, email)
            } else {
                Log.e("AuthTest", "Registration error: " + task.exception)
                Toast.makeText(
                    this,
                    "Registration failed: " + task.exception!!.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showNameInputDialog(userId: String, email: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Register Name")
        builder.setMessage("Please enter your name:")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog: DialogInterface?, which: Int ->
            var name = input.text.toString().trim { it <= ' ' }
            if (name.isEmpty()) {
                name = "User"
            }
            saveUserToDatabase(userId, name, email)
        }

        builder.setNegativeButton("Cancel") { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
            Toast.makeText(this, "Registration cancelled.", Toast.LENGTH_SHORT).show()
        }

        builder.show()
    }

    private fun saveUserToDatabase(userId: String, name: String, email: String) {
        userRef!!.child(userId).setValue(User(name, email))
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "User registered successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.e("AuthTest", "Database error: " + task.exception)
                    Toast.makeText(
                        this,
                        "Error saving user: " + task.exception!!.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }.addOnFailureListener { e: Exception ->
                Log.e("AuthTest", "Failure: " + e.message)
                Toast.makeText(
                    this,
                    "Error saving to database: " + e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun loginUser(email: String, password: String) {
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        auth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener(
            this
        ) { task: Task<AuthResult?> ->
            if (task.isSuccessful) {
                val intent = Intent(
                    this,
                    RoutesListActivity::class.java
                )
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                Log.e("AuthTest", "Login error: " + task.exception)
                Toast.makeText(
                    this,
                    "Login failed: " + task.exception!!.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun isValidEmail(email: String?): Boolean {
        return email != null && !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        // Senha deve ter pelo menos 6 caracteres, uma letra, um número e um caracter especial
        val passwordRegex =
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\\\$!%*?&])[A-Za-z\\d@\\\$!%*?&]{6,}\$".toRegex()
        return password.isNotEmpty() && password.matches(passwordRegex)
    }

    // User class for database
    private class User {
        var name: String? = null
        var email: String? = null

        constructor(name: String?, email: String?) {
            this.name = name
            this.email = email
        }

        constructor()
    }
}