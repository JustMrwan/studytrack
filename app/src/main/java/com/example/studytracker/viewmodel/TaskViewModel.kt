package com.example.studytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.studytrack.data.Task
import com.example.studytrack.data.TaskDataStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TaskFilter { ALL, PENDING, DONE }

class TaskViewModel(private val store: TaskDataStore) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(TaskFilter.ALL)
    val selectedFilter: StateFlow<TaskFilter> = _selectedFilter.asStateFlow()

    val tasks: StateFlow<List<Task>> = combine(
        store.tasksFlow,
        _selectedFilter
    ) { allTasks, filter ->
        when (filter) {
            TaskFilter.ALL -> allTasks
            TaskFilter.PENDING -> allTasks.filter { !it.isCompleted }
            TaskFilter.DONE -> allTasks.filter { it.isCompleted }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setFilter(filter: TaskFilter) {
        _selectedFilter.value = filter
    }

    fun addTask(title: String, course: String, dueDate: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            store.addTask(
                title = title.trim(),
                course = course.trim().ifBlank { "General" },
                dueDate = dueDate.trim().ifBlank { "No deadline" }
            )
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { store.updateTask(task) }
    }

    fun toggleCompleted(task: Task) {
        viewModelScope.launch {
            store.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { store.deleteTask(task.id) }
    }
}

class TaskViewModelFactory(private val store: TaskDataStore) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(store) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}