package com.example.macbook.myapplication.ocr

import android.graphics.ImageFormat
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.macbook.myapplication.model.PreviewFrame
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer

class FirebaseOCRImpl(private val tts: TextToSpeech, private val detector: FirebaseVisionTextRecognizer) : OCR {

    private var shouldIgnore = true
    private var interrupt = false

    private var firebaseOCRTask: Task<FirebaseVisionText>? = null

    override fun accept(previewFrame: PreviewFrame) {
        if (shouldIgnore)
            return

        val orientation = when (previewFrame.rotation) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> {
                FirebaseVisionImageMetadata.ROTATION_0
            }
        }

        val imageFormat = when (previewFrame.imageFormat) {
            ImageFormat.NV21 -> FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21
            ImageFormat.YV12 -> FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12
            else -> {
                FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21
            }
        }

        val metadata = FirebaseVisionImageMetadata.Builder()
            .setWidth(previewFrame.width)
            .setHeight(previewFrame.height)
            .setFormat(imageFormat)
            .setRotation(orientation)
            .build()

        val image = FirebaseVisionImage.fromByteArray(previewFrame.data, metadata)
        shouldIgnore = true

        firebaseOCRTask = detector.processImage(image).also {

            it.addOnSuccessListener { firebaseVisionText ->

                if (firebaseVisionText.text.isBlank()||interrupt) {
                    shouldIgnore = false
                    interrupt = false
                    return@addOnSuccessListener
                }

                tts.speak(
                    firebaseVisionText.text,
                    TextToSpeech.QUEUE_ADD,
                    hashMapOf(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID to "messageID")
                )
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String?) {
                        shouldIgnore = false
                    }

                    override fun onError(utteranceId: String?) {}

                    override fun onStart(utteranceId: String?) {}
                })
            }
            it.addOnFailureListener {
                shouldIgnore = false
            }
        }
    }

    override fun stopTalking() {
        tts.stop()
        tts.setOnUtteranceProgressListener(null)
        interrupt = true
    }

    override fun startTalking() {
        shouldIgnore = false
    }
}
