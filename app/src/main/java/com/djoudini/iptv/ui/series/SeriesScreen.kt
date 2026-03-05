package com.djoudini.iptv.ui.series

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.djoudini.iptv.data.remote.XtreamSeries
import com.djoudini.iptv.ui.components.DynamicSidebar
import com.djoudini.iptv.ui.components.SidebarItem
import androidx.compose.animation.core.*
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    viewModel: SeriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val seriesList = uiState.series.collectAsLazyPagingItems()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        // Sidebar for Categories
        val sidebarItems = uiState.categories.map { SidebarItem(it.id, it.name) }
        DynamicSidebar(
            items = sidebarItems,
            onItemSelected = { item -> 
                val cat = uiState.categories.find { it.id == item.id }
                if(cat != null) viewModel.selectCategory(cat)
            }
        )

        // Main Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = uiState.currentCategory?.name ?: "Serien",
                        color = Color.White,
                        fontSize = 22.sp, // Reduced from 28sp
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { viewModel.syncCurrentCategory() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Aktualisieren",
                                tint = Color.White
                            )
                        }
                    }
                }
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Zurück", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    userScrollEnabled = false
                ) {
                    items(12) { index ->
                        SeriesCardSkeleton(index = index)
                    }
                }
            } else if (uiState.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(count = seriesList.itemCount, key = { index -> seriesList[index]?.seriesId ?: index }) { index ->
                        val series = seriesList[index]
                        if (series != null) {
                            SeriesCard(
                                series = series,
                                onClick = { onNavigateToDetail(series.seriesId) }
                            )
                        } else {
                            SeriesCardSkeleton(index = index)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeriesCard(
    series: XtreamSeries,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "seriesScale"
    )

    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent

    Column(
        modifier = Modifier
            .graphicsLayer { 
                scaleX = scale
                scaleY = scale 
            }
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .focusable() // D-Pad navigation
            .onFocusChanged { isFocused = it.isFocused }
            .border(3.dp, borderColor, RoundedCornerShape(8.dp))
    ) {
        // Poster
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .background(Color(0xFF1E1E1E))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(series.cover)
                    .crossfade(true)
                    .size(coil.size.Size(320, 480))
                    .build(),
                contentDescription = series.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Rating Badge
            if (series.rating != null && series.rating > 0.0) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = series.rating.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Title Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141414))
                .padding(12.dp)
        ) {
            Text(
                text = series.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SeriesCardSkeleton(index: Int = 0) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF141414))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .fillUpEffect(index)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp)
                    .fillUpEffect(index)
            )
        }
    }
}
private fun Modifier.fillUpEffect(index: Int = 0): Modifier = composed {
    val fillFraction = remember { Animatable(0f) }

    LaunchedEffect(key1 = index) {
        // Stagger the start
        kotlinx.coroutines.delay((index % 12 * 100).toLong())

        // Loop the animation manually
        while (true) {
            fillFraction.snapTo(0f)
            fillFraction.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1200, easing = LinearEasing)
            )
            kotlinx.coroutines.delay(600) // Hold at the end before restarting
        }
    }

    this.drawWithContent {
        drawContent()
        val currentHeight = size.height * fillFraction.value
        val topOffset = size.height - currentHeight
        drawRect(
            color = Color(0xFF2E2E2E),
            topLeft = Offset(0f, topOffset),
            size = Size(size.width, currentHeight)
        )
    }
}
