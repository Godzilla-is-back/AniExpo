package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.DownloadedEpisodeEntity
import com.example.data.model.Anime
import com.example.data.model.Episode
import com.example.data.model.StreamQuality
import com.example.data.model.StreamServer
import com.example.data.repository.AnimeRepository
import com.example.ui.components.PressableScale
import com.example.ui.player.AnimePlayerScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.GoogleLoginSheet
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.SakuraGlintAqua
import com.example.ui.theme.SakuraLightPink
import com.example.ui.theme.SakuraPinkGlow
import com.example.ui.theme.SakuraStreamTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.launch

enum class MainTab(val id: String, val title: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    HOME("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    SEARCH("search", "Search", Icons.Filled.Search, Icons.Outlined.Search),
    WATCHLIST("watchlist", "My List", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder),
    DOWNLOADS("downloads", "Offline", Icons.Filled.Download, Icons.Outlined.Download),
    SETTINGS("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun SakuraMainApp(repository: AnimeRepository) {
    val coroutineScope = rememberCoroutineScope()

    // Pre-seed initial data on startup
    LaunchedEffect(Unit) {
        repository.seedInitialDataIfEmpty()
    }

    // State flows from repository
    val settings by repository.settings.collectAsStateWithLifecycle()
    val userProfile by repository.currentUser.collectAsStateWithLifecycle()
    val watchlist by repository.watchlist.collectAsStateWithLifecycle(initialValue = emptyList())
    val continueWatching by repository.continueWatching.collectAsStateWithLifecycle(initialValue = emptyList())
    val downloads by repository.downloads.collectAsStateWithLifecycle(initialValue = emptyList())
    val isOfflineModeOnly by repository.offlineModeOnly.collectAsStateWithLifecycle()

    val liveAnimeList by repository.animeList.collectAsStateWithLifecycle()
    val latestReleases by repository.latestReleases.collectAsStateWithLifecycle()
    val trendingAnime by repository.trendingAnime.collectAsStateWithLifecycle()
    val topRatedAnime by repository.topRatedAnime.collectAsStateWithLifecycle()
    val isSyncing by repository.isSyncing.collectAsStateWithLifecycle()
    val syncStatusText by repository.syncStatusText.collectAsStateWithLifecycle()

    val bookmarkedIds = remember(watchlist) { watchlist.map { it.animeId }.toSet() }
    val downloadedEpisodeIds = remember(downloads) { downloads.map { it.episodeId }.toSet() }

    // Navigation and screen stack state
    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var selectedAnime by remember { mutableStateOf<Anime?>(null) }
    var playingEpisodeInfo by remember { mutableStateOf<Pair<Anime, Episode>?>(null) }
    var playingInitialPosition by remember { mutableStateOf(0L) }
    var showGoogleLoginSheet by remember { mutableStateOf(false) }

    SakuraStreamTheme(themeMode = settings.themeMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (playingEpisodeInfo != null) {
                // Active Full-screen Anime Video Player
                val (anime, episode) = playingEpisodeInfo!!
                val episodes = repository.getEpisodesForAnime(anime.id)

                AnimePlayerScreen(
                    anime = anime,
                    episode = episode,
                    allEpisodes = episodes,
                    initialPositionMs = playingInitialPosition,
                    initialServer = settings.defaultServer,
                    initialQuality = settings.defaultQuality,
                    onBack = {
                        playingEpisodeInfo = null
                        playingInitialPosition = 0L
                    },
                    onSelectEpisode = { newEp ->
                        playingEpisodeInfo = anime to newEp
                        playingInitialPosition = 0L
                    },
                    onDownloadEpisode = { ep, server, quality ->
                        coroutineScope.launch {
                            repository.startDownload(anime, ep, server, quality)
                        }
                    },
                    onSaveProgress = { ep, pos, dur, server ->
                        coroutineScope.launch {
                            repository.saveWatchProgress(ep, anime, server, pos, dur)
                        }
                    }
                )
            } else if (selectedAnime != null) {
                // Detail Screen for chosen anime
                val anime = selectedAnime!!
                val episodes = repository.getEpisodesForAnime(anime.id)

                DetailScreen(
                    anime = anime,
                    episodes = episodes,
                    isBookmarked = bookmarkedIds.contains(anime.id),
                    downloadedEpisodeIds = downloadedEpisodeIds,
                    currentServer = settings.defaultServer,
                    onBack = { selectedAnime = null },
                    onWatchEpisode = { ep ->
                        playingEpisodeInfo = anime to ep
                        playingInitialPosition = 0L
                    },
                    onToggleBookmark = {
                        coroutineScope.launch {
                            if (bookmarkedIds.contains(anime.id)) {
                                repository.removeFromWatchlist(anime.id)
                            } else {
                                repository.toggleWatchlist(anime)
                            }
                        }
                    },
                    onSelectServer = { server ->
                        repository.updateSettings(settings.copy(defaultServer = server))
                    },
                    onDownloadEpisode = { ep, server, quality ->
                        coroutineScope.launch {
                            repository.startDownload(anime, ep, server, quality)
                        }
                    }
                )
            } else {
                // Main Tabbed Navigation Scaffold
                Scaffold(
                    bottomBar = {
                        NextGenBottomNav(
                            currentTab = currentTab,
                            onTabSelected = { currentTab = it },
                            downloadsCount = downloads.size
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        Crossfade(
                            targetState = currentTab,
                            animationSpec = tween(280, easing = FastOutSlowInEasing),
                            label = "tab_crossfade"
                        ) { tab ->
                            when (tab) {
                                MainTab.HOME -> {
                                    HomeScreen(
                                        animeList = liveAnimeList,
                                        latestReleases = latestReleases,
                                        trendingAnime = trendingAnime,
                                        topRatedAnime = topRatedAnime,
                                        isSyncingWithAniList = isSyncing,
                                        syncStatusText = syncStatusText,
                                        onSyncWithAniList = {
                                            coroutineScope.launch {
                                                repository.syncWithAniListAndMal()
                                            }
                                        },
                                        continueWatchingList = continueWatching,
                                        bookmarkedIds = bookmarkedIds,
                                        currentServer = settings.defaultServer,
                                        userProfile = userProfile,
                                        isOfflineMode = isOfflineModeOnly,
                                        onSelectAnime = { selectedAnime = it },
                                        onWatchAnime = { anime ->
                                            val firstEp = repository.getEpisodesForAnime(anime.id).firstOrNull()
                                            if (firstEp != null) {
                                                playingEpisodeInfo = anime to firstEp
                                                playingInitialPosition = 0L
                                            }
                                        },
                                        onResumeWatch = { hist ->
                                            val anime = repository.getAnimeById(hist.animeId)
                                            if (anime != null) {
                                                val ep = repository.getEpisodesForAnime(anime.id)
                                                    .find { it.id == hist.episodeId }
                                                    ?: repository.getEpisodesForAnime(anime.id).firstOrNull()
                                                if (ep != null) {
                                                    playingEpisodeInfo = anime to ep
                                                    playingInitialPosition = hist.positionMs
                                                }
                                            }
                                        },
                                        onToggleBookmark = { anime ->
                                            coroutineScope.launch {
                                                if (bookmarkedIds.contains(anime.id)) {
                                                    repository.removeFromWatchlist(anime.id)
                                                } else {
                                                    repository.toggleWatchlist(anime)
                                                }
                                            }
                                        },
                                        onSelectServer = { server ->
                                            repository.updateSettings(settings.copy(defaultServer = server))
                                        },
                                        onToggleOfflineMode = {
                                            repository.toggleOfflineMode()
                                        },
                                        onOpenSearch = { currentTab = MainTab.SEARCH },
                                        onOpenGoogleLogin = { showGoogleLoginSheet = true }
                                    )
                                }

                                MainTab.SEARCH -> {
                                    SearchScreen(
                                        animeList = liveAnimeList,
                                        bookmarkedIds = bookmarkedIds,
                                        onSelectAnime = { selectedAnime = it },
                                        onToggleBookmark = { anime ->
                                            coroutineScope.launch {
                                                if (bookmarkedIds.contains(anime.id)) {
                                                    repository.removeFromWatchlist(anime.id)
                                                } else {
                                                    repository.toggleWatchlist(anime)
                                                }
                                            }
                                        },
                                        onSearchOnline = { query ->
                                            repository.searchLiveAnime(query)
                                        }
                                    )
                                }

                                MainTab.WATCHLIST -> {
                                    WatchlistScreen(
                                        watchlist = watchlist,
                                        userProfile = userProfile,
                                        onSelectAnimeId = { id ->
                                            val a = repository.getAnimeById(id)
                                            if (a != null) selectedAnime = a
                                        },
                                        onRemoveFromWatchlist = { id ->
                                            coroutineScope.launch {
                                                repository.removeFromWatchlist(id)
                                            }
                                        },
                                        onOpenGoogleLogin = { showGoogleLoginSheet = true },
                                        onExploreAnime = { currentTab = MainTab.HOME }
                                    )
                                }

                                MainTab.DOWNLOADS -> {
                                    DownloadsScreen(
                                        downloads = downloads,
                                        isOfflineModeOnly = isOfflineModeOnly,
                                        onToggleOfflineMode = {
                                            repository.toggleOfflineMode()
                                        },
                                        onPlayOfflineEpisode = { dl ->
                                            val anime = repository.getAnimeById(dl.animeId) ?: Anime(
                                                id = dl.animeId,
                                                title = dl.animeTitle,
                                                coverUrl = dl.coverUrl,
                                                score = 90,
                                                genres = listOf("Offline Stream"),
                                                episodesCount = 12,
                                                description = "Offline Saved Content"
                                            )
                                            val ep = Episode(
                                                id = dl.episodeId,
                                                animeId = dl.animeId,
                                                episodeNum = dl.episodeNum,
                                                title = dl.episodeTitle,
                                                thumbnail = dl.coverUrl,
                                                videoUrl = dl.videoUrl
                                            )
                                            playingEpisodeInfo = anime to ep
                                            playingInitialPosition = 0L
                                        },
                                        onDeleteDownload = { epId ->
                                            coroutineScope.launch {
                                                repository.deleteDownload(epId)
                                            }
                                        },
                                        onClearAllDownloads = {
                                            coroutineScope.launch {
                                                repository.clearDownloads()
                                            }
                                        }
                                    )
                                }

                                MainTab.SETTINGS -> {
                                    SettingsScreen(
                                        settings = settings,
                                        userProfile = userProfile,
                                        onUpdateSettings = { newS ->
                                            repository.updateSettings(newS)
                                        },
                                        onOpenGoogleLogin = { showGoogleLoginSheet = true },
                                        onClearCache = {
                                            coroutineScope.launch {
                                                // Clears temporary buffer
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Google Login Bottom Sheet
            if (showGoogleLoginSheet) {
                GoogleLoginSheet(
                    userProfile = userProfile,
                    onDismiss = { showGoogleLoginSheet = false },
                    onLoginSuccess = { email, name ->
                        repository.setGoogleUser(email, name)
                    },
                    onLogout = {
                        repository.logoutGoogle()
                    }
                )
            }
        }
    }
}

/**
 * Next-gen Glassmorphic Floating Bottom Navigation Bar
 */
@Composable
fun NextGenBottomNav(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    downloadsCount: Int
) {
    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainTab.entries.forEach { tab ->
                val isSelected = tab == currentTab

                PressableScale(
                    scaleDownTo = 0.88f,
                    onClick = { onTabSelected(tab) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        if (tab == MainTab.DOWNLOADS && downloadsCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = SakuraPinkGlow,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = "$downloadsCount",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) SakuraLightPink else TextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                contentDescription = tab.title,
                                tint = if (isSelected) SakuraLightPink else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = tab.title,
                            color = if (isSelected) SakuraLightPink else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )

                        // Active pill indicator
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .height(3.dp)
                                .width(if (isSelected) 16.dp else 0.dp)
                                .background(
                                    if (isSelected) SakuraPinkGlow else Color.Transparent,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}
