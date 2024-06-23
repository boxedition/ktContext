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
package com.google.mediapipe.examples.objectdetection.fragments

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.mediapipe.examples.objectdetection.GestureRecognizerHelper
import com.google.mediapipe.examples.objectdetection.MainViewModel
import com.google.mediapipe.examples.objectdetection.MainViewModelGesture
import com.google.mediapipe.examples.objectdetection.ObjectDetectorHelper
import com.google.mediapipe.examples.objectdetection.adapter.GestureRecognizerResultsAdapter
import com.google.mediapipe.examples.objectdetection.databinding.FragmentCameraBinding
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraFragment : Fragment(), ObjectDetectorHelper.DetectorListener,
    GestureRecognizerHelper.GestureRecognizerListener {

    private val TAG = "ObjectDetection"

    private var _fragmentCameraBinding: FragmentCameraBinding? = null

    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    //Object
    private lateinit var objectDetectorHelper: ObjectDetectorHelper
    private val viewModel: MainViewModel by activityViewModels()

    //Gesture
    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper
    private val viewModelGesture: MainViewModelGesture by activityViewModels()
    private var defaultNumResults = 1
    private val gestureRecognizerResultAdapter: GestureRecognizerResultsAdapter by lazy {
        GestureRecognizerResultsAdapter().apply {
            updateAdapterSize(defaultNumResults)
        }
    }

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.

        backgroundExecutor.execute {
            viewModel.currentTask.value?.let { currentTask ->
                when (currentTask.modelType) {
                    "OBJECT_DETECTION" -> {
                        if (objectDetectorHelper.isClosed()) {
                            objectDetectorHelper.setupObjectDetector()
                        }
                    }

                    "HAND_TRACKING" -> {
                        if (gestureRecognizerHelper.isClosed()) {
                            gestureRecognizerHelper.setupGestureRecognizer()
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // save Gesture settings
        if (this::gestureRecognizerHelper.isInitialized) {
            viewModelGesture.setMinHandDetectionConfidence(gestureRecognizerHelper.minHandDetectionConfidence)
            viewModelGesture.setMinHandTrackingConfidence(gestureRecognizerHelper.minHandTrackingConfidence)
            viewModelGesture.setMinHandPresenceConfidence(gestureRecognizerHelper.minHandPresenceConfidence)
            viewModelGesture.setDelegate(gestureRecognizerHelper.currentDelegate)

            // Close the Gesture Recognizer helper and release resources
            backgroundExecutor.execute { gestureRecognizerHelper.clearGestureRecognizer() }
        }
        // save ObjectDetector settings
        if (this::objectDetectorHelper.isInitialized) {
            viewModel.setModel(objectDetectorHelper.currentModel)
            viewModel.setDelegate(objectDetectorHelper.currentDelegate)
            viewModel.setThreshold(objectDetectorHelper.threshold)
            viewModel.setMaxResults(objectDetectorHelper.maxResults)
            // Close the object detector and release resources
            backgroundExecutor.execute { objectDetectorHelper.clearObjectDetector() }
        }

    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        // Shut down our background executor.
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(
            Long.MAX_VALUE, TimeUnit.NANOSECONDS
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)

        return fragmentCameraBinding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize our background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()

        // Create the ObjectDetectionHelper that will handle the inference
        backgroundExecutor.execute {
            viewModel.currentTask.value?.let { currentTask ->
                when (currentTask.modelType) {
                    "OBJECT_DETECTION" -> {
                        objectDetectorHelper = ObjectDetectorHelper(
                            context = requireContext(),
                            threshold = viewModel.currentThreshold,
                            currentDelegate = viewModel.currentDelegate,
                            currentModel = viewModel.currentModel,
                            maxResults = viewModel.currentMaxResults,
                            objectDetectorListener = this,
                            runningMode = RunningMode.LIVE_STREAM
                        )
                    }

                    "HAND_TRACKING" -> {
                        gestureRecognizerHelper = GestureRecognizerHelper(
                            context = requireContext(),
                            runningMode = RunningMode.LIVE_STREAM,
                            minHandDetectionConfidence = viewModelGesture.currentMinHandDetectionConfidence,
                            minHandTrackingConfidence = viewModelGesture.currentMinHandTrackingConfidence,
                            minHandPresenceConfidence = viewModelGesture.currentMinHandPresenceConfidence,
                            currentDelegate = viewModelGesture.currentDelegate,
                            gestureRecognizerListener = this
                        )
                    }
                }
            }
            // Wait for the views to be properly laid out
            fragmentCameraBinding.viewFinder.post {
                // Set up the camera and its use cases
                setUpCamera()
            }
        }


        fragmentCameraBinding.overlay.setRunningMode(RunningMode.LIVE_STREAM)
    }


    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector - makes assumption that we're only using the back camera
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation).build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer = ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888).build()
            // The analyzer can then be assigned to the instance
            .also {
                viewModel.currentTask.value?.let { currentTask ->
                    when (currentTask.modelType) {
                        "OBJECT_DETECTION" -> {
                            it.setAnalyzer(
                                backgroundExecutor, objectDetectorHelper::detectLivestreamFrame
                            )
                        }

                        "HAND_TRACKING" -> {
                            it.setAnalyzer(backgroundExecutor) { image ->
                                recognizeHand(image)
                            }
                        }
                    }
                }
            }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun recognizeHand(imageProxy: ImageProxy) {
        gestureRecognizerHelper.recognizeLiveStream(
            imageProxy = imageProxy,
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation = fragmentCameraBinding.viewFinder.display.rotation
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            //Object
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            if (errorCode == ObjectDetectorHelper.GPU_ERROR) {
                fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.setSelection(
                    ObjectDetectorHelper.DELEGATE_CPU, false
                )
            }
        }
    }

    // Update UI after objects have been detected. Extracts original image height/width
    // to scale and place bounding boxes properly through OverlayView
    override fun onResults(resultBundle: ObjectDetectorHelper.ResultBundle) {
        activity?.runOnUiThread {
            if (_fragmentCameraBinding != null) {
                fragmentCameraBinding.bottomSheetLayout.inferenceTimeVal.text =
                    String.format("%d ms", resultBundle.inferenceTime)

                // Pass necessary information to OverlayView for drawing on the canvas
                val detectionResult = resultBundle.results[0]

                //Check if there is a detectedObjects that matches current objective
                viewModel.currentTask.value?.let { currentTask ->
                    val detectedObjects = detectionResult.detections()
                    for (detection in detectedObjects) {
                        //Log.d(TAG, "Detection: $detection")
                        for (category in detection.categories()) {
                            //Log.d(TAG, "Category: $category")
                            val categoryName = category.categoryName()
                            //Log.d(TAG, "Category: $categoryName")
                            if (categoryName == currentTask.modelValue) {
                                //Log.d(TAG, "Detected object matches current task model value: $categoryName")
                                Toast.makeText(
                                    requireContext(),
                                    "Current task model value detected: $categoryName",
                                    Toast.LENGTH_SHORT
                                ).show()
                                break
                            }
                        }
                    }
                }

                if (isAdded) {
                    fragmentCameraBinding.overlay.setResults(
                        detectionResult,
                        resultBundle.inputImageHeight,
                        resultBundle.inputImageWidth,
                        resultBundle.inputImageRotation
                    )
                }

                // Force a redraw
                fragmentCameraBinding.overlay.invalidate()
            }
        }
    }

    override fun onErrorGesture(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            gestureRecognizerResultAdapter.updateResults(emptyList())
            if (errorCode == GestureRecognizerHelper.GPU_ERROR) {
                fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.setSelection(
                    GestureRecognizerHelper.DELEGATE_CPU, false
                )
            }
        }
    }

    override fun onResultsGesture(resultBundle: GestureRecognizerHelper.ResultBundle) {
        activity?.runOnUiThread {
            if (_fragmentCameraBinding != null) {
                // Show result of recognized gesture
                val gestureCategories = resultBundle.results.first().gestures()
                if (gestureCategories.isNotEmpty()) {
                    gestureRecognizerResultAdapter.updateResults(
                        gestureCategories.first()
                    )
                } else {
                    gestureRecognizerResultAdapter.updateResults(emptyList())
                }

                fragmentCameraBinding.bottomSheetLayout.inferenceTimeVal.text =
                    String.format("%d ms", resultBundle.inferenceTime)

                // Pass necessary information to OverlayView for drawing on the canvas
                fragmentCameraBinding.overlayGesture.setResults(
                    resultBundle.results.first(),
                    resultBundle.inputImageHeight,
                    resultBundle.inputImageWidth,
                    RunningMode.LIVE_STREAM
                )

                // Force a redraw
                fragmentCameraBinding.overlay.invalidate()
            }
        }
    }
}
