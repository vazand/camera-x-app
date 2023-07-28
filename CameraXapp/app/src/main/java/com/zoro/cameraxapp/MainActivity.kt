package com.zoro.cameraxapp

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.zoro.cameraxapp.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    var imageCapture: ImageCapture? = null
    private val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    private val REQUEST_PERMISSION_CODE = 123

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        if (allPermissionGranted()) {
            startCamera()
            //Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            //Toast.makeText(this, " :( Permission Not Granted", Toast.LENGTH_SHORT).show()

            requestCameraPermissions()
        }
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }

        cameraExecutor = Executors.newSingleThreadExecutor()


    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG,"Now its on Onpause state")
    }


    private fun startCamera() {
        val cameraProviderFeature = ProcessCameraProvider.getInstance(this)
        cameraProviderFeature.addListener({

            val cameraProvider = cameraProviderFeature.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val preViewBuilder = Preview.Builder().build().also {
                it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
            }
            // Important step ->
            imageCapture = ImageCapture.Builder().build()

            //
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preViewBuilder,imageCapture)
            } catch (exp: Exception) {
                Log.e(TAG, "$exp")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionGranted() = ContextCompat.checkSelfPermission(
        baseContext,
        REQUIRED_PERMISSION
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestCameraPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                REQUIRED_PERMISSION
            )
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(REQUIRED_PERMISSION),
                REQUEST_PERMISSION_CODE
            )

            //Toast.makeText(this, ":(", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(REQUIRED_PERMISSION),
                REQUEST_PERMISSION_CODE
            )
            //Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()

        }
    }


    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val name = SimpleDateFormat(FILE_NAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Camera-X-app-Images")
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
                contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Image capture failed ${exception}")
                    Toast.makeText(baseContext,"Image capture failed ${exception}",Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(
                        baseContext,
                        "Image saved in and as ${outputFileResults.savedUri}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(TAG, "Image saved in and as ${outputFileResults.savedUri}")
                }
            }
        )

    }
    companion object{
        const val TAG = "MainActivity"
        const val FILE_NAME_FORMAT = "-MM-DD-HH-mm-ss-SSS"
    }

}
