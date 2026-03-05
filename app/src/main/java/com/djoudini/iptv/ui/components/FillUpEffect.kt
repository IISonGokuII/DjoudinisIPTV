package com.djoudini.iptv.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.fillUpEffect(index: Int = 0): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "fillUpTransition")
    
    // Create a stagger effect based on index
    val delay = index * 150
    
    val fillFraction by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, delayMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(delay, StartOffsetType.Delay)
        ),
        label = "fillUpFraction"
    )

    this.drawWithContent {
        drawContent()
        // Draw the filling color from bottom to top
        val currentHeight = size.height * fillFraction
        val topOffset = size.height - currentHeight
        
        drawRect(
            color = Color(0xFF2E2E2E), // slightly brighter fill color
            topLeft = Offset(0f, topOffset),
            size = Size(size.width, currentHeight)
        )
    }
}
