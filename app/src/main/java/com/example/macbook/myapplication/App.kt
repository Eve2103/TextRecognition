package com.example.macbook.myapplication

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import org.koin.android.ext.android.startKoin

class App() : Application() {

    override fun onCreate() {
        FirebaseApp.initializeApp(this)
        Log.e("DAS", "Create ")
        super.onCreate()
        startKoin(this, listOf(ocrModule))

    }
}