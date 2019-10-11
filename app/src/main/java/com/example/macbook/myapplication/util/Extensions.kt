package com.example.macbook.myapplication.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.support.v4.content.ContextCompat

fun Camera.Parameters.setOptimalPreviewSize(width: Int, height: Int) {
    val ASPECT_TOLERANCE = 0.5
    val targetRatio = height.toDouble() / width.toDouble()

    var optimalSize: Camera.Size = supportedPreviewSizes[0]
    var minDiff = java.lang.Double.MAX_VALUE

    val targetHeight = height

    for (size in supportedPreviewSizes) {
        val ratio = size.width.toDouble() / size.height.toDouble()
        if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
        if (Math.abs(ratio - targetRatio) < minDiff) {
            optimalSize = size
            minDiff = Math.abs(ratio - targetRatio)
        }
    }

    setPreviewSize(optimalSize.width, optimalSize.height)

}

fun Context.checkCameraPermission() : Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}