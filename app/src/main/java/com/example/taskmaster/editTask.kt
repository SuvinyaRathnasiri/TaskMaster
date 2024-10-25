package com.example.taskmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class editTask : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_task)

        val taskName = findViewById<EditText>(R.id.edit_task_name)
        val taskDescription = findViewById<EditText>(R.id.edit_task_description)
        val taskDate = findViewById<EditText>(R.id.edit_task_date)
        val taskTime = findViewById<EditText>(R.id.edit_task_time)
        val saveButton = findViewById<Button>(R.id.save_button)

        // Get task data from Intent
        taskName.setText(intent.getStringExtra("taskName"))
        taskDescription.setText(intent.getStringExtra("taskDescription"))
        taskDate.setText(intent.getStringExtra("taskDate"))
        taskTime.setText(intent.getStringExtra("taskTime"))

        saveButton.setOnClickListener {
            val updatedTask = Intent().apply {
                putExtra("taskName", taskName.text.toString())
                putExtra("taskDescription", taskDescription.text.toString())
                putExtra("taskDate", taskDate.text.toString())
                putExtra("taskTime", taskTime.text.toString())
            }
            setResult(RESULT_OK, updatedTask)
            Toast.makeText(this, "Task edited successfully!", Toast.LENGTH_LONG).show() // Show the message
            finish()
        }

    }
}



