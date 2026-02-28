# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in $ANDROID_SDK_ROOT/tools/proguard/proguard-android.txt

# Keep all classes in your package
-keep class org.example.project.babygrowthtrackingapplication.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Facebook
-keep class com.facebook.** { *; }

# Ktor
-keep class io.ktor.** { *; }

# Kotlinx Serialization
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep all @Serializable classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
}