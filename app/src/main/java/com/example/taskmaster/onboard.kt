package com.example.taskmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class onboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboard)

        val button2 = findViewById<Button>(R.id.button2)
        button2.setOnClickListener{
            val intent1 = Intent( this,login::class.java)
            startActivity(intent1)
        }

        }
    }
