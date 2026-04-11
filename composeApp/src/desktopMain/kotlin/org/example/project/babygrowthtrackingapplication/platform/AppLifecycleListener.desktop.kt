package org.example.project.babygrowthtrackingapplication.platform
actual class AppLifecycleObserver actual constructor() {
    actual fun register(listener: AppLifecycleListener) {}
    actual fun unregister() {}
}