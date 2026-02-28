package org.example.project.babygrowthtrackingapplication

import android.app.Application
import com.google.firebase.FirebaseApp
import org.example.project.babygrowthtrackingapplication.data.auth.SocialAuthManager
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

val appModule = module {
    single { SocialAuthManager() }
}

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // ✅ Initialize Firebase first
        FirebaseApp.initializeApp(this)

        // ✅ Start Koin
        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}