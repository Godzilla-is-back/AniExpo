package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val animeId: Int,
    val title: String,
    val coverUrl: String,
    val score: Int,
    val genres: String, // comma-separated
    val userEmail: String,
    val status: String = "Watching", // Watching, Plan to Watch, Completed
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val episodeId: String,
    val animeId: Int,
    val animeTitle: String,
    val episodeTitle: String,
    val episodeNum: Int,
    val coverUrl: String,
    val videoUrl: String,
    val serverName: String,
    val positionMs: Long,
    val durationMs: Long,
    val userEmail: String,
    val lastWatchedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadedEpisodeEntity(
    @PrimaryKey val episodeId: String,
    val animeId: Int,
    val animeTitle: String,
    val episodeTitle: String,
    val episodeNum: Int,
    val coverUrl: String,
    val videoUrl: String,
    val serverName: String,
    val quality: String,
    val fileSizeBytes: Long = 284000000L, // ~284 MB
    val isCompleted: Boolean = true,
    val progressPercent: Int = 100,
    val downloadedTimestamp: Long = System.currentTimeMillis()
)
