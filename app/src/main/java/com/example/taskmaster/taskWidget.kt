import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.taskmaster.MainActivity
import com.example.taskmaster.R
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class taskWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        updateWidgetPeriodically(context, appWidgetIds)
    }

    private fun updateWidgetPeriodically(context: Context, appWidgetIds: IntArray) {
        val intent = Intent(context, taskWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Update every hour (3600000 milliseconds)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), 3600000, pendingIntent)
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val remoteViews = RemoteViews(context.packageName, R.layout.task_widget)

        val sharedPreferences = context.getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
        val tasksJsonString = sharedPreferences.getString("tasks_json", "[]")

        Log.d("Widget", "Tasks JSON: $tasksJsonString")

        val tasksJsonArray = JSONArray(tasksJsonString)

        // Get today's date
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val todayDate = dateFormat.format(Calendar.getInstance().time)

        // Filter tasks for today
        val tasksForToday = StringBuilder()
        for (i in 0 until tasksJsonArray.length()) {
            val taskJson = tasksJsonArray.getJSONObject(i)
            val taskDate = taskJson.getString("date")

            if (taskDate == todayDate) {
                tasksForToday.append(taskJson.getString("name")).append("\n")
            }
        }

        Log.d("Widget", "Tasks for Today: $tasksForToday")

        val tasksText = if (tasksForToday.isEmpty()) {
            "No tasks for today"
        } else {
            tasksForToday.toString()
        }

        remoteViews.setTextViewText(R.id.textView19, tasksText)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews.setOnClickPendingIntent(R.id.openWebButton, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }
}




