package com.google.mediapipe.examples.objectdetection.fragments

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.google.mediapipe.examples.objectdetection.MainViewModel
import com.google.mediapipe.examples.objectdetection.R
import com.google.mediapipe.examples.objectdetection.databinding.FragmentObjectCompletedBinding
import com.google.mediapipe.examples.objectdetection.databinding.FragmentStartBinding

class ObjectCompletedFragment : Fragment() {
    private var _binding: FragmentObjectCompletedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentObjectCompletedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.detectedFrame.observe(viewLifecycleOwner) { bitmap ->
            displayFrame(bitmap)
        }

        viewModel.currentTask.observe(viewLifecycleOwner, Observer { task ->
            if (task != null) {
                binding.taskTextView.text = task.description
            }
        })

        binding.continueButton.setOnClickListener {
            view.findNavController().navigate(ObjectCompletedFragmentDirections.actionObjectCompletedFragmentToCameraFragment())
        }
    }

    private fun displayFrame(bitmap: Bitmap) {
        binding.imageView.setImageBitmap(bitmap)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}