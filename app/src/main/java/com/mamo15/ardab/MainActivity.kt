package com.mamo15.ardab

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity // Switched from ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mamo15.ardab.localization.LanguageManager
import com.mamo15.ardab.ui.MainScreen
import com.mamo15.ardab.ui.theme.ArdabTheme

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

class MainActivity : AppCompatActivity() { // Inherit from AppCompatActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        // Depending on your updated LanguageManager implementation,
        // applying the saved language here is still good practice.
        LanguageManager.applySavedLanguage(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ArdabTheme {
                val configuration = LocalConfiguration.current
                val layoutDirection = if (configuration.layoutDirection == android.util.LayoutDirection.RTL) {
                    LayoutDirection.Rtl
                } else {
                    LayoutDirection.Ltr
                }

                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    MainScreen()
                }
            }
        }
    }
}