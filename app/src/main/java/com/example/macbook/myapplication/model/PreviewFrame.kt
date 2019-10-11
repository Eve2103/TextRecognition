package com.example.macbook.myapplication.model

data class PreviewFrame(var width: Int = 0,
                        var height: Int = 0,
                        var imageFormat: Int = 0,
                        var rotation: Int = 0,
                        var data:ByteArray = byteArrayOf())