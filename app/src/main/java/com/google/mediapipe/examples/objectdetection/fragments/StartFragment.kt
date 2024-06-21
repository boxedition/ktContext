package com.google.mediapipe.examples.objectdetection.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.mediapipe.examples.objectdetection.R
import com.google.mediapipe.examples.objectdetection.databinding.FragmentStartBinding

class StartFragment : Fragment() {
    private lateinit var binding: FragmentStartBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_start,container,false)

        binding.playButton.setOnClickListener {
            //it.findNavController().navigate(StartFragmentDirections.actionStartFragmentToMapFragment())
        }

        binding.cameraButton.setOnClickListener{
            it.findNavController().navigate(StartFragmentDirections.actionStartFragmentToCameraFragment())
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
    }
}