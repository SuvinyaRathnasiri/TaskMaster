package com.example.taskmaster

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameEditText = findViewById<EditText>(R.id.editTextText3)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword)
        val button6 = findViewById<Button>(R.id.button6)

        val sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)

        // Check if credentials exist and pre-fill them
        val savedUsername = sharedPreferences.getString("USERNAME", "")
        val savedPassword = sharedPreferences.getString("PASSWORD", "")

        if (savedUsername != null && savedPassword != null) {
            usernameEditText.setText(savedUsername)
            passwordEditText.setText(savedPassword)
        }

        button6.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                // Save the credentials using SharedPreferences
                val editor = sharedPreferences.edit()
                editor.putString("USERNAME", username)
                editor.putString("PASSWORD", password)
                editor.apply()

                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                // Navigate to the home screen
                val intent = Intent(this, home::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
