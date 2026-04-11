// commonMain/.../platform/AppLifecycleObserver.kt
package org.example.project.babygrowthtrackingapplication.platform

interface AppLifecycleListener {
    fun onAppForeground()
    fun onAppBackground()
}

expect class AppLifecycleObserver() {
    fun register(listener: AppLifecycleListener)
    fun unregister()
}