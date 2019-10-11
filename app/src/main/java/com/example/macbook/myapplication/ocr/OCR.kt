package com.example.macbook.myapplication.ocr

import com.example.macbook.myapplication.model.PreviewFrame
import io.reactivex.functions.Consumer

interface OCR : Consumer<PreviewFrame> {

    fun startTalking()

    fun stopTalking()

}