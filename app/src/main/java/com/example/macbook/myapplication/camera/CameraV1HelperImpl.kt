package com.example.macbook.myapplication.camera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.net.Uri
import android.provider.Settings
import com.example.macbook.myapplication.model.PreviewFrame
import com.example.macbook.myapplication.util.checkCameraPermission
import com.example.macbook.myapplication.util.setOptimalPreviewSize
import com.vanniktech.rxpermission.Permission
import com.vanniktech.rxpermission.RealRxPermission
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.jetbrains.anko.toast
import java.io.IOException

@Suppress("DEPRECATION")
class CameraV1HelperImpl(val context: Context) : CameraHelper {

    private lateinit var camera: Camera
    private lateinit var backCameraInfo: Camera.CameraInfo
    private lateinit var surfaceTexture: SurfaceTexture
    private val previewFrameParams = PreviewFrame()

    private var width = 0
    private var height = 0

    private val cameraPreviewSubject = PublishSubject.create<PreviewFrame>()

    private val backCamera: Pair<Camera.CameraInfo, Int> = getBackCamera()

    private var isCameraStarted: Boolean = false
    private var isTextureReady: Boolean = false


    private fun getBackCamera(): Pair<Camera.CameraInfo, Int> {
        val cameraInfo = Camera.CameraInfo()
        val numberOfCameras = Camera.getNumberOfCameras()

        for (i in 0 until numberOfCameras) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return Pair<Camera.CameraInfo, Int>(
                    cameraInfo,
                    Integer.valueOf(i)
                )
            }
        }
        throw Exception("Back camera not found")
    }

    private fun cameraDisplayRotation() {
        val displayOrientation = (backCameraInfo.orientation - 0 + 360) % 360
        camera.setDisplayOrientation(displayOrientation)
        previewFrameParams.rotation = displayOrientation
    }

    override fun attachSurfaceTexture(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        this.surfaceTexture = surfaceTexture
        this.width = width
        this.height = height
        isTextureReady = true
        startCamera()
    }

    private fun requeatPermission(){
        val disposable = RealRxPermission.getInstance(context)
            .request(Manifest.permission.CAMERA)
            .subscribe { it ->
                when (it.state()) {
                    Permission.State.GRANTED ->
                        startCamera()
                    Permission.State.DENIED -> {
                        context.toast("Please provide camera permission")
                    }
                    Permission.State.DENIED_NOT_SHOWN -> {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", context.packageName, null)
                        intent.data = uri
                        context.startActivity(intent)

                    }
                }
            }
    }

    override fun startCamera() {
        if (!context.checkCameraPermission()) {
            requeatPermission()
            return
        }

        if (isCameraStarted || !isTextureReady) {
            return
        }

        isCameraStarted = true
        backCameraInfo = backCamera.first
        camera = Camera.open(backCamera.second)

        cameraDisplayRotation()
        camera.setPreviewCallback { data, camera ->
            data?.let { cameraPreviewSubject.onNext(previewFrameParams.copy(data = it)) }
        }

        camera.parameters.apply {
            setOptimalPreviewSize(width, height)
            focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            previewFrameParams.width = previewSize.width
            previewFrameParams.height = previewSize.height
            previewFrameParams.imageFormat = previewFormat
            camera.parameters = this
        }

        try {
            camera.setPreviewTexture(surfaceTexture)
            camera.startPreview()
        } catch (ioe: IOException) {

        }
    }

    override fun stopCamera() {
        if (isCameraStarted) {
            camera.stopPreview()
            camera.setPreviewCallback(null)
            camera.release()
            isCameraStarted = false
        }
    }

    override fun cameraFrames(): Observable<PreviewFrame> {
        return cameraPreviewSubject
    }

}