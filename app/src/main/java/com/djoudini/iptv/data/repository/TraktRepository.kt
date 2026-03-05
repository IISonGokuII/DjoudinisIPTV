package com.djoudini.iptv.data.repository

import com.djoudini.iptv.data.preferences.SettingsRepository
import com.djoudini.iptv.data.remote.trakt.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktRepository @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val okHttpClient: OkHttpClient
) {
    private val clientId = "YOUR_TRAKT_CLIENT_ID" // Should be in a secure config
    private val clientSecret = "YOUR_TRAKT_CLIENT_SECRET"

    private val traktService: TraktService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.trakt.tv/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TraktService::class.java)
    }

    suspend fun generateDeviceCode(): TraktDeviceCodeResponse {
        return traktService.generateDeviceCode(TraktDeviceCodeRequest(clientId))
    }

    /**
     * Polls the Trakt API until the user authorizes the app or the code expires.
     */
    fun pollForAccessToken(deviceCode: String, interval: Int): Flow<TraktAuthStatus> = flow {
        emit(TraktAuthStatus.Polling)
        while (true) {
            delay(interval * 1000L)
            try {
                val response = traktService.getAccessToken(
                    TraktAccessTokenRequest(deviceCode, clientId, clientSecret)
                )
                
                if (response.isSuccessful) {
                    val tokenResponse = response.body()!!
                    val userSettings = traktService.getUserSettings(
                        token = "Bearer ${tokenResponse.accessToken}",
                        apiKey = clientId
                    )
                    
                    settingsRepository.saveTraktToken(
                        tokenResponse.accessToken,
                        userSettings.user.username
                    )
                    
                    emit(TraktAuthStatus.Success(userSettings.user.username))
                    break
                } else if (response.code() == 400) {
                    // Pending - keep polling
                    continue
                } else {
                    emit(TraktAuthStatus.Error("Auth failed with code: ${response.code()}"))
                    break
                }
            } catch (e: Exception) {
                emit(TraktAuthStatus.Error(e.localizedMessage ?: "Unknown Error"))
                break
            }
        }
    }

    suspend fun logout() {
        settingsRepository.logoutTrakt()
    }
}

sealed class TraktAuthStatus {
    object Polling : TraktAuthStatus()
    data class Success(val username: String) : TraktAuthStatus()
    data class Error(val message: String) : TraktAuthStatus()
}
