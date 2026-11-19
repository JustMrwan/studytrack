package com.example.studytrack.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class TaskDetailActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Untitled"
        val course = intent.getStringExtra("EXTRA_COURSE") ?: "General"
        val due = intent.getStringExtra("EXTRA_DUE") ?: "No deadline"
        val isDone = intent.getBooleanExtra("EXTRA_DONE", false)

        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Task Details") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(title, style = MaterialTheme.typography.headlineSmall)
                        Text("Course: $course", style = MaterialTheme.typography.bodyLarge)
                        Text("Deadline: $due", style = MaterialTheme.typography.bodyLarge)
                        AssistChip(
                            onClick = {},
                            label = { Text(if (isDone) "Completed" else "Pending") }
                        )
                    }
                }
            }
        }
    }
}