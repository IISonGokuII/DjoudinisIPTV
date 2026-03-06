package com.djoudini.iptv.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToSeriesDetail: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Alle", "Live TV", "Filme", "Serien")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favoriten", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A))
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1A1A1A),
                contentColor = Color.White
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Content
            val favorites = when (selectedTab) {
                0 -> uiState.allFavorites
                1 -> uiState.liveFavorites
                2 -> uiState.vodFavorites
                3 -> uiState.seriesFavorites
                else -> uiState.allFavorites
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (favorites.isEmpty()) {
                EmptyFavoritesState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favorites, key = { it.streamId }) { favorite ->
                        FavoriteItemCard(
                            favorite = favorite,
                            onClick = {
                                when (favorite.type) {
                                    "LIVE", "VOD" -> favorite.streamUrl?.let { onNavigateToPlayer(it) }
                                    "SERIES" -> onNavigateToSeriesDetail(favorite.streamId)
                                }
                            },
                            onRemove = { viewModel.removeFavorite(favorite.streamId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteItemCard(
    favorite: FavoriteWithDetails,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val icon: ImageVector = when (favorite.type) {
        "LIVE" -> Icons.Default.LiveTv
        "VOD" -> Icons.Default.Movie
        "SERIES" -> Icons.Default.Tv
        else -> Icons.Default.LiveTv
    }
    
    val typeLabel = when (favorite.type) {
        "LIVE" -> "Live TV"
        "VOD" -> "Film"
        "SERIES" -> "Serie"
        else -> favorite.type
    }
    
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) Color(0xFF2A2A2A) else Color(0xFF1A1A1A)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 8.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo or Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2A2A2A)),
                contentAlignment = Alignment.Center
            ) {
                if (favorite.logoUrl != null) {
                    AsyncImage(
                        model = favorite.logoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = favorite.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$typeLabel • Hinzugefügt: ${dateFormat.format(Date(favorite.addedAt))}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Remove button
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Entfernen",
                    tint = Color(0xFFE50914)
                )
            }
        }
    }
}

@Composable
private fun EmptyFavoritesState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LiveTv,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Keine Favoriten vorhanden",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Fügen Sie Sendern, Filmen oder Serien zu Ihren Favoriten hinzu",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}
