package com.example.studytrack.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.studytrack.data.Task
import com.example.studytrack.data.TaskDataStore
import com.example.studytrack.viewmodel.TaskFilter
import com.example.studytrack.viewmodel.TaskViewModel
import com.example.studytrack.viewmodel.TaskViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(TaskDataStore(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                StudyTrackScreen(viewModel = viewModel)
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyTrackScreen(viewModel: TaskViewModel) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsState()
    val currentFilter by viewModel.selectedFilter.collectAsState()

    var editingTask by remember { mutableStateOf<Task?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Explicit Intent -> Open detail screen
    val openDetail: (Task) -> Unit = { task ->
        val intent = Intent(context, TaskDetailActivity::class.java).apply {
            putExtra("EXTRA_TITLE", task.title)
            putExtra("EXTRA_COURSE", task.course)
            putExtra("EXTRA_DUE", task.dueDate)
            putExtra("EXTRA_DONE", task.isCompleted)
        }
        context.startActivity(intent)
    }

    // Implicit Intent -> Share task with external apps
    val shareTask: (Task) -> Unit = { task ->
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "StudyTrack Task: ${task.title} (${task.course}) - Due: ${task.dueDate}"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share task via"))
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("StudyTrack") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                TaskFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = currentFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = {
                            Text(
                                filter.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    )
                }
            }

            if (tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No tasks yet. Tap + to create one.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                // Responsive Layout: 1 column on phones, 2 columns on wider screens
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isWideScreen = maxWidth > 600.dp
                    if (isWideScreen) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(tasks, key = { it.id }) { task ->
                                TaskCard(
                                    task = task,
                                    onClick = { openDetail(task) },
                                    onToggle = { viewModel.toggleCompleted(task) },
                                    onEdit = { editingTask = task },
                                    onShare = { shareTask(task) },
                                    onDelete = { viewModel.deleteTask(task) }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(tasks, key = { it.id }) { task ->
                                TaskCard(
                                    task = task,
                                    onClick = { openDetail(task) },
                                    onToggle = { viewModel.toggleCompleted(task) },
                                    onEdit = { editingTask = task },
                                    onShare = { shareTask(task) },
                                    onDelete = { viewModel.deleteTask(task) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        TaskFormDialog(
            initialTask = null,
            onDismiss = { showAddDialog = false },
            onSave = { title, course, due ->
                viewModel.addTask(title, course, due)
                showAddDialog = false
            }
        )
    }

    editingTask?.let { task ->
        TaskFormDialog(
            initialTask = task,
            onDismiss = { editingTask = null },
            onSave = { title, course, due ->
                viewModel.updateTask(task.copy(title = title, course = course, dueDate = due))
                editingTask = null
            }
        )
    }
}

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggle() })
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                Text(
                    text = "${task.course} • Due: ${task.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
            IconButton(onClick = onShare) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share"
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete"
                )
            }
        }
    }
}

@Composable
fun TaskFormDialog(
    initialTask: Task?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var course by remember { mutableStateOf(initialTask?.course ?: "") }
    var dueDate by remember { mutableStateOf(initialTask?.dueDate ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTask == null) "New Task" else "Edit Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") })
                OutlinedTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = { Text("Course (e.g., FNI-421)") })
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date") })
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, course, dueDate) },
                enabled = title.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}