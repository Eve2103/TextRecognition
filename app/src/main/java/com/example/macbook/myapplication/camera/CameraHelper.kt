package com.example.macbook.myapplication.camera

import android.graphics.SurfaceTexture
import com.example.macbook.myapplication.model.PreviewFrame
import io.reactivex.Observable

interface CameraHelper {

    fun attachSurfaceTexture(surfaceTexture: SurfaceTexture, width: Int, height: Int)

    fun startCamera()

    fun stopCamera()

    fun cameraFrames(): Observable<PreviewFrame>

}