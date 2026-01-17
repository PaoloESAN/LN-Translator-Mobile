package com.paoloesan.lntranslator_mobile

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.paoloesan.lntranslator_mobile.ui.home.HomeScreen
import com.paoloesan.lntranslator_mobile.ui.settings.SettingsScreen
import com.paoloesan.lntranslator_mobile.ui.theme.LNTranslatormobileTheme

class MainActivity : AppCompatActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val tema = prefs.getString("tema_app", "Predeterminado del sistema")

        val modo = when (tema) {
            "Claro" -> AppCompatDelegate.MODE_NIGHT_NO
            "Oscuro" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(modo)
        enableEdgeToEdge()
        setContent {
            LNTranslatormobileTheme {
                var selectedItem by remember { mutableStateOf("Inicio") }
                val items = listOf("Inicio", "Ajustes")
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                when (selectedItem) {
                                    "Inicio" -> Text("LN Translator")
                                    "Ajustes" -> Text("Ajustes")
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = {
                                        when (screen) {
                                            "Inicio" -> if (selectedItem == screen) {
                                                Icon(
                                                    Icons.Rounded.Home,
                                                    contentDescription = screen
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Outlined.Home,
                                                    contentDescription = screen
                                                )
                                            }

                                            "Ajustes" -> if (selectedItem == screen) {
                                                Icon(
                                                    Icons.Rounded.Settings,
                                                    contentDescription = screen
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Outlined.Settings,
                                                    contentDescription = screen
                                                )
                                            }
                                        }
                                    },
                                    label = { Text(screen) },
                                    selected = selectedItem == screen,
                                    onClick = {
                                        selectedItem = screen
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    when (selectedItem) {
                        "Inicio" -> HomeScreen(modifier = Modifier.padding(innerPadding))
                        "Ajustes" -> SettingsScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}