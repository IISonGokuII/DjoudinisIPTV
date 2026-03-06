package com.djoudini.iptv.ui.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DashboardScreen(
    onNavigateToLive: () -> Unit,
    onNavigateToVod: () -> Unit,
    onNavigateToSeries: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)) // Deep dark background
            .padding(32.dp)
    ) {
        // Top Bar: User Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Willkommen, ${uiState.username}",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ablaufdatum: ${uiState.expirationDate} • Verbindungen: ${uiState.activeConnections}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            
            // App Logo or Status Indicator could go here
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                 Text("PRO", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Main Content: The 4 Big Tiles
        // We use a Column of Rows for a fixed 2x2 grid that scales perfectly
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp) // Reduced spacing
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardTile(
                    title = "LIVE TV",
                    subtitle = "${uiState.liveCount} Sender",
                    isLoading = uiState.isLiveLoading,
                    color1 = Color(0xFFE50914), // Netflix Red
                    color2 = Color(0xFF8A0008),
                    onClick = onNavigateToLive,
                    modifier = Modifier.weight(1f)
                )
                DashboardTile(
                    title = "FILME",
                    subtitle = "${uiState.vodCount} Filme",
                    isLoading = uiState.isVodLoading,
                    color1 = Color(0xFF007BFF), // Deep Blue
                    color2 = Color(0xFF00448A),
                    onClick = onNavigateToVod,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardTile(
                    title = "SERIEN",
                    subtitle = "${uiState.seriesCount} Serien",
                    isLoading = uiState.isSeriesLoading,
                    color1 = Color(0xFF28A745), // Emerald Green
                    color2 = Color(0xFF135A24),
                    onClick = onNavigateToSeries,
                    modifier = Modifier.weight(1f)
                )
                DashboardTile(
                    title = "FAVORITEN",
                    subtitle = "Gespeicherte Inhalte",
                    isLoading = false,
                    color1 = Color(0xFFFFA500), // Orange
                    color2 = Color(0xFFCC5500),
                    onClick = onNavigateToFavorites,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardTile(
                    title = "EINSTELLUNGEN",
                    subtitle = "System & Account",
                    isLoading = false,
                    color1 = Color(0xFF6C757D), // Tech Gray
                    color2 = Color(0xFF343A40),
                    onClick = onNavigateToSettings,
                    modifier = Modifier.weight(1f)
                )
                // Spacer for now - could add another tile later
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DashboardTile(
    title: String,
    subtitle: String,
    isLoading: Boolean,
    color1: Color,
    color2: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    // TV-First: Extreme Smooth Scaling
    val scale by animateFloatAsState(
        targetValue = if (isFocused && !isLoading) 1.05f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "tileScale"
    )

    // TV-First: Glowing Border
    val borderColor by animateColorAsState(
        targetValue = if (isFocused && !isLoading) Color.White else Color.Transparent,
        animationSpec = tween(durationMillis = 150),
        label = "borderColor"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = if (isLoading) listOf(Color(0xFF1A1A1A), Color(0xFF121212)) else listOf(color1, color2)
                )
            )
            .border(3.dp, borderColor, RoundedCornerShape(20.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .focusable(!isLoading) // Only focusable when not loading
            .then(
                if (!isLoading) Modifier.clickable { onClick() } else Modifier
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Text(
                text = title,
                color = if (isLoading) Color.Gray else Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            
            if (isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(0.6f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = color1,
                    trackColor = Color.DarkGray.copy(alpha = 0.3f)
                )
                Text(
                    text = "Lade Daten...",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
