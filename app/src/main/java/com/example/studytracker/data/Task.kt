package com.example.studytrack.data

data class Task(
    val id: Long = 0L,
    val title: String,
    val course: String,
    val dueDate: String,
    val isCompleted: Boolean = false
)