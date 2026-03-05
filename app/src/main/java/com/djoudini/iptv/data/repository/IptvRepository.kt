package com.djoudini.iptv.data.repository

import com.djoudini.iptv.data.local.*
import com.djoudini.iptv.data.preferences.SettingsRepository
import com.djoudini.iptv.data.remote.*
import com.djoudini.iptv.domain.model.ProviderType
import com.djoudini.iptv.util.M3uParser
import com.djoudini.iptv.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import androidx.paging.PagingData
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IptvRepository @Inject constructor(
    private val providerDao: ProviderDao,
    private val categoryDao: CategoryDao,
    private val channelDao: ChannelDao,
    private val vodDao: VodDao,
    private val seriesDao: SeriesDao,
    private val epgDao: EpgDao,
    private val vodProgressDao: VodProgressDao,
    private val settingsRepository: SettingsRepository,
    private val okHttpClient: OkHttpClient
) {
    private var cachedService: XtreamService? = null
    private var currentHost: String? = null

    private suspend fun getService(): XtreamService? {
        val hostUrl = settingsRepository.hostUrlFlow.first() ?: return null
        val baseUrl = if (hostUrl.endsWith("/")) hostUrl else "$hostUrl/"

        if (cachedService == null || currentHost != baseUrl) {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            cachedService = retrofit.create(XtreamService::class.java)
            currentHost = baseUrl
        }
        return cachedService
    }

    // --- Authentication ---

    data class LoginResult(val response: XtreamLoginResponse, val providerId: Long)

    suspend fun loginXtream(host: String, user: String, pass: String): Result<LoginResult> = withContext(Dispatchers.IO) {
        try {
            val baseUrl = if (host.endsWith("/")) host else "$host/"
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val service = retrofit.create(XtreamService::class.java)
            val response = service.login(user, pass)

            if (response.userInfo?.status == "Active") {
                // Save to local DB as a provider
                val providerId = providerDao.insertProvider(
                    ProviderEntity(
                        name = "Xtream Provider",
                        type = ProviderType.XTREAM.name,
                        url = host,
                        username = user,
                        password = pass
                    )
                )
                settingsRepository.saveCredentials(host, user, pass)
                Result.Success(LoginResult(response, providerId))
            } else {
                Result.Error("Login fehlgeschlagen. Bitte Zugangsdaten prüfen.")
            }
        } catch (e: Exception) {
            Result.Error("Netzwerkfehler: ${e.localizedMessage}")
        }
    }

    // --- M3U Import ---

    suspend fun importM3u(name: String, inputStream: InputStream, url: String? = null): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val providerId = providerDao.insertProvider(
                ProviderEntity(
                    name = name,
                    type = ProviderType.M3U.name,
                    url = url ?: "local_file"
                )
            )
            val (categories, channels) = M3uParser.parse(inputStream, providerId)
            categoryDao.insertCategories(categories)
            channelDao.insertChannels(channels)
            Result.Success(providerId)
        } catch (e: Exception) {
            Result.Error("Fehler beim M3U-Import: ${e.localizedMessage}")
        }
    }

    // --- Data Fetching (Xtream) ---

    suspend fun fetchAllXtreamCategories(providerId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = settingsRepository.usernameFlow.first() ?: return@withContext Result.Error("Nicht eingeloggt")
            val pass = settingsRepository.passwordFlow.first() ?: return@withContext Result.Error("Nicht eingeloggt")
            val service = getService() ?: return@withContext Result.Error("Service nicht verfügbar")

            val live = service.getLiveCategories(user, pass).map { it.toEntity(providerId, "LIVE") }
            val vod = service.getVodCategories(user, pass).map { it.toEntity(providerId, "VOD") }
            val series = service.getSeriesCategories(user, pass).map { it.toEntity(providerId, "SERIES") }

            categoryDao.insertCategories(live + vod + series)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Fehler beim Laden der Kategorien: ${e.localizedMessage}")
        }
    }

    // --- UI Data Flows ---

    fun getCategories(providerId: Long, type: String): Flow<List<CategoryEntity>> = 
        categoryDao.getCategories(providerId, type)

    fun getChannels(categoryId: String, providerId: Long): Flow<List<ChannelEntity>> = 
        channelDao.getChannelsByCategory(categoryId, providerId)

    fun getVods(categoryId: String, providerId: Long): Flow<List<VodEntity>> = 
        vodDao.getVodsByCategory(categoryId, providerId)

    fun getSeries(categoryId: String, providerId: Long): Flow<List<SeriesEntity>> = 
        seriesDao.getSeriesByCategory(categoryId, providerId)

    // --- Legacy Bridges for UI Compatibility ---
    fun getLiveCategoriesFlow(): Flow<List<CategoryEntity>> = categoryDao.getCategories(1L, "LIVE")
    
    suspend fun getEpgForStream(streamId: String): XtreamEpgResponse? {
        val user = settingsRepository.usernameFlow.first() ?: return null
        val pass = settingsRepository.passwordFlow.first() ?: return null
        return getService()?.getShortEpg(user, pass, "get_short_epg", streamId)
    }

    suspend fun syncLiveStreamsForCategory(categoryId: String) {
        val user = settingsRepository.usernameFlow.first() ?: return
        val pass = settingsRepository.passwordFlow.first() ?: return
        val host = settingsRepository.hostUrlFlow.first() ?: return
        val streams = getService()?.getLiveStreams(user, pass, "get_live_streams", categoryId) ?: return
        
        val entities = streams.map { stream ->
            val baseUrl = if (host.endsWith("/")) host else "$host/"
            ChannelEntity(
                streamId = stream.streamId.toString(),
                name = stream.name,
                logoUrl = stream.streamIcon,
                streamUrl = "${baseUrl}${user}/${pass}/${stream.streamId}",
                categoryId = categoryId,
                providerId = 1L
            )
        }
        channelDao.insertChannels(entities)
    }

    // Paging bridges (placeholder for actual implementation)
    fun getChannelsByCategoryPaging(categoryId: String): Flow<PagingData<ChannelEntity>> = flowOf(PagingData.empty())

    fun getVodCategoriesFlow(): Flow<List<CategoryEntity>> = categoryDao.getCategories(1L, "VOD")
    fun getVodsByCategoryPaging(categoryId: String): Flow<PagingData<VodEntity>> = flowOf(PagingData.empty())
    suspend fun syncVodStreamsForCategory(categoryId: String) {
        val user = settingsRepository.usernameFlow.first() ?: return
        val pass = settingsRepository.passwordFlow.first() ?: return
        val host = settingsRepository.hostUrlFlow.first() ?: return
        val streams = getService()?.getVodStreams(user, pass, "get_vod_streams", categoryId) ?: return
        
        val entities = streams.map { stream ->
            val baseUrl = if (host.endsWith("/")) host else "$host/"
            VodEntity(
                streamId = stream.streamId ?: 0,
                name = stream.name ?: "",
                streamIcon = stream.streamIcon,
                rating = stream.rating,
                containerExtension = stream.containerExtension,
                categoryId = categoryId,
                providerId = 1L,
                directSource = "${baseUrl}movie/${user}/${pass}/${stream.streamId}.${stream.containerExtension ?: "mp4"}"
            )
        }
        vodDao.deleteVodsByCategory(categoryId)
        vodDao.insertVods(entities)
    }

    fun getSeriesCategoriesFlow(): Flow<List<CategoryEntity>> = categoryDao.getCategories(1L, "SERIES")
    fun getSeriesByCategoryPaging(categoryId: String): Flow<PagingData<SeriesEntity>> = flowOf(PagingData.empty())
    suspend fun syncSeriesForCategory(categoryId: String) {
        val user = settingsRepository.usernameFlow.first() ?: return
        val pass = settingsRepository.passwordFlow.first() ?: return
        val streams = getService()?.getSeries(user, pass, "get_series", categoryId) ?: return
        
        val entities = streams.map { stream ->
            SeriesEntity(
                seriesId = stream.seriesId ?: 0,
                name = stream.name,
                cover = stream.cover,
                rating = stream.rating,
                categoryId = categoryId,
                providerId = 1L
            )
        }
        seriesDao.deleteSeriesByCategory(categoryId)
        seriesDao.insertSeries(entities)
    }

    // --- Helper Extensions ---
    private fun XtreamCategory.toEntity(providerId: Long, type: String) = CategoryEntity(
        id = this.id,
        name = this.name,
        type = type,
        providerId = providerId,
        isAdult = this.name.lowercase().contains("adult") || this.name.lowercase().contains("xxx")
    )

    // --- Dashboard Count Methods ---
    suspend fun fetchLiveStreamsCount(): Int {
        return 0 // Placeholder
    }

    suspend fun fetchVodStreamsCount(): Int {
        return 0 // Placeholder
    }

    suspend fun fetchSeriesCount(): Int {
        return 0 // Placeholder
    }

    // --- Onboarding Methods ---
    suspend fun loadCategoriesForOnboarding(providerId: Long): Result<List<CategoryEntity>> {
        return try {
            val result = fetchAllXtreamCategories(providerId)
            when (result) {
                is Result.Success -> {
                    // Fetch categories from DB after syncing
                    val categories = getCategories(providerId, "LIVE").first() +
                            getCategories(providerId, "VOD").first() +
                            getCategories(providerId, "SERIES").first()
                    Result.Success(categories)
                }
                is Result.Error -> Result.Error(result.message)
                else -> Result.Error("Unknown error")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    // --- VOD & Series Info ---
    suspend fun getVodInfo(vodId: Int): XtreamVodInfo? {
        return try {
            val user = settingsRepository.usernameFlow.first() ?: return null
            val pass = settingsRepository.passwordFlow.first() ?: return null
            val service = getService() ?: return null
            service.getVodInfo(user, pass, "get_vod_info", vodId)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getSeriesInfo(seriesId: Int): XtreamSeriesInfoResponse? {
        return try {
            val user = settingsRepository.usernameFlow.first() ?: return null
            val pass = settingsRepository.passwordFlow.first() ?: return null
            val service = getService() ?: return null
            service.getSeriesInfo(user, pass, "get_series_info", seriesId)
        } catch (e: Exception) {
            null
        }
    }
}
