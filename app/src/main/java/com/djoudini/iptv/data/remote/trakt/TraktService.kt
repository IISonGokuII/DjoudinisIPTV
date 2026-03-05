package com.djoudini.iptv.data.remote.trakt

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface TraktService {

    @POST("oauth/device/code")
    suspend fun generateDeviceCode(
        @Body request: TraktDeviceCodeRequest
    ): TraktDeviceCodeResponse

    @POST("oauth/device/token")
    suspend fun getAccessToken(
        @Body request: TraktAccessTokenRequest
    ): Response<TraktAccessTokenResponse>

    @GET("users/settings")
    suspend fun getUserSettings(
        @Header("Authorization") token: String,
        @Header("trakt-api-key") apiKey: String,
        @Header("trakt-api-version") apiVersion: String = "2"
    ): TraktUserSettings
}

data class TraktDeviceCodeRequest(
    @SerializedName("client_id") val clientId: String
)

data class TraktDeviceCodeResponse(
    @SerializedName("device_code") val deviceCode: String,
    @SerializedName("user_code") val userCode: String,
    @SerializedName("verification_url") val verificationUrl: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("interval") val interval: Int
)

data class TraktAccessTokenRequest(
    val code: String,
    @SerializedName("client_id") val clientId: String,
    @SerializedName("client_secret") val clientSecret: String
)

data class TraktAccessTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("scope") val scope: String,
    @SerializedName("created_at") val createdAt: Long
)

data class TraktUserSettings(
    val user: TraktUser
)

data class TraktUser(
    val username: String,
    val name: String?
)
