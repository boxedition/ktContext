package com.google.mediapipe.examples.objectdetection.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.mediapipe.examples.objectdetection.R
import com.google.mediapipe.examples.objectdetection.Task

class TaskAdapter(private val onTaskClick: (Task) -> Unit) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var tasks: List<Task> = listOf()

    fun submitList(taskList: List<Task>) {
        tasks = taskList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view, onTaskClick)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int = tasks.size

    class TaskViewHolder(itemView: View, private val onTaskClick: (Task) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
        private val taskDescription: TextView = itemView.findViewById(R.id.taskDescription)
        private val taskState: TextView = itemView.findViewById(R.id.taskState)

        fun bind(task: Task) {
            taskTitle.text = task.title
            taskDescription.text = task.description
            if (task.completed == true) {
                taskState.text = "Completed"
            } else {
                taskState.text = "Not Completed"
                itemView.setOnClickListener { onTaskClick(task) }
            }
        }
    }
}