package com.resonix.app.core

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

/** Stores the language the user picked inside the app ("ar" or "en") and applies it. */
object LocaleManager {
    private const val PREFS = "resonix_prefs"
    private const val KEY = "app_language"

    fun saved(ctx: Context): String? =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)

    /** The language currently used by the resources of [ctx]. */
    fun current(ctx: Context): String = ctx.resources.configuration.locales[0].language

    /** Call from attachBaseContext. Returns [base] unchanged until the user picks a language. */
    fun wrap(base: Context): Context {
        val lang = saved(base) ?: return base
        val locale = Locale.forLanguageTag(lang)
        Locale.setDefault(locale)
        val cfg = Configuration(base.resources.configuration)
        cfg.setLocale(locale)
        cfg.setLayoutDirection(locale)
        return base.createConfigurationContext(cfg)
    }

    fun setLanguage(activity: Activity, lang: String) {
        if (saved(activity) == lang) return
        activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, lang).apply()
        val locale = Locale.forLanguageTag(lang)
        Locale.setDefault(locale)
        // Keep the application context (used by the repository and notices) in sync.
        val app = activity.applicationContext
        val cfg = Configuration(app.resources.configuration)
        cfg.setLocale(locale)
        cfg.setLayoutDirection(locale)
        @Suppress("DEPRECATION")
        app.resources.updateConfiguration(cfg, app.resources.displayMetrics)
        activity.recreate()
    }
}

fun Context.findActivity(): Activity? {
    var c: Context = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}
