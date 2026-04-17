package org.example.project.babygrowthtrackingapplication

import android.app.Application
import com.google.firebase.FirebaseApp
import org.example.project.babygrowthtrackingapplication.data.auth.SocialAuthManager
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.notifications.FcmTokenService
import org.example.project.babygrowthtrackingapplication.notifications.NotificationRepository
import org.example.project.babygrowthtrackingapplication.notifications.NotificationViewModel
import org.example.project.babygrowthtrackingapplication.platform.AppContextHolder
import org.example.project.babygrowthtrackingapplication.platform.initMemoryLocalStorage
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

// ─────────────────────────────────────────────────────────────────────────────
// MyApplication.kt — FIXED
//
// BUG: NotificationViewModel was not registered in the Koin graph.
//      MainActivity tried to do `by inject()` on NotificationViewModel, which
//      threw a NoBeanDefinitionException at runtime because no matching
//      definition existed.
//
// FIX:
//  • Register NotificationViewModel as a singleton in appModule so:
//      - MainActivity can inject it to call onDeepLinkReceived()
//      - The Compose App() tree gets the exact same instance via LocalKoin or
//        koinViewModel() / get()
//
//  NOTE: Adjust the constructor parameters below to match how your project
//        provides ApiService, base URL, and token. The key requirement is that
//        the SAME instance is shared between MainActivity and the Compose tree.
// ─────────────────────────────────────────────────────────────────────────────

val appModule = module {
    single { SocialAuthManager() }

    // NotificationViewModel registered as a singleton so MainActivity and the
    // Compose tree both receive the same instance.
    single {
        // These dependencies should already exist in your Koin graph.
        // Adjust names/types to match your actual registrations.
        val apiService: ApiService = get()
        val baseUrl = "http://10.0.2.2:8080/api"   // replace with your AppConstants.Api.SERVER_EMULATOR

        NotificationViewModel(
            repository      = NotificationRepository(
                client   = get(),   // HttpClient singleton
                baseUrl  = baseUrl,
                getToken = { get<PreferencesManagerProvider>().getToken() }
            ),
            getUserId       = { get<PreferencesManagerProvider>().getUserId() },
            fcmTokenService = FcmTokenService()
        )
    }
}

// Helper interface — replace with your actual PreferencesManager access pattern
interface PreferencesManagerProvider {
    fun getUserId(): String?
    fun getToken(): String?
}

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        AppContextHolder.init(this)
        initMemoryLocalStorage(this)

        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}