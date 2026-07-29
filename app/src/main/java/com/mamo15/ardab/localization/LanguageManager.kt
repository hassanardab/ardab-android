package com.mamo15.ardab.localization

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LanguageManager {

    private const val PREFS = "language_prefs"
    private const val KEY = "language"

    const val SYSTEM = "system"
    const val ENGLISH = "en"
    const val ARABIC = "ar"
    const val RUSSIAN = "ru"

    /**
     * Applies the saved language upon app start.
     * Called in MainActivity's onCreate.
     */
    fun applySavedLanguage(context: Context) {
        val language = currentLanguage(context)
        val appLocale = if (language == SYSTEM) {
            LocaleListCompat.getEmptyLocaleList() // Tells Android to use the system default
        } else {
            LocaleListCompat.forLanguageTags(language)
        }
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    /**
     * Save the language preference and apply the change instantly.
     * Activity recreation is no longer needed because AppCompatDelegate handles it.
     */
    fun setLanguage(context: Context, language: String) {
        // 1. Save to SharedPreferences
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, language)
            .apply()

        // 2. Apply globally using AppCompatDelegate
        val appLocale = if (language == SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language)
        }
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    /**
     * Get the currently saved language code.
     */
    fun currentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY, SYSTEM) ?: SYSTEM
    }
}