package com.example.studytrack.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "studytrack_prefs")

class TaskDataStore(private val context: Context) {

    private val tasksKey = stringPreferencesKey("tasks_json")

    // READ
    val tasksFlow: Flow<List<Task>> = context.dataStore.data.map { prefs ->
        decodeTasks(prefs[tasksKey])
            .sortedWith(compareBy<Task> { it.isCompleted }.thenByDescending { it.id })
    }

    // CREATE
    suspend fun addTask(title: String, course: String, dueDate: String) {
        context.dataStore.edit { prefs ->
            val current = decodeTasks(prefs[tasksKey])
            val nextId = (current.maxOfOrNull { it.id } ?: 0L) + 1L
            val updated = current + Task(
                id = nextId,
                title = title,
                course = course,
                dueDate = dueDate
            )
            prefs[tasksKey] = encodeTasks(updated)
        }
    }

    // UPDATE
    suspend fun updateTask(task: Task) {
        context.dataStore.edit { prefs ->
            val updated = decodeTasks(prefs[tasksKey]).map {
                if (it.id == task.id) task else it
            }
            prefs[tasksKey] = encodeTasks(updated)
        }
    }

    // DELETE
    suspend fun deleteTask(taskId: Long) {
        context.dataStore.edit { prefs ->
            val updated = decodeTasks(prefs[tasksKey]).filterNot { it.id == taskId }
            prefs[tasksKey] = encodeTasks(updated)
        }
    }

    private fun decodeTasks(json: String?): List<Task> {
        if (json.isNullOrBlank()) return emptyList()
        val array = JSONArray(json)
        return List(array.length()) { index ->
            val obj = array.getJSONObject(index)
            Task(
                id = obj.getLong("id"),
                title = obj.getString("title"),
                course = obj.getString("course"),
                dueDate = obj.getString("dueDate"),
                isCompleted = obj.getBoolean("isCompleted")
            )
        }
    }

    private fun encodeTasks(tasks: List<Task>): String {
        val array = JSONArray()
        tasks.forEach { task ->
            array.put(
                JSONObject().apply {
                    put("id", task.id)
                    put("title", task.title)
                    put("course", task.course)
                    put("dueDate", task.dueDate)
                    put("isCompleted", task.isCompleted)
                }
            )
        }
        return array.toString()
    }
}