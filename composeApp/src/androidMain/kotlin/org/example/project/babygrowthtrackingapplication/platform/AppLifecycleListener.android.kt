package org.example.project.babygrowthtrackingapplication.platform

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

// androidMain
actual class AppLifecycleObserver actual constructor() : DefaultLifecycleObserver {
    private var listener: AppLifecycleListener? = null

    actual fun register(listener: AppLifecycleListener) {
        this.listener = listener
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }
    actual fun unregister() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        listener = null
    }
    override fun onStart(owner: LifecycleOwner) { listener?.onAppForeground() }
    override fun onStop(owner: LifecycleOwner)  { listener?.onAppBackground() }
}