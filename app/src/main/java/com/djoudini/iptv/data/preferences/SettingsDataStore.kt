package com.djoudini.iptv.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.djoudini.iptv.domain.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "djoudinis_iptv_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val internalDataStore = context.dataStore

    companion object {
        // Auth & Connection
        val PROVIDER_TYPE = stringPreferencesKey("provider_type")
        val HOST_URL = stringPreferencesKey("host_url")
        val USERNAME = stringPreferencesKey("username")
        val PASSWORD = stringPreferencesKey("password")
        val M3U_URL = stringPreferencesKey("m3u_url")
        
        // Onboarding
        val IS_ONBOARDING_COMPLETE = booleanPreferencesKey("is_onboarding_complete")
        
        // Player Engine
        val BUFFER_SIZE_TYPE = stringPreferencesKey("buffer_size_type")
        val CUSTOM_BUFFER_MS = intPreferencesKey("custom_buffer_ms")
        val VIDEO_DECODER = stringPreferencesKey("video_decoder")
        val AUTO_FRAME_RATE = booleanPreferencesKey("auto_frame_rate")
        val DEINTERLACING = booleanPreferencesKey("deinterlacing")
        val DEFAULT_ASPECT_RATIO = stringPreferencesKey("default_aspect_ratio")
        
        // EPG & Sync
        val EPG_UPDATE_INTERVAL = stringPreferencesKey("epg_update_interval")
        val EPG_TIME_SHIFT = floatPreferencesKey("epg_time_shift")
        
        // Security & App
        val PARENTAL_PIN = stringPreferencesKey("parental_pin")
        val AUTOSTART = booleanPreferencesKey("autostart")
        val LAST_PLAYED_STREAM_ID = longPreferencesKey("last_played_stream_id")
        val RESUME_VOD_POSITION = booleanPreferencesKey("resume_vod_position")

        // Legacy/Misc
        val USER_AGENT = stringPreferencesKey("user_agent")
        const val DEFAULT_USER_AGENT = "IPTVSmartersPro"
    }

    // --- Flows ---

    val isOnboardingCompleteFlow: Flow<Boolean> = internalDataStore.data.map { it[IS_ONBOARDING_COMPLETE] ?: false }
    
    val providerTypeFlow: Flow<ProviderType> = internalDataStore.data.map { 
        ProviderType.valueOf(it[PROVIDER_TYPE] ?: ProviderType.XTREAM.name) 
    }

    val bufferSizeFlow: Flow<BufferSize> = internalDataStore.data.map { 
        BufferSize.valueOf(it[BUFFER_SIZE_TYPE] ?: BufferSize.NORMAL.name) 
    }
    
    val customBufferMsFlow: Flow<Int> = internalDataStore.data.map { it[CUSTOM_BUFFER_MS] ?: 15000 }

    val videoDecoderFlow: Flow<VideoDecoder> = internalDataStore.data.map { 
        VideoDecoder.valueOf(it[VIDEO_DECODER] ?: VideoDecoder.HARDWARE.name) 
    }

    val autoFrameRateFlow: Flow<Boolean> = internalDataStore.data.map { it[AUTO_FRAME_RATE] ?: false }
    
    val deinterlacingFlow: Flow<Boolean> = internalDataStore.data.map { it[DEINTERLACING] ?: false }

    val aspectRatioFlow: Flow<AspectRatio> = internalDataStore.data.map { 
        AspectRatio.valueOf(it[DEFAULT_ASPECT_RATIO] ?: AspectRatio.FIT.name) 
    }

    val epgUpdateIntervalFlow: Flow<EpgUpdateInterval> = internalDataStore.data.map { 
        EpgUpdateInterval.valueOf(it[EPG_UPDATE_INTERVAL] ?: EpgUpdateInterval.EVERY_24H.name) 
    }

    val epgTimeShiftFlow: Flow<Float> = internalDataStore.data.map { it[EPG_TIME_SHIFT] ?: 0.0f }

    val parentalPinFlow: Flow<String?> = internalDataStore.data.map { it[PARENTAL_PIN] }

    val autostartFlow: Flow<Boolean> = internalDataStore.data.map { it[AUTOSTART] ?: false }

    val lastPlayedStreamIdFlow: Flow<Long?> = internalDataStore.data.map { it[LAST_PLAYED_STREAM_ID] }

    val resumeVodPositionFlow: Flow<Boolean> = internalDataStore.data.map { it[RESUME_VOD_POSITION] ?: true }

    // --- Legacy Flows for UI Compatibility ---
    val hostUrlFlow: Flow<String?> = internalDataStore.data.map { it[HOST_URL] }
    val usernameFlow: Flow<String?> = internalDataStore.data.map { it[USERNAME] }
    val passwordFlow: Flow<String?> = internalDataStore.data.map { it[PASSWORD] }
    
    val expDateFlow: Flow<String?> = internalDataStore.data.map { it[stringPreferencesKey("exp_date")] }
    val activeConsFlow: Flow<String> = internalDataStore.data.map { it[stringPreferencesKey("active_cons")] ?: "0" }
    val maxConnectionsFlow: Flow<String> = internalDataStore.data.map { it[stringPreferencesKey("max_connections")] ?: "0" }
    
    val cachedLiveCountFlow: Flow<String> = internalDataStore.data.map { it[stringPreferencesKey("cached_live_count")] ?: "0" }
    val cachedVodCountFlow: Flow<String> = internalDataStore.data.map { it[stringPreferencesKey("cached_vod_count")] ?: "0" }
    val cachedSeriesCountFlow: Flow<String> = internalDataStore.data.map { it[stringPreferencesKey("cached_series_count")] ?: "0" }

    suspend fun saveCounts(live: String, vod: String, series: String) {
        internalDataStore.edit {
            it[stringPreferencesKey("cached_live_count")] = live
            it[stringPreferencesKey("cached_vod_count")] = vod
            it[stringPreferencesKey("cached_series_count")] = series
        }
    }

    // --- Save Methods ---

    suspend fun saveOnboardingComplete(complete: Boolean) {
        internalDataStore.edit { it[IS_ONBOARDING_COMPLETE] = complete }
    }

    suspend fun saveProviderType(type: ProviderType) {
        internalDataStore.edit { it[PROVIDER_TYPE] = type.name }
    }

    suspend fun saveBufferSize(type: BufferSize, customMs: Int = 15000) {
        internalDataStore.edit { 
            it[BUFFER_SIZE_TYPE] = type.name
            it[CUSTOM_BUFFER_MS] = customMs
        }
    }

    suspend fun saveVideoDecoder(decoder: VideoDecoder) {
        internalDataStore.edit { it[VIDEO_DECODER] = decoder.name }
    }

    suspend fun saveAutoFrameRate(enabled: Boolean) {
        internalDataStore.edit { it[AUTO_FRAME_RATE] = enabled }
    }

    suspend fun saveDeinterlacing(enabled: Boolean) {
        internalDataStore.edit { it[DEINTERLACING] = enabled }
    }

    suspend fun saveAspectRatio(ratio: AspectRatio) {
        internalDataStore.edit { it[DEFAULT_ASPECT_RATIO] = ratio.name }
    }

    suspend fun saveEpgSettings(interval: EpgUpdateInterval, shift: Float) {
        internalDataStore.edit { 
            it[EPG_UPDATE_INTERVAL] = interval.name
            it[EPG_TIME_SHIFT] = shift
        }
    }

    suspend fun saveParentalPin(pin: String?) {
        internalDataStore.edit { 
            if (pin == null) it.remove(PARENTAL_PIN) else it[PARENTAL_PIN] = pin 
        }
    }

    suspend fun saveAutostart(enabled: Boolean) {
        internalDataStore.edit { it[AUTOSTART] = enabled }
    }

    suspend fun saveLastPlayedStream(streamId: Long) {
        internalDataStore.edit { it[LAST_PLAYED_STREAM_ID] = streamId }
    }

    suspend fun saveCredentials(host: String, user: String, pass: String) {
        internalDataStore.edit {
            it[HOST_URL] = host
            it[USERNAME] = user
            it[PASSWORD] = pass
        }
    }
}
