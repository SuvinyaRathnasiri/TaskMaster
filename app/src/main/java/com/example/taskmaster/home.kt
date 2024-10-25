package com.example.taskmaster

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class home : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: taskList.TaskAdapter
    private val todayTaskList = mutableListOf<taskList.Task>()

    // Timer management
    private val timerHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize RecyclerView for today's tasks
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize TaskAdapter with proper callbacks
        taskAdapter = taskList.TaskAdapter(todayTaskList,
            onEditClick = { task -> editTask(task) },
            onDeleteClick = { task -> deleteTask(task) },
            onUpdateTimer = { task, duration, isRunning -> updateTimer(task, duration, isRunning) })
        recyclerView.adapter = taskAdapter

        // Load today's tasks
        loadTasksForToday()

        // Handle navigation buttons
        findViewById<FloatingActionButton>(R.id.floatingActionButton2).setOnClickListener {
            startActivity(Intent(this, addTask::class.java))
        }
        findViewById<ImageView>(R.id.imageView7).setOnClickListener {
            startActivity(Intent(this, taskList::class.java))
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadTasksForToday() {
        val sharedPreferences = getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
        val tasksJsonString = sharedPreferences.getString("tasks_json", "[]") ?: "[]"
        val tasksJsonArray = JSONArray(tasksJsonString)

        val todayDate = getCurrentDate()
        todayTaskList.clear()

        for (i in 0 until tasksJsonArray.length()) {
            val taskJson = tasksJsonArray.getJSONObject(i)
            val taskDate = taskJson.getString("date")

            // Check if task's date matches today's date
            if (taskDate == todayDate) {
                val task = taskList.Task(
                    taskJson.getString("name"),
                    taskJson.getString("description"),
                    taskJson.getString("date"),
                    taskJson.getString("time"),
                    taskJson.optLong("timerDuration", 0L),
                    taskJson.optBoolean("isTimerRunning", false)
                )
                todayTaskList.add(task)

                // Start timer if it's running
                if (task.isTimerRunning) {
                    startTimer(task) // Start the timer if it was running
                }

            }
        }

        taskAdapter.notifyDataSetChanged() // Refresh the RecyclerView with today's tasks
    }


    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        return dateFormat.format(calendar.time)
    }

    private fun editTask(task: taskList.Task) {
        val alertDialog = android.app.AlertDialog.Builder(this)
        alertDialog.setTitle("Edit Task")
        alertDialog.setMessage("Do you want to edit the task: ${task.name}?")

        alertDialog.setPositiveButton("Yes") { dialog, _ ->
            // Proceed to editing activity
            val intent = Intent(this, editTask::class.java).apply {
                putExtra("taskName", task.name)
                putExtra("taskDescription", task.description)
                putExtra("taskDate", task.date)
                putExtra("taskTime", task.time)
            }
            startActivityForResult(intent, EDIT_TASK_REQUEST_CODE)
            dialog.dismiss() // Close the dialog
        }

        alertDialog.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss() // Close the dialog if user cancels
        }

        alertDialog.create().show() // Show the AlertDialog
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_TASK_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.let {
                val updatedTask = taskList.Task(
                    name = it.getStringExtra("taskName") ?: "",
                    description = it.getStringExtra("taskDescription") ?: "",
                    date = it.getStringExtra("taskDate") ?: "",
                    time = it.getStringExtra("taskTime") ?: ""
                )
                updateTaskInList(updatedTask) // Update task in the list

                // Show success message after editing the task in an AlertDialog
                val alertDialog = android.app.AlertDialog.Builder(this)
                alertDialog.setTitle("Success")
                alertDialog.setMessage("Task edited successfully.")
                alertDialog.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss() // Close the dialog
                }
                alertDialog.create().show() // Show the AlertDialog
            }
        }
    }


    private fun updateTaskInList(updatedTask: taskList.Task) {
        val sharedPreferences = getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
        val tasksJsonString = sharedPreferences.getString("tasks_json", "[]") ?: "[]"
        val tasksJsonArray = JSONArray(tasksJsonString)

        for (i in 0 until tasksJsonArray.length()) {
            val taskJson = tasksJsonArray.getJSONObject(i)
            if (taskJson.getString("name") == updatedTask.name) {
                // Update the task details
                taskJson.put("description", updatedTask.description)
                taskJson.put("date", updatedTask.date)
                taskJson.put("time", updatedTask.time)
                break
            }
        }

        // Save the updated tasks back to SharedPreferences
        sharedPreferences.edit().putString("tasks_json", tasksJsonArray.toString()).apply()
        loadTasksForToday() // Reload tasks
    }

    private fun deleteTask(task: taskList.Task) {
        val alertDialog = android.app.AlertDialog.Builder(this)
        alertDialog.setTitle("Delete Task")
        alertDialog.setMessage("Are you sure you want to delete the task: ${task.name}?")

        alertDialog.setPositiveButton("Yes") { dialog, _ ->
            // Perform deletion here
            val sharedPreferences = getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
            val tasksJsonString = sharedPreferences.getString("tasks_json", "[]") ?: "[]"
            val tasksJsonArray = JSONArray(tasksJsonString)

            for (i in 0 until tasksJsonArray.length()) {
                val taskJson = tasksJsonArray.getJSONObject(i)
                if (taskJson.getString("name") == task.name) {
                    tasksJsonArray.remove(i) // Remove the task
                    break
                }
            }

            // Save the updated tasks back to SharedPreferences
            sharedPreferences.edit().putString("tasks_json", tasksJsonArray.toString()).apply()
            loadTasksForToday() // Reload tasks
            showMessage("Task deleted successfully") // Show a message
            dialog.dismiss() // Close the dialog
        }

        alertDialog.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss() // Just close the dialog without doing anything
        }

        alertDialog.create().show() // Show the AlertDialog
    }


    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateTimer(task: taskList.Task, duration: Long, isRunning: Boolean) {
        if (isRunning) {
            // Pause the timer
            task.isTimerRunning = false
            timerRunnable?.let { timerHandler.removeCallbacks(it) }
        } else {
            // Start the timer
            task.isTimerRunning = true
            task.timerDuration = duration // Set the duration before starting
            startTimer(task)
        }
        saveTaskToPreferences(task) // Save the updated state
    }

    private fun startTimer(task: taskList.Task) {
        var remainingTime = task.timerDuration // Use current duration

        timerRunnable = object : Runnable {
            @SuppressLint("NotifyDataSetChanged")
            override fun run() {
                if (remainingTime > 0) {
                    remainingTime -= 1000 // Decrease by 1 second
                    task.timerDuration = remainingTime

                    taskAdapter.notifyDataSetChanged() // Refresh the RecyclerView
                    timerHandler.postDelayed(this, 1000) // Schedule the next tick
                } else {
                    // Timer finished
                    task.isTimerRunning = false
                    task.timerDuration = 0 // Reset duration
                    taskAdapter.notifyDataSetChanged() // Refresh the RecyclerView
                    showMessage("Timer finished for task: ${task.name}") // Notify user
                }
            }
        }

        // Start the timer
        timerHandler.post(timerRunnable as Runnable) // Ensure to post the runnable to start the timer
    }

    // Consider overriding onPause() to stop timers when activity is paused
    override fun onPause() {
        super.onPause()
        // Stop all timers when the activity goes to background
        todayTaskList.forEach { task ->
            if (task.isTimerRunning) {
                task.isTimerRunning = false
                timerRunnable?.let { timerHandler.removeCallbacks(it) }
                saveTaskToPreferences(task) // Save state
            }
        }
    }


    private fun saveTaskToPreferences(task: taskList.Task) {
        val sharedPreferences = getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
        val tasksJsonString = sharedPreferences.getString("tasks_json", "[]") ?: "[]"
        val tasksJsonArray = JSONArray(tasksJsonString)

        for (i in 0 until tasksJsonArray.length()) {
            val taskJson = tasksJsonArray.getJSONObject(i)
            if (taskJson.getString("name") == task.name) {
                // Update the timer duration and running state
                taskJson.put("timerDuration", task.timerDuration)
                taskJson.put("isTimerRunning", task.isTimerRunning)
                break
            }
        }

        // Save the updated tasks back to SharedPreferences
        sharedPreferences.edit().putString("tasks_json", tasksJsonArray.toString()).apply()
    }

    companion object {
        private const val EDIT_TASK_REQUEST_CODE = 1
    }

}


