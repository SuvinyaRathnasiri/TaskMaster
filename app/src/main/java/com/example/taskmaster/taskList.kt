package com.example.taskmaster

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar // Import Calendar

class taskList : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<Task>()
    private val filteredTaskList = mutableListOf<Task>() // For filtered tasks
    private val timerMap = mutableMapOf<String, Long>()

    private val EDIT_TASK_REQUEST_CODE = 1

    data class Task(
        var name: String,
        var description: String,
        var date: String,
        var time: String,
        var timerDuration: Long = 0L,
        var isTimerRunning: Boolean = false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        // Set up UI elements
        val imageView8 = findViewById<ImageView>(R.id.imageView8)
        imageView8.setOnClickListener {
            startActivity(Intent(this, home::class.java))
        }

        val imageView9 = findViewById<ImageView>(R.id.imageView9)
        imageView9.setOnClickListener {
            startActivity(Intent(this, taskList::class.java))
        }

        val imageView4 = findViewById<ImageView>(R.id.imageView4)
        imageView4.setOnClickListener {
            startActivity(Intent(this, home::class.java))
        }
        val floatingActionButton3 = findViewById<FloatingActionButton>(R.id.floatingActionButton3)
        floatingActionButton3.setOnClickListener {
            startActivity(Intent(this, addTask::class.java))
        }

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.recycleView2)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the TaskAdapter with timer callback
        taskAdapter = TaskAdapter(filteredTaskList, ::onEditClick, ::onDeleteClick, ::onUpdateTimer)
        recyclerView.adapter = taskAdapter

        // Load tasks from SharedPreferences
        loadTasks()


        // Set up CalendarView listener
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            filterTasksByDate(selectedDate)
        }
    }

    private fun loadTasks() {
        val sharedPreferences = getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
        val tasksJsonString = sharedPreferences.getString("tasks_json", "[]")
        Log.d("TaskList", "Loaded JSON: $tasksJsonString")
        val tasksJsonArray = JSONArray(tasksJsonString)

        taskList.clear()
        for (i in 0 until tasksJsonArray.length()) {
            val taskJson = tasksJsonArray.getJSONObject(i)
            val task = Task(
                taskJson.getString("name"),
                taskJson.getString("description"),
                taskJson.getString("date"),
                taskJson.getString("time"),
                taskJson.optLong("timerDuration", 0L),
                taskJson.optBoolean("isTimerRunning", false)
            )
            taskList.add(task)
        }

        Log.d("TaskList", "Loaded tasks: ${taskList.map { it.name }}")

        filteredTaskList.clear()
        filteredTaskList.addAll(taskList) // Initially show all tasks
        taskAdapter.notifyDataSetChanged()

        // Call this to show all tasks initially
        filterTasksByDate("") // Show all tasks initially
    }


    private fun filterTasksByDate(selectedDate: String) {
        filteredTaskList.clear()

        Log.d("TaskList", "Filtering tasks for date: $selectedDate")

        if (selectedDate.isEmpty()) {
            filteredTaskList.addAll(taskList)
        } else {
            val filteredTasks = taskList.filter { it.date == selectedDate }
            filteredTaskList.addAll(filteredTasks)

            Log.d("TaskList", "Tasks before filtering: ${taskList.map { it.date }}")
            Log.d("TaskList", "Filtered tasks: ${filteredTasks.map { it.date }}")

            if (filteredTasks.isEmpty()) {
                Log.d("TaskList", "No tasks found for date: $selectedDate")
            }
        }

        taskAdapter.notifyDataSetChanged()
    }

    private fun onEditClick(task: Task) {
        val intent = Intent(this, editTask::class.java).apply {
            putExtra("taskName", task.name)
            putExtra("taskDescription", task.description)
            putExtra("taskDate", task.date)
            putExtra("taskTime", task.time)
        }
        startActivityForResult(intent, EDIT_TASK_REQUEST_CODE)
    }

    private fun onDeleteClick(task: Task) {
        Log.d("TaskList", "Task '${task.name}' deleted")
        val position = taskList.indexOf(task)
        if (position != -1) {
            // Remove the task from the list
            taskList.removeAt(position)
            filteredTaskList.remove(task)

            // Notify the adapter of the removed item
            taskAdapter.notifyItemRemoved(position)

            // Update SharedPreferences after notifying the adapter
            updateTasksInPreferences()

            // Show a dialog to confirm deletion
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Task Deleted")
            builder.setMessage("Task '${task.name}' has been deleted successfully.")
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        } else {
            Log.e("TaskList", "Task not found in the list!")
        }
    }

    private fun onUpdateTimer(task: Task, duration: Long, isRunning: Boolean) {
        task.timerDuration = duration
        task.isTimerRunning = isRunning
        updateTasksInPreferences()
        taskAdapter.notifyDataSetChanged()
    }

    private fun updateTasksInPreferences() {
        val sharedPreferences = getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val tasksJsonArray = JSONArray()
        for (task in taskList) {
            val taskJson = JSONObject()
            taskJson.put("name", task.name)
            taskJson.put("description", task.description)
            taskJson.put("date", task.date)
            taskJson.put("time", task.time)
            taskJson.put("timerDuration", task.timerDuration)
            taskJson.put("isTimerRunning", task.isTimerRunning)
            tasksJsonArray.put(taskJson)
        }
        editor.putString("tasks_json", tasksJsonArray.toString())
        editor.apply()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_TASK_REQUEST_CODE && resultCode == RESULT_OK) {
            val taskName = data?.getStringExtra("taskName") ?: return
            val taskDescription = data.getStringExtra("taskDescription") ?: return
            val taskDate = data.getStringExtra("taskDate") ?: return
            val taskTime = data.getStringExtra("taskTime") ?: return

            // Update the task list with the new data
            val task = taskList.find { it.name == taskName } ?: return
            task.description = taskDescription
            task.date = taskDate
            task.time = taskTime

            updateTasksInPreferences()
            filterTasksByDate("") // Reset filter to show all tasks
            loadTasks()

            // Show a confirmation dialog for task update
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Task Updated")
            builder.setMessage("Task '$taskName' has been updated successfully.")
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }
    }


    class TaskAdapter(
    private val taskList: List<taskList.Task>,
    private val onEditClick: (taskList.Task) -> Unit,
    private val onDeleteClick: (taskList.Task) -> Unit,
    private val onUpdateTimer: (taskList.Task, Long, Boolean) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

        inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val taskName: TextView = view.findViewById(R.id.task_name)
            val taskDescription: TextView = view.findViewById(R.id.task_description)
            val taskDate: TextView = view.findViewById(R.id.task_date)
            val taskTime: TextView = view.findViewById(R.id.task_time)
            val taskTimer: TextView = view.findViewById(R.id.task_timer)
            val startButton: Button = view.findViewById(R.id.start_timer)
            val pauseButton: Button = view.findViewById(R.id.pause_timer)
            val stopButton: Button = view.findViewById(R.id.stop_timer)
            val editButton: Button = view.findViewById(R.id.button5)
            val deleteButton: Button = view.findViewById(R.id.button4)

            // Timer functionality
            var handler: Handler = Handler()
            var runnable: Runnable? = null
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
            return TaskViewHolder(view)
        }

        override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
            val task = taskList[position]
            holder.taskName.text = task.name
            holder.taskDescription.text = task.description
            holder.taskDate.text = task.date
            holder.taskTime.text = task.time
            holder.taskTimer.text = formatTime(task.timerDuration)

            Log.d("TaskAdapter", "Binding task: ${task.name} at position $position")

            holder.editButton.setOnClickListener { onEditClick(task) }
            holder.deleteButton.setOnClickListener { onDeleteClick(task) }
            holder.startButton.setOnClickListener { startTimer(holder, task) }
            holder.pauseButton.setOnClickListener { pauseTimer(holder, task) }
            holder.stopButton.setOnClickListener { resetTimer(holder, task) }
        }

        override fun getItemCount(): Int = taskList.size

        private fun startTimer(holder: TaskViewHolder, task: taskList.Task) {
            holder.runnable = object : Runnable {
                override fun run() {
                    if (task.isTimerRunning) {
                        task.timerDuration += 1000
                        holder.taskTimer.text = formatTime(task.timerDuration)
                        holder.handler.postDelayed(this, 1000)
                    }
                }
            }
            holder.handler.post(holder.runnable!!)
            onUpdateTimer(task, task.timerDuration, true)
        }

        private fun pauseTimer(holder: TaskViewHolder, task: taskList.Task) {
            holder.handler.removeCallbacks(holder.runnable!!)
            // Keep the timer where it stopped
            onUpdateTimer(task, task.timerDuration, false)
        }

        private fun resetTimer(holder: TaskViewHolder, task: taskList.Task) {
            holder.handler.removeCallbacks(holder.runnable!!)
            // Reset timer duration to 0
            task.timerDuration = 0L
            holder.taskTimer.text = formatTime(task.timerDuration)
            onUpdateTimer(task, task.timerDuration, false)
        }

        private fun formatTime(timeInMillis: Long): String {
            val seconds = (timeInMillis / 1000) % 60
            val minutes = (timeInMillis / (1000 * 60)) % 60
            val hours = (timeInMillis / (1000 * 60 * 60)) % 24
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }
}













