package com.djoudini.iptv.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming

interface XtreamService {
    @GET("player_api.php")
    suspend fun login(
        @Query("username") user: String,
        @Query("password") pass: String
    ): XtreamLoginResponse

    @Streaming
    @GET("player_api.php")
    suspend fun getLiveStreamsRaw(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_live_streams"
    ): ResponseBody

    @Streaming
    @GET("player_api.php")
    suspend fun getVodStreamsRaw(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_vod_streams"
    ): ResponseBody

    @Streaming
    @GET("player_api.php")
    suspend fun getSeriesRaw(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_series"
    ): ResponseBody

    @GET("player_api.php")
    suspend fun getLiveCategories(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_live_categories"
    ): List<XtreamCategory>

    @GET("player_api.php")
    suspend fun getVodCategories(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_vod_categories"
    ): List<XtreamCategory>

    @GET("player_api.php")
    suspend fun getSeriesCategories(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_series_categories"
    ): List<XtreamCategory>

    @GET("player_api.php")
    suspend fun getLiveStreams(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_live_streams",
        @Query("category_id") categoryId: String? = null
    ): List<XtreamStream>

    @GET("player_api.php")
    suspend fun getVodStreams(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_vod_streams",
        @Query("category_id") categoryId: String? = null
    ): List<XtreamVodStream>

    @GET("player_api.php")
    suspend fun getSeries(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_series",
        @Query("category_id") categoryId: String? = null
    ): List<XtreamSeries>

    @GET("player_api.php")
    suspend fun getShortEpg(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_short_epg",
        @Query("stream_id") streamId: String
    ): XtreamEpgResponse

    @GET("player_api.php")
    suspend fun getVodInfo(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_vod_info",
        @Query("vod_id") vodId: Int
    ): XtreamVodInfo

    @GET("player_api.php")
    suspend fun getSeriesInfo(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_series_info",
        @Query("series_id") seriesId: Int
    ): XtreamSeriesInfoResponse
}

data class XtreamSeriesInfoResponse(
    @SerializedName("seasons") val seasons: List<XtreamSeason>?,
    @SerializedName("info") val info: XtreamSeriesInfo?,
    @SerializedName("episodes") val episodes: Map<String, List<XtreamEpisode>>?
)

data class XtreamSeason(
    @SerializedName("air_date") val airDate: String?,
    @SerializedName("episode_count") val episodeCount: Int?,
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("season_number") val seasonNumber: Int?,
    @SerializedName("cover") val cover: String?,
    @SerializedName("cover_big") val coverBig: String?
)

data class XtreamSeriesInfo(
    @SerializedName("name") val name: String?,
    @SerializedName("cover") val cover: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("cast") val cast: String?,
    @SerializedName("director") val director: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("releaseDate") val releaseDate: String?,
    @SerializedName("last_modified") val lastModified: String?,
    @SerializedName("rating") val rating: Double?,
    @SerializedName("rating_5based") val rating5Based: Double?,
    @SerializedName("backdrop_path") val backdropPath: List<String>?,
    @SerializedName("youtube_trailer") val youtubeTrailer: String?,
    @SerializedName("episode_run_time") val episodeRunTime: String?,
    @SerializedName("category_id") val categoryId: String?
)

data class XtreamEpisode(
    @SerializedName("id") val id: String,
    @SerializedName("episode_num") val episodeNum: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("container_extension") val containerExtension: String?,
    @SerializedName("info") val info: XtreamEpisodeInfo?,
    @SerializedName("custom_sid") val customSid: String?,
    @SerializedName("added") val added: String?,
    @SerializedName("season") val season: Int?,
    @SerializedName("direct_source") val directSource: String?
)

data class XtreamEpisodeInfo(
    @SerializedName("movie_image") val movieImage: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("releasedate") val releaseDate: String?,
    @SerializedName("rating") val rating: Double?,
    @SerializedName("duration_secs") val durationSecs: Int?,
    @SerializedName("duration") val duration: String?
)

data class XtreamEpgResponse(
    @SerializedName("epg_listings") val epgListings: List<XtreamEpgListing>?
)

data class XtreamEpgListing(
    @SerializedName("id") val id: String,
    @SerializedName("epg_id") val epgId: String,
    @SerializedName("title") val title: String,
    @SerializedName("lang") val lang: String?,
    @SerializedName("start") val start: String,
    @SerializedName("end") val end: String,
    @SerializedName("description") val description: String,
    @SerializedName("channel_id") val channelId: String,
    @SerializedName("start_timestamp") val startTimestamp: Long,
    @SerializedName("stop_timestamp") val stopTimestamp: Long
)

data class XtreamVodStream(
    @SerializedName("num") val num: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("stream_type") val streamType: String?,
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("rating") val rating: Double?,
    @SerializedName("rating_5based") val rating5Based: Double?,
    @SerializedName("added") val added: String?,
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("container_extension") val containerExtension: String?,
    @SerializedName("custom_sid") val customSid: String?,
    @SerializedName("direct_source") val directSource: String?
)

data class XtreamSeries(
    @SerializedName("num") val num: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("series_id") val seriesId: Int,
    @SerializedName("cover") val cover: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("cast") val cast: String?,
    @SerializedName("director") val director: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("releaseDate") val releaseDate: String?,
    @SerializedName("last_modified") val lastModified: String?,
    @SerializedName("rating") val rating: Double?,
    @SerializedName("rating_5based") val rating5Based: Double?,
    @SerializedName("backdrop_path") val backdropPath: List<String>?,
    @SerializedName("youtube_trailer") val youtubeTrailer: String?,
    @SerializedName("episode_run_time") val episodeRunTime: String?,
    @SerializedName("category_id") val categoryId: String
)

data class XtreamVodInfo(
    @SerializedName("info") val info: XtreamVodInfoDetails?,
    @SerializedName("movie_data") val movieData: XtreamVodStream?
)

data class XtreamVodInfoDetails(
    @SerializedName("kinopoisk_url") val kinopoiskUrl: String?,
    @SerializedName("tmdb_id") val tmdbId: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("o_name") val originalName: String?,
    @SerializedName("cover_big") val coverBig: String?,
    @SerializedName("movie_image") val movieImage: String?,
    @SerializedName("releasedate") val releaseDate: String?,
    @SerializedName("episode_run_time") val episodeRunTime: String?,
    @SerializedName("youtube_trailer") val youtubeTrailer: String?,
    @SerializedName("director") val director: String?,
    @SerializedName("actors") val actors: String?,
    @SerializedName("cast") val cast: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("age") val age: String?,
    @SerializedName("mpaa_rating") val mpaaRating: String?,
    @SerializedName("rating_count_kinopoisk") val ratingCountKinopoisk: Int?,
    @SerializedName("country") val country: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("backdrop_path") val backdropPath: List<String>?,
    @SerializedName("duration_secs") val durationSecs: Int?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("video") val video: VideoInfo?,
    @SerializedName("audio") val audio: AudioInfo?,
    @SerializedName("bitrate") val bitrate: Int?,
    @SerializedName("rating") val rating: Double?
)

data class VideoInfo(
    @SerializedName("index") val index: Int?,
    @SerializedName("codec_name") val codecName: String?,
    @SerializedName("codec_long_name") val codecLongName: String?,
    @SerializedName("profile") val profile: String?,
    @SerializedName("codec_type") val codecType: String?,
    @SerializedName("codec_time_base") val codecTimeBase: String?,
    @SerializedName("codec_tag_string") val codecTagString: String?,
    @SerializedName("codec_tag") val codecTag: String?,
    @SerializedName("width") val width: Int?,
    @SerializedName("height") val height: Int?,
    @SerializedName("coded_width") val codedWidth: Int?,
    @SerializedName("coded_height") val codedHeight: Int?,
    @SerializedName("has_b_frames") val hasBFrames: Int?,
    @SerializedName("sample_aspect_ratio") val sampleAspectRatio: String?,
    @SerializedName("display_aspect_ratio") val displayAspectRatio: String?,
    @SerializedName("pix_fmt") val pixFmt: String?,
    @SerializedName("level") val level: Int?,
    @SerializedName("color_range") val colorRange: String?,
    @SerializedName("color_space") val colorSpace: String?,
    @SerializedName("color_transfer") val colorTransfer: String?,
    @SerializedName("color_primaries") val colorPrimaries: String?,
    @SerializedName("chroma_location") val chromaLocation: String?,
    @SerializedName("field_order") val fieldOrder: String?,
    @SerializedName("timecode") val timecode: String?,
    @SerializedName("refs") val refs: Int?,
    @SerializedName("is_avc") val isAvc: String?,
    @SerializedName("nal_length_size") val nalLengthSize: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("r_frame_rate") val rFrameRate: String?,
    @SerializedName("avg_frame_rate") val avgFrameRate: String?,
    @SerializedName("time_base") val timeBase: String?,
    @SerializedName("start_pts") val startPts: Long?,
    @SerializedName("start_time") val startTime: String?,
    @SerializedName("duration_ts") val durationTs: Long?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("bit_rate") val bitRate: String?,
    @SerializedName("max_bit_rate") val maxBitRate: String?,
    @SerializedName("bits_per_raw_sample") val bitsPerRawSample: String?,
    @SerializedName("nb_frames") val nbFrames: String?
)

data class AudioInfo(
    @SerializedName("index") val index: Int?,
    @SerializedName("codec_name") val codecName: String?,
    @SerializedName("codec_long_name") val codecLongName: String?,
    @SerializedName("profile") val profile: String?,
    @SerializedName("codec_type") val codecType: String?,
    @SerializedName("codec_time_base") val codecTimeBase: String?,
    @SerializedName("codec_tag_string") val codecTagString: String?,
    @SerializedName("codec_tag") val codecTag: String?,
    @SerializedName("sample_fmt") val sampleFmt: String?,
    @SerializedName("sample_rate") val sampleRate: String?,
    @SerializedName("channels") val channels: Int?,
    @SerializedName("channel_layout") val channelLayout: String?,
    @SerializedName("bits_per_sample") val bitsPerSample: Int?,
    @SerializedName("id") val id: String?,
    @SerializedName("r_frame_rate") val rFrameRate: String?,
    @SerializedName("avg_frame_rate") val avgFrameRate: String?,
    @SerializedName("time_base") val timeBase: String?,
    @SerializedName("start_pts") val startPts: Long?,
    @SerializedName("start_time") val startTime: String?,
    @SerializedName("duration_ts") val durationTs: Long?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("bit_rate") val bitRate: String?,
    @SerializedName("max_bit_rate") val maxBitRate: String?,
    @SerializedName("nb_frames") val nbFrames: String?
)

data class XtreamLoginResponse(
    @SerializedName("user_info") val userInfo: XtreamUserInfo?,
    @SerializedName("server_info") val serverInfo: XtreamServerInfo?
)

data class XtreamUserInfo(
    val username: String,
    val status: String,
    val exp_date: String?,
    val is_trial: String?,
    val active_cons: String?,
    val max_connections: String?
)

data class XtreamServerInfo(
    val url: String,
    val port: String,
    val https_port: String?,
    val server_protocol: String?,
    val rtmp_port: String?,
    val timezone: String?
)

data class XtreamCategory(
    @SerializedName("category_id") val id: String,
    @SerializedName("category_name") val name: String,
    @SerializedName("parent_id") val parentId: Int?
)

data class XtreamStream(
    @SerializedName("num") val num: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("stream_type") val streamType: String?,
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("epg_channel_id") val epgChannelId: String?,
    @SerializedName("added") val added: String?,
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("custom_sid") val customSid: String?,
    @SerializedName("tv_archive") val tvArchive: Int?,
    @SerializedName("direct_source") val directSource: String?,
    @SerializedName("tv_archive_duration") val tvArchiveDuration: Int?
)
