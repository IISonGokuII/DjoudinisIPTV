package com.djoudini.iptv.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Entities (Restored for UI compatibility) ---

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "XTREAM" or "M3U"
    val url: String,
    val username: String? = null,
    val password: String? = null,
    val isActive: Boolean = true
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // "LIVE", "VOD", "SERIES"
    val providerId: Long,
    val isAdult: Boolean = false,
    val isSelected: Boolean = true
)

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val streamId: String,
    val name: String,
    val logoUrl: String?,
    val streamUrl: String,
    val categoryId: String,
    val providerId: Long,
    val isFavorite: Boolean = false,
    val tvgId: String? = null
)

@Entity(tableName = "vods")
data class VodEntity(
    @PrimaryKey val streamId: Int,
    val name: String,
    val streamIcon: String?,
    val rating: Double?,
    val containerExtension: String?,
    val categoryId: String,
    val providerId: Long,
    val directSource: String? = null
)

@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey val seriesId: Int,
    val name: String,
    val cover: String?,
    val rating: Double?,
    val categoryId: String,
    val providerId: Long
)

// --- Added for specifications ---

@Entity(tableName = "epg_events")
data class EpgEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelId: String,
    val startTime: Long,
    val endTime: Long,
    val title: String,
    val description: String? = null,
    val category: String? = null,
    val providerId: Long
)

@Entity(tableName = "vod_progress")
data class VodProgressEntity(
    @PrimaryKey val streamId: Long,
    val position: Long,
    val duration: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val streamId: String,
    val streamType: String, // "LIVE", "VOD", "SERIES"
    val addedAt: Long = System.currentTimeMillis()
)

// --- DAOs (Restored and Extended) ---

@Dao
interface ProviderDao {
    @Query("SELECT * FROM providers")
    fun getAllProviders(): Flow<List<ProviderEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ProviderEntity): Long
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE providerId = :providerId AND type = :type")
    fun getCategories(providerId: Long, type: String): Flow<List<CategoryEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)
    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE categoryId = :categoryId AND providerId = :providerId")
    fun getChannelsByCategory(categoryId: String, providerId: Long): Flow<List<ChannelEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)
}

@Dao
interface VodDao {
    @Query("SELECT * FROM vods WHERE categoryId = :categoryId AND providerId = :providerId")
    fun getVodsByCategory(categoryId: String, providerId: Long): Flow<List<VodEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVods(vods: List<VodEntity>)
    @Query("DELETE FROM vods WHERE categoryId = :categoryId")
    suspend fun deleteVodsByCategory(categoryId: String)
}

@Dao
interface SeriesDao {
    @Query("SELECT * FROM series WHERE categoryId = :categoryId AND providerId = :providerId")
    fun getSeriesByCategory(categoryId: String, providerId: Long): Flow<List<SeriesEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: List<SeriesEntity>)
    @Query("DELETE FROM series WHERE categoryId = :categoryId")
    suspend fun deleteSeriesByCategory(categoryId: String)
}

@Dao
interface EpgDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EpgEventEntity>)
}

@Dao
interface VodProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: VodProgressEntity)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>
    
    @Query("SELECT * FROM favorites WHERE streamType = :type ORDER BY addedAt DESC")
    fun getFavoritesByType(type: String): Flow<List<FavoriteEntity>>
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE streamId = :streamId)")
    fun isFavorite(streamId: String): Flow<Boolean>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)
    
    @Query("DELETE FROM favorites WHERE streamId = :streamId")
    suspend fun removeFavorite(streamId: String)
    
    @Query("DELETE FROM favorites")
    suspend fun clearAllFavorites()
}

// --- Database ---

@Database(
    entities = [
        ProviderEntity::class, 
        CategoryEntity::class, 
        ChannelEntity::class,
        VodEntity::class,
        SeriesEntity::class,
        EpgEventEntity::class, 
        VodProgressEntity::class,
        FavoriteEntity::class
    ], 
    version = 5, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun providerDao(): ProviderDao
    abstract fun categoryDao(): CategoryDao
    abstract fun channelDao(): ChannelDao
    abstract fun vodDao(): VodDao
    abstract fun seriesDao(): SeriesDao
    abstract fun epgDao(): EpgDao
    abstract fun vodProgressDao(): VodProgressDao
    abstract fun favoriteDao(): FavoriteDao
}
