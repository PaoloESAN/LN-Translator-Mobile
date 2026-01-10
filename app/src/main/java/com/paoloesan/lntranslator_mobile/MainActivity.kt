package com.paoloesan.lntranslator_mobile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paoloesan.lntranslator_mobile.ui.theme.LNTranslatormobileTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                                            "Inicio" -> Icon(
                                                Icons.Filled.Home,
                                                contentDescription = screen
                                            )

                                            "Ajustes" -> Icon(
                                                Icons.Filled.Settings,
                                                contentDescription = screen
                                            )
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
                        "Inicio" -> MainScreen(modifier = Modifier.padding(innerPadding))
                        "Ajustes" -> AjustesScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "¡Bienvenido a tu Pantalla!",
                fontSize = 24.sp
            )

            Button(
                onClick = {
                    Toast.makeText(context, "¡Botón presionado!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Presióname")
            }
        }
    }
}

@Composable
fun AjustesScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "Estas en ajustes",
                fontSize = 24.sp
            )

            Button(
                onClick = {
                    Toast.makeText(context, "ajustes aqui", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Presióname")
            }
        }
    }
}