package com.example.taskmaster

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONObject
import taskWidget

class addTask : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        // Handle image clicks for navigation
        findViewById<ImageView>(R.id.imageView10).setOnClickListener {
            startActivity(Intent(this, taskList::class.java))
        }
        findViewById<ImageView>(R.id.imageView6).setOnClickListener {
            startActivity(Intent(this, home::class.java))
        }
        findViewById<ImageView>(R.id.imageView2).setOnClickListener {
            startActivity(Intent(this, home::class.java))
        }
        findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener {
            startActivity(Intent(this, addTask::class.java))
        }

        // Initialize views
        val editTextTaskName = findViewById<EditText>(R.id.editTextText)
        val editTextTaskDescription = findViewById<EditText>(R.id.editTextText2)
        val calendarView = findViewById<CalendarView>(R.id.calendarView3)
        val editTextTime = findViewById<EditText>(R.id.editTextTime)
        val saveButton = findViewById<Button>(R.id.button)

        var selectedDate = ""
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year) // Format date as "dd/MM/yyyy"
        }

        // Disable keyboard input for time and show time picker dialog
        editTextTime.inputType = InputType.TYPE_NULL
        editTextTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                    editTextTime.setText(formattedTime)
                },
                hour, minute, true
            )
            timePickerDialog.show()
        }

        // Save task when Save button is clicked
        saveButton.setOnClickListener {
            val taskName = editTextTaskName.text.toString()
            val taskDescription = editTextTaskDescription.text.toString()
            val taskTime = editTextTime.text.toString()

            if (taskName.isNotEmpty() && taskDescription.isNotEmpty() && selectedDate.isNotEmpty() && taskTime.isNotEmpty()) {
                saveTaskToSharedPreferences(taskName, taskDescription, selectedDate, taskTime)

                // Schedule a reminder for today's tasks
                if (selectedDate == getCurrentDate()) {
                    scheduleReminder(taskName, taskDescription, selectedDate, taskTime)
                }

                // Send broadcast to update the widget
                val intent = Intent(this, taskWidget::class.java)
                intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                sendBroadcast(intent)

                showSuccessDialog() // Show success dialog

            } else {
                showErrorDialog() // Show error dialog
            }
        }
    }

    // Function to save the task in SharedPreferences
    private fun saveTaskToSharedPreferences(taskName: String, taskDescription: String, taskDate: String, taskTime: String) {
        val sharedPreferences = getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Retrieve the current tasks JSON array from SharedPreferences
        val tasksJsonString = sharedPreferences.getString("tasks_json", "[]") // Default to empty JSON array
        val tasksJsonArray = JSONArray(tasksJsonString)

        // Create a new task JSON object
        val newTask = JSONObject().apply {
            put("id", System.currentTimeMillis().toString()) // Unique ID based on timestamp
            put("name", taskName)
            put("description", taskDescription)
            put("date", taskDate)
            put("time", taskTime)
        }

        // Add the new task to the array
        tasksJsonArray.put(newTask)

        // Save the updated task list back to SharedPreferences
        editor.putString("tasks_json", tasksJsonArray.toString())
        editor.apply()
    }

    // Function to show success dialog
    private fun showSuccessDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Success")
        alertDialog.setMessage("Task saved successfully.")
        alertDialog.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            startActivity(Intent(this, taskList::class.java))
            finish() // Close the addTask activity
        }
        alertDialog.create().show() // Show the AlertDialog
    }

    // Function to show error dialog
    private fun showErrorDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Error")
        alertDialog.setMessage("Please fill in all fields.")
        alertDialog.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        alertDialog.create().show() // Show the AlertDialog
    }

    // Function to get the current date in "dd/MM/yyyy" format
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        return String.format("%02d/%02d/%04d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
    }

    // Function to schedule a reminder
    private fun scheduleReminder(taskName: String, taskDescription: String, taskDate: String, taskTime: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, TaskReminderReceiver::class.java).apply {
            putExtra("taskName", taskName)
            putExtra("taskDescription", taskDescription)
        }

        // Parse the taskTime and set the reminder time accordingly
        val calendar = Calendar.getInstance()
        val timeParts = taskTime.split(":")
        calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
        calendar.set(Calendar.MINUTE, timeParts[1].toInt())
        calendar.set(Calendar.SECOND, 0)

        // Create a PendingIntent for the reminder
        val pendingIntent = PendingIntent.getBroadcast(
            this, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the alarm
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}
