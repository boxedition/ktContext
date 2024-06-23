package com.google.mediapipe.examples.objectdetection.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.mediapipe.examples.objectdetection.MainViewModel
import com.google.mediapipe.examples.objectdetection.R
import com.google.mediapipe.examples.objectdetection.adapter.TaskAdapter
import com.google.mediapipe.examples.objectdetection.databinding.FragmentTasksBinding

class TasksFragment : Fragment() {
    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskAdapter = TaskAdapter { task ->
            //Toast.makeText(context, "Current task set to: ${task.title}", Toast.LENGTH_SHORT).show()
            viewModel.setCurrentTask(task)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = taskAdapter

        viewModel.tasks.observe(viewLifecycleOwner, Observer { tasks ->
            taskAdapter.submitList(tasks)
        })

        viewModel.currentTask.observe(viewLifecycleOwner, Observer { task ->
            // Update the UI to show the current task, e.g., update a TextView
            binding.currentTaskTextView.text = "Current Task: ${task?.title}"
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}