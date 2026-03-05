package com.djoudini.iptv.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.djoudini.iptv.data.preferences.ViewType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTrakt: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Top Header
        Text(
            text = "Erweiterte Einstellungen",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        // Setting: Trakt.tv
        SettingsSection(title = "Account-Synchronisation") {
            Button(
                onClick = onNavigateToTrakt,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFED1C24)) // Trakt Red
            ) {
                Text("Trakt.tv verbinden", color = Color.White)
            }
        }
        
        // Setting: View Mode
        SettingsSection(title = "Ansicht (VOD/Serien)") {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingsChip(
                    text = "Grid",
                    isSelected = uiState.viewType == ViewType.GRID,
                    onClick = { viewModel.updateViewType(ViewType.GRID) }
                )
                SettingsChip(
                    text = "Liste",
                    isSelected = uiState.viewType == ViewType.LIST,
                    onClick = { viewModel.updateViewType(ViewType.LIST) }
                )
                SettingsChip(
                    text = "Details",
                    isSelected = uiState.viewType == ViewType.DETAIL,
                    onClick = { viewModel.updateViewType(ViewType.DETAIL) }
                )
            }
        }

        // Setting: Player Engine
        SettingsSection(title = "Player Puffer (Live TV Fast Zapping)") {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingsChip(
                    text = "Sehr Schnell (500ms)",
                    isSelected = uiState.bufferSize == 500,
                    onClick = { viewModel.updateBufferSize(500) }
                )
                SettingsChip(
                    text = "Normal (1500ms)",
                    isSelected = uiState.bufferSize == 1500,
                    onClick = { viewModel.updateBufferSize(1500) }
                )
                SettingsChip(
                    text = "Stabil (5000ms)",
                    isSelected = uiState.bufferSize == 5000,
                    onClick = { viewModel.updateBufferSize(5000) }
                )
            }
        }

        SettingsSection(title = "Player Engine (User-Agent)") {
            var uaText by remember { mutableStateOf(uiState.userAgent) }
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = uaText,
                    onValueChange = { uaText = it },
                    label = { Text("User-Agent Spoofing", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.DarkGray
                    ),
                    modifier = Modifier.fillMaxWidth(0.5f)
                )
                Button(
                    onClick = { viewModel.updateUserAgent(uaText) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Speichern", color = Color.White)
                }
            }
        }
        
        SettingsSection(title = "App Daten & Performance") {
            Button(
                onClick = { viewModel.clearCache() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020)) // Error Red
            ) {
                Text("Offline-Cache & Datenbank leeren", color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onNavigateBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
             Text("Zurück zum Dashboard", color = Color.White)
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        content()
    }
}

@Composable
fun SettingsChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF2C2C2C)
    val borderColor = if (isFocused) Color.White else Color.Transparent

    Box(
        modifier = Modifier
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .background(bgColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .focusable() // D-Pad support
            .onFocusChanged { isFocused = it.isFocused }
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
