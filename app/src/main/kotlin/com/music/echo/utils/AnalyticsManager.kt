package iad1tya.echo.music.utils

import android.content.Context
import android.os.Bundle
import timber.log.Timber

object AnalyticsManager {
    private var firebaseAnalyticsInstance: Any? = null
    private var setUserIdMethod: java.lang.reflect.Method? = null
    private var logEventMethod: java.lang.reflect.Method? = null

    fun initialize(context: Context) {
        try {
            val faClass = Class.forName("com.google.firebase.analytics.FirebaseAnalytics")
            val getInstanceMethod = faClass.getMethod("getInstance", Context::class.java)
            firebaseAnalyticsInstance = getInstanceMethod.invoke(null, context)
            setUserIdMethod = faClass.getMethod("setUserId", String::class.java)
            logEventMethod = faClass.getMethod("logEvent", String::class.java, Bundle::class.java)
            Timber.d("AnalyticsManager initialized with Firebase Analytics")
        } catch (e: Exception) {
            Timber.d("Firebase Analytics not available (likely FOSS build)")
        }
    }

    fun setUserId(userId: String) {
        try {
            setUserIdMethod?.invoke(firebaseAnalyticsInstance, userId)
        } catch (e: Exception) {
            Timber.e(e, "Failed to set User ID")
        }
    }

    fun logEvent(eventName: String, bundle: Bundle? = null) {
        try {
            logEventMethod?.invoke(firebaseAnalyticsInstance, eventName, bundle)
        } catch (e: Exception) {
            Timber.e(e, "Failed to log event: $eventName")
        }
    }
}
