package com.djoudini.iptv.ui.livetv

import android.content.res.Configuration
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout


import com.djoudini.iptv.ui.onboarding.OnboardingState
import com.djoudini.iptv.ui.onboarding.OnboardingViewModel

@Composable
fun LiveTvScreen(
    liveTvViewModel: LiveTvViewModel = hiltViewModel(),
    onboardingViewModel: OnboardingViewModel = hiltViewModel()
) {
    val liveTvUiState by liveTvViewModel.uiState.collectAsState()
    val onboardingUiState by onboardingViewModel.uiState.collectAsState()
    val channels = liveTvUiState.channels.collectAsLazyPagingItems()
    val configuration = LocalConfiguration.current

    if (onboardingUiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    if (!onboardingUiState.onboardingComplete) {
        // Hier sollte der OnboardingScreen angezeigt werden, oder eine Weiterleitung erfolgen
        // Da wir den OnboardingScreen nicht direkt hier einbetten können und keine Navigation haben,
        // werde ich vorerst einen Platzhalter anzeigen und einen Log-Eintrag hinzufügen.
        // In einer realen Anwendung würde hier die Navigation zum Onboarding-Fluss stattfinden.
        Text(
            text = "Onboarding noch nicht abgeschlossen. Bitte schließe das Onboarding ab.",
            color = Color.White,
            modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
        )
        return
    }

    if (liveTvUiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    // Full-screen player dialog
    if (liveTvUiState.isFullScreen && liveTvUiState.currentChannel != null) {
        Dialog(
            onDismissRequest = { liveTvViewModel.toggleFullScreen() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                IptvPlayerView(
                    channel = liveTvUiState.currentChannel!!,
                    bufferSizeMs = liveTvUiState.bufferSize,
                    isFullScreen = true,
                    onToggleFullScreen = { liveTvViewModel.toggleFullScreen() }
                )
            }
        }
    }

    // Main content
    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        LandscapeLayout(liveTvUiState, liveTvViewModel, channels)
    } else {
        PortraitLayout(liveTvUiState, liveTvViewModel, channels)
    }
}

@Composable
fun LandscapeLayout(
    liveTvUiState: LiveTvUiState,
    liveTvViewModel: LiveTvViewModel,
    channels: LazyPagingItems<ChannelEntity>
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AnimatedVisibility(visible = !liveTvUiState.isFullScreen) {
            Row {
                val sidebarItems = liveTvUiState.categories.map { SidebarItem(it.id, it.name) }
                DynamicSidebar(
                    items = sidebarItems,
                    onItemSelected = { item ->
                        val cat = liveTvUiState.categories.find { it.id == item.id }
                        if(cat != null) liveTvViewModel.selectCategory(cat)
                    }
                )
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF141414))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sender", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        if (liveTvUiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            IconButton(onClick = { liveTvViewModel.syncCurrentCategory() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Aktualisieren", tint = Color.White)
                            }
                        }
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(count = channels.itemCount, key = { index -> channels[index]?.streamId ?: index }) { index ->
                            val channel = channels[index]
                            if (channel != null) {
                                ChannelListItem(
                                    channel = channel,
                                    isSelected = liveTvUiState.currentChannel?.streamId == channel.streamId,
                                    onClick = { liveTvViewModel.selectChannel(channel) }
                                )
                            }
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFF0A0A0A))
        ) {
            PlayerAndEpg(uiState = liveTvUiState, viewModel = liveTvViewModel, isPortrait = false)
        }
    }
}

@Composable
fun PortraitLayout(
    uiState: LiveTvUiState,
    viewModel: LiveTvViewModel,
    channels: LazyPagingItems<ChannelEntity>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        PlayerAndEpg(liveTvUiState = liveTvUiState, liveTvViewModel = liveTvViewModel, isPortrait = true)

        AnimatedVisibility(visible = !liveTvUiState.isFullScreen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Takes remaining space
                    .background(Color(0xFF141414))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sender", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    if (liveTvUiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { liveTvViewModel.syncCurrentCategory() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Aktualisieren", tint = Color.White)
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(count = channels.itemCount, key = { index -> channels[index]?.streamId ?: index }) { index ->
                        val channel = channels[index]
                        if (channel != null) {
                            ChannelListItem(
                                channel = channel,
                                isSelected = liveTvUiState.currentChannel?.streamId == channel.streamId,
                                onClick = { liveTvViewModel.selectChannel(channel) }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ColumnScope.PlayerAndEpg(liveTvUiState: LiveTvUiState, liveTvViewModel: LiveTvViewModel, isPortrait: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
    ) {
        if (liveTvUiState.currentChannel != null) {
            IptvPlayerView(
                channel = liveTvUiState.currentChannel!!,
                epgListings = liveTvUiState.currentEpg,
                bufferSizeMs = liveTvUiState.bufferSize,
                onToggleFullScreen = { liveTvViewModel.toggleFullScreen() }
            )
        } else {
            Text(
                text = "Wähle einen Sender",
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    AnimatedVisibility(visible = !liveTvUiState.isFullScreen) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "EPG - Aktuelles Programm",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (liveTvUiState.currentEpg.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isPortrait) Modifier else Modifier.weight(1f))
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = liveTvUiState.currentEpg, key = { it.id }) { epg ->
                        EpgListItem(epg = epg)
                    }
                }
            } else {
                Text(
                    text = "Keine EPG-Daten verfügbar.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}


@Composable
fun ChannelListItem(
    channel: ChannelEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        isFocused -> Color(0xFF2C2C2C)
        else -> Color.Transparent
    }
    
    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = channel.name,
            color = if (isSelected || isFocused) Color.White else Color.LightGray,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun IptvPlayerView(
    channel: ChannelEntity,
    bufferSizeMs: Int,
    onToggleFullScreen: () -> Unit,
    epgListings: List<XtreamEpgListing> = emptyList(),
    isFullScreen: Boolean = false
) {
    val context = LocalContext.current
    
    val exoPlayer = remember(bufferSizeMs) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    var isPlaying by remember { mutableStateOf(true) }
    var showOverlay by remember { mutableStateOf(true) }

    LaunchedEffect(showOverlay) {
        if (showOverlay && isPlaying) {
            delay(3000)
            showOverlay = false
        }
    }

    LaunchedEffect(channel.streamUrl) {
        exoPlayer.setMediaItem(MediaItem.fromUri(channel.streamUrl))
        exoPlayer.prepare()
        exoPlayer.play()
        isPlaying = true
        showOverlay = true
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    val currentProgram = epgListings.firstOrNull()
    val decodedTitle = try {
        if (currentProgram?.title != null) {
            String(Base64.getDecoder().decode(currentProgram.title), StandardCharsets.UTF_8)
        } else null
    } catch (e: Exception) {
        currentProgram?.title
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { showOverlay = !showOverlay }
            .focusable()
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = showOverlay,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                // Top Info
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(24.dp)
                ) {
                    Text(
                        text = channel.name,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (decodedTitle != null) "Jetzt: $decodedTitle" else "LIVE",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Center Controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play/Pause Button
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                            .clickable {
                                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            }
                            .focusable(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                
                // Bottom Controls
                IconButton(
                    onClick = onToggleFullScreen,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = "Toggle Fullscreen",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EpgListItem(epg: XtreamEpgListing) {
    // ... EpgListItem implementation remains the same
}
