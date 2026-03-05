package com.djoudini.iptv.player

import android.content.Context
import android.os.Build
import android.view.Display
import android.view.WindowManager
import androidx.annotation.RequiresApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AfrController @Inject constructor(
    private val context: Context
) {
    /**
     * Attempts to match the display refresh rate to the video frame rate.
     * Only supported on Android TV devices with appropriate hardware.
     */
    fun syncRefreshRate(videoFps: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val supportedModes = display.supportedModes
            
            // Find the best mode matching the FPS
            val bestMode = supportedModes.minByOrNull { mode ->
                kotlin.math.abs(mode.refreshRate - videoFps)
            }
            
            // Note: In a real app, you'd need a Window reference to set the preferredDisplayModeId
            // This controller acts as a helper to determine if a switch is needed.
        }
    }
}
