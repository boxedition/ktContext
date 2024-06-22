/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.mediapipe.examples.objectdetection

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.mediapipe.examples.objectdetection.databinding.ActivityMainBinding
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.security.Timestamp



class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        activityMainBinding.navigation.setupWithNavController(navController)
        activityMainBinding.navigation.setOnNavigationItemReselectedListener {
            // ignore the reselection
        }

        //Simulate getting information from "Server"
        viewModel.setTasks(getTasks().tasks)
    }

    private fun getTasks(): TaskList {
        val json = """
        {
          "tasks": [
            {
              "id": 1,
              "title": "Beer4Two",
              "description": "Find 2 full beers and drink them",
              "modelType": "OBJECT_DETECTION",
              "modelValue": "cup",
              "completed": false
            },
            {
              "id": 2,
              "title": "T-Pose??",
              "description": "Find a cool bench and T-Pose in front",
              "modelType": "POSE_OBJECT_DETECTION",
              "modelValue": "??"
              "completed": false
            },
            {
              "id": 3,
              "title": "Good Job :)",
              "description": "Make a thumbs-up!!!",
              "modelType": "HAND_TRACKING",
              "modelValue": "thumbs_up",
              "completed": false
            }
          ]
        }
        """

        val jsonFormat = Json { ignoreUnknownKeys = true }
        return jsonFormat.decodeFromString(json)
    }

}
