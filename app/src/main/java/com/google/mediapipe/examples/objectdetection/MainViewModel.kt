/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mediapipe.examples.objectdetection

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 *  This ViewModel is used to store object detector helper settings
 */
class MainViewModel : ViewModel() {
    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> get() = _tasks

    val detectedFrame: MutableLiveData<Bitmap> = MutableLiveData()

    private val _currentTask = MutableLiveData<Task?>()
    val currentTask: MutableLiveData<Task?> get() = _currentTask

    private var _delegate: Int = ObjectDetectorHelper.DELEGATE_CPU
    private var _threshold: Float =
        ObjectDetectorHelper.THRESHOLD_DEFAULT
    private var _maxResults: Int =
        ObjectDetectorHelper.MAX_RESULTS_DEFAULT
    private var _model: Int = ObjectDetectorHelper.MODEL_EFFICIENTDETV0

    val currentDelegate: Int get() = _delegate
    val currentThreshold: Float get() = _threshold
    val currentMaxResults: Int get() = _maxResults
    val currentModel: Int get() = _model

    fun setDelegate(delegate: Int) {
        _delegate = delegate
    }

    fun setThreshold(threshold: Float) {
        _threshold = threshold
    }

    fun setMaxResults(maxResults: Int) {
        _maxResults = maxResults
    }

    fun setModel(model: Int) {
        _model = model
    }

    fun setTasks(taskList: List<Task>) {
        _tasks.value = taskList
        _currentTask.value = firstAvailableTask(taskList)
    }

    fun firstAvailableTask(taskList: List<Task>): Task? {
        return taskList.firstOrNull { !it.completed }
    }

    fun markTaskAsComplete(taskId: Int) {
        val currentTasks = _tasks.value
        if (currentTasks != null) {
            // Create nova lista de tarefas
            val updatedTaskList = currentTasks.map { task ->
                if (task.id == taskId) {
                    task.copy(completed = true)
                } else {
                    task
                }
            }
            //Salvar nova lista
            _tasks.value = updatedTaskList
            _currentTask.value = firstAvailableTask(updatedTaskList)
        }
    }

    fun setCurrentTask(task: Task) {
        _currentTask.value = task
    }
}
