package com.djoudini.iptv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OsdOverlay(
    channelName: String,
    channelLogo: String?,
    epgTitle: String?,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Bottom Control Bar with Glassmorphism
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(32.dp)
                .height(140.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .blur(10.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(32.dp)
                .height(140.dp)
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channelName,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = epgTitle ?: "Keine Programminformationen",
                    color = Color.LightGray,
                    fontSize = 16.sp
                )
            }

            // Controls
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(onClick = {}) { Icon(Icons.Default.SkipPrevious, null, tint = Color.White) }
                IconButton(onClick = {}) { Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(48.dp)) }
                IconButton(onClick = {}) { Icon(Icons.Default.SkipNext, null, tint = Color.White) }
            }
            
            Spacer(modifier = Modifier.width(32.dp))
            
            IconButton(onClick = {}) { Icon(Icons.Default.Settings, null, tint = Color.White) }
        }
    }
}
