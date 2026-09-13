package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {

    // --- Watchlist ---
    @Query("SELECT * FROM watchlist WHERE userEmail = :userEmail ORDER BY addedTimestamp DESC")
    fun getWatchlistForUser(userEmail: String): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist ORDER BY addedTimestamp DESC")
    fun getAllWatchlist(): Flow<List<WatchlistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE animeId = :animeId)")
    fun isInWatchlist(animeId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE animeId = :animeId")
    suspend fun removeFromWatchlist(animeId: Int)

    // --- Watch History / Continue Watching ---
    @Query("SELECT * FROM watch_history ORDER BY lastWatchedTimestamp DESC LIMIT 15")
    fun getContinueWatching(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateWatchHistory(item: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE episodeId = :episodeId")
    suspend fun deleteHistory(episodeId: String)

    // --- Offline Saved Content / Downloads ---
    @Query("SELECT * FROM downloads ORDER BY downloadedTimestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadedEpisodeEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM downloads WHERE episodeId = :episodeId AND isCompleted = 1)")
    fun isDownloaded(episodeId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(item: DownloadedEpisodeEntity)

    @Query("DELETE FROM downloads WHERE episodeId = :episodeId")
    suspend fun deleteDownload(episodeId: String)

    @Query("DELETE FROM downloads")
    suspend fun clearAllDownloads()
}
