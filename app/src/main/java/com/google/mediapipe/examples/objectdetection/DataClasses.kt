package com.google.mediapipe.examples.objectdetection

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val modelType: String,
    val completed: Boolean
)

@Serializable
data class TaskList(
    val tasks: List<Task>
)