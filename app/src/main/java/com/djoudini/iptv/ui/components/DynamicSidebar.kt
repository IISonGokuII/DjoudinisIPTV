package com.djoudini.iptv.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Dynamic TV-First Sidebar. Expands smoothly upon receiving D-Pad focus.
 */
@Composable
fun DynamicSidebar(
    items: List<SidebarItem>,
    onItemSelected: (SidebarItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDrawerExpanded by remember { mutableStateOf(false) }
    var hasFocusInSidebar by remember { mutableStateOf(false) }

    // Auto-collapse when losing D-Pad focus completely
    LaunchedEffect(hasFocusInSidebar) {
        if (!hasFocusInSidebar) {
            delay(200) // Small delay to prevent jitter
            isDrawerExpanded = false
        } else {
            isDrawerExpanded = true
        }
    }

    val drawerWidth by animateDpAsState(
        targetValue = if (isDrawerExpanded) 240.dp else 72.dp,
        animationSpec = tween(durationMillis = 300),
        label = "drawerWidth"
    )

    Column(
        modifier = modifier
            .width(drawerWidth)
            .fillMaxHeight()
            .background(Color(0xFF141414)) // Netflix-like Dark Gray
            .padding(vertical = 32.dp, horizontal = 12.dp)
            .onFocusChanged { state ->
                hasFocusInSidebar = state.hasFocus
            }
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.forEach { item ->
            SidebarItemView(
                item = item,
                isExpanded = isDrawerExpanded,
                onClick = { onItemSelected(item) }
            )
        }
    }
}

@Composable
fun SidebarItemView(
    item: SidebarItem,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    // D-Pad Focus State Tracking
    var isFocused by remember { mutableStateOf(false) }
    
    // Scale up the item elegantly when focused via TV remote
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.15f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )
    
    // Glowing border for TV focus feedback
    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) Color(0xFF2C2C2C) else Color.Transparent)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            // CRITICAL FOR TV: Makes the component reachable by D-Pad
            .focusable() 
            .onFocusChanged { isFocused = it.isFocused }
            .padding(horizontal = 16.dp)
    ) {
        // Placeholder for an Icon
        Box(modifier = Modifier
            .size(28.dp)
            .background(if (isFocused) MaterialTheme.colorScheme.primary else Color.Gray, RoundedCornerShape(4.dp))
        )

        if (isExpanded) {
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = item.title,
                color = if (isFocused) Color.White else Color.LightGray,
                fontSize = 16.sp,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

data class SidebarItem(val id: String, val title: String)
