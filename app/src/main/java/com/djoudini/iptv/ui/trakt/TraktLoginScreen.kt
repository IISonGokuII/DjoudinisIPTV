package com.djoudini.iptv.ui.trakt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.djoudini.iptv.data.repository.TraktAuthStatus

@Composable
fun TraktLoginScreen(
    onNavigateBack: () -> Unit,
    viewModel: TraktViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userName by viewModel.traktUserName.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(500.dp)
                .background(Color(0xFF1A1A1A), RoundedCornerShape(16.dp))
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Trakt.tv Synchronisation",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            if (userName != null) {
                // Logged In State
                Text(
                    text = "Eingeloggt als: $userName",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Abmelden", color = Color.White)
                }
            } else if (uiState.userCode == null) {
                // Initial State
                Text(
                    text = "Synchronisiere deine Watchlist und deinen Fortschritt automatisch mit Trakt.tv.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { viewModel.startDeviceAuth() },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Jetzt verbinden", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Device Code Auth State
                Text(
                    text = "Besuche bitte die folgende URL auf deinem Smartphone oder PC:",
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = uiState.verificationUrl ?: "trakt.tv/activate",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Und gib diesen Code ein:",
                    color = Color.LightGray
                )
                Text(
                    text = uiState.userCode ?: "",
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
                
                if (uiState.authStatus is TraktAuthStatus.Polling) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Warte auf Bestätigung...", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            if (uiState.error != null) {
                Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
            }

            TextButton(onClick = onNavigateBack) {
                Text("Abbrechen", color = Color.Gray)
            }
        }
    }
}
