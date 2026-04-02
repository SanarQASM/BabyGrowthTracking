package org.example.project.babygrowthtrackingapplication.platform
// androidMain

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

@SuppressLint("StaticFieldLeak")
object AppContextHolder {
    private var _context: Context? = null

    /** Call once from Application.onCreate(). */
    fun init(application: Application) {
        _context = application.applicationContext
    }

    /** Returns the application Context. Throws if init() was not called. */
    val context: Context
        get() = _context
            ?: error(
                "AppContextHolder not initialised. " +
                        "Call AppContextHolder.init(this) in your Application.onCreate()."
            )
}