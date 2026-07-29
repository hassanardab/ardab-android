package com.mamo15.ardab.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mamo15.ardab.R
import com.mamo15.ardab.localization.LanguageManager

@Composable
fun ProfileScreen() {

    val context = LocalContext.current

    var selectedLanguage by remember {
        mutableStateOf(LanguageManager.currentLanguage(context))
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Top
        ) {

            Text(
                text = stringResource(R.string.app_settings),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.language),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.language_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(12.dp))

                    LanguageOption(
                        title = stringResource(R.string.follow_system),
                        selected = selectedLanguage == LanguageManager.SYSTEM
                    ) {
                        selectedLanguage = LanguageManager.SYSTEM
                        LanguageManager.setLanguage(
                            context,
                            LanguageManager.SYSTEM
                        )
                    }

                    LanguageOption(
                        title = stringResource(R.string.english),
                        selected = selectedLanguage == LanguageManager.ENGLISH
                    ) {
                        selectedLanguage = LanguageManager.ENGLISH
                        LanguageManager.setLanguage(
                            context,
                            LanguageManager.ENGLISH
                        )
                    }

                    LanguageOption(
                        title = stringResource(R.string.arabic),
                        selected = selectedLanguage == LanguageManager.ARABIC
                    ) {
                        selectedLanguage = LanguageManager.ARABIC
                        LanguageManager.setLanguage(
                            context,
                            LanguageManager.ARABIC
                        )
                    }

                    LanguageOption(
                        title = stringResource(R.string.russian),
                        selected = selectedLanguage == LanguageManager.RUSSIAN
                    ) {
                        selectedLanguage = LanguageManager.RUSSIAN
                        LanguageManager.setLanguage(
                            context,
                            LanguageManager.RUSSIAN
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        RadioButton(
            selected = selected,
            onClick = null
        )

        Text(
            text = title,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}