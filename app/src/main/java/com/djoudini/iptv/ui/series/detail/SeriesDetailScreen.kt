package com.djoudini.iptv.ui.series.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.djoudini.iptv.data.remote.XtreamEpisode
import com.djoudini.iptv.ui.vod.detail.MetaBadge

@Composable
fun SeriesDetailScreen(
    seriesId: Int,
    onNavigateBack: () -> Unit,
    viewModel: SeriesDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(seriesId) {
        viewModel.loadSeriesInfo(seriesId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        } else if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (uiState.seriesInfo != null) {
            val response = uiState.seriesInfo!!
            val info = response.info
            val seasons = response.seasons ?: emptyList()
            val episodesMap = response.episodes ?: emptyMap()

            var selectedSeason by remember { mutableStateOf(seasons.firstOrNull()?.seasonNumber ?: 1) }

            // Backdrop Image with Gradient Overlay
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = info?.backdropPath?.firstOrNull() ?: info?.cover,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().alpha(0.3f)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF0A0A0A)),
                                startY = 300f
                            )
                        )
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp)
            ) {
                // Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 48.dp)
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = info?.name ?: "Seriendetails",
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                ) {
                    // Poster & Meta
                    Column(modifier = Modifier.width(260.dp)) {
                        AsyncImage(
                            model = info?.cover,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(2f / 3f)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MetaBadge(text = info?.releaseDate?.take(4) ?: "N/A")
                            Spacer(modifier = Modifier.width(8.dp))
                            if (info?.rating != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = info.rating.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = info?.plot ?: "Keine Beschreibung verfügbar.",
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(48.dp))

                    // Seasons & Episodes Area
                    Column(modifier = Modifier.weight(1f)) {
                        // Season Selector
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(end = 48.dp)
                        ) {
                            items(items = seasons, key = { it.seasonNumber ?: it.hashCode() }) { season ->
                                val isSelected = selectedSeason == season.seasonNumber
                                SeasonTab(
                                    seasonName = season.name ?: "Staffel ${season.seasonNumber}",
                                    isSelected = isSelected,
                                    onClick = { season.seasonNumber?.let { selectedSeason = it } }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Episode List
                        val currentEpisodes = episodesMap[selectedSeason.toString()] ?: emptyList()
                        
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 32.dp, end = 48.dp)
                        ) {
                            items(items = currentEpisodes, key = { it.id }) { episode ->
                                EpisodeItem(episode = episode)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeasonTab(seasonName: String, isSelected: Boolean, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1A1A1A)
    val textColor = if (isSelected) Color.White else Color.Gray
    val borderColor = if (isFocused) Color.White else Color.Transparent

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
        modifier = Modifier
            .clickable { onClick() }
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
    ) {
        Text(
            text = seasonName.uppercase(),
            color = textColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun EpisodeItem(episode: XtreamEpisode) {
    var isFocused by remember { mutableStateOf(false) }
    
    val bgColor = if (isFocused) Color(0xFF2C2C2C) else Color(0xFF141414)
    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { /* TODO: Play Episode */ }
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Episode Thumbnail
        Box(
            modifier = Modifier
                .width(160.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E1E1E))
        ) {
            AsyncImage(
                model = episode.info?.movieImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Play Icon Overlay
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.Center).size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Episode Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${episode.episodeNum}. ${episode.title ?: "Episode ${episode.episodeNum}"}",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (episode.info?.duration != null) {
                Text(
                    text = episode.info.duration,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = episode.info?.plot ?: "Keine Beschreibung verfügbar.",
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
