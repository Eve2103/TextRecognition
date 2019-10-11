package com.example.macbook.myapplication

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.TextureView
import com.example.macbook.myapplication.camera.CameraHelper
import com.example.macbook.myapplication.ocr.OCR
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.contentView
import org.jetbrains.anko.toast
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity() {

    private val cameraHelper:CameraHelper by inject()
    private  val ocr: OCR by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textureView.surfaceTextureListener = object: TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                return true
            }

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                cameraHelper.attachSurfaceTexture(surface, width, height)
            }

        }

        val compositeDisposable = CompositeDisposable()

        btn_record_text.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                toast("Started")
                compositeDisposable.add(cameraHelper.cameraFrames().subscribe(ocr))
                cameraHelper.cameraFrames().subscribeOn(Schedulers.io()).observeOn(Schedulers.computation())
                ocr.startTalking()
            } else {
                toast("Stopped")
                ocr.stopTalking()
                compositeDisposable.clear()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        cameraHelper.startCamera()
    }

    override fun onStop() {
        super.onStop()
        cameraHelper.stopCamera()
    }

}
