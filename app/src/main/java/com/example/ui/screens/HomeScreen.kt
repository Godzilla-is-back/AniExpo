package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.Anime
import com.example.data.model.StreamServer
import com.example.data.model.UserProfile
import com.example.ui.components.AnimePosterCard
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.GlowingWatchButton
import com.example.ui.components.OfflineStatusPill
import com.example.ui.components.PressableScale
import com.example.ui.components.SectionHeader
import com.example.ui.components.ServerLatencyBadge
import com.example.ui.theme.SakuraGlintAqua
import com.example.ui.theme.SakuraLightPink
import com.example.ui.theme.SakuraPinkGlow
import com.example.ui.theme.StreamGoodGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    animeList: List<Anime>,
    latestReleases: List<Anime> = emptyList(),
    trendingAnime: List<Anime> = emptyList(),
    topRatedAnime: List<Anime> = emptyList(),
    isSyncingWithAniList: Boolean = false,
    syncStatusText: String = "AniList & MyAnimeList Connected",
    onSyncWithAniList: () -> Unit = {},
    continueWatchingList: List<WatchHistoryEntity>,
    bookmarkedIds: Set<Int>,
    currentServer: StreamServer,
    userProfile: UserProfile,
    isOfflineMode: Boolean,
    onSelectAnime: (Anime) -> Unit,
    onWatchAnime: (Anime) -> Unit,
    onResumeWatch: (WatchHistoryEntity) -> Unit,
    onToggleBookmark: (Anime) -> Unit,
    onSelectServer: (StreamServer) -> Unit,
    onToggleOfflineMode: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenGoogleLogin: () -> Unit
) {
    val heroList = remember(trendingAnime, animeList) {
        if (trendingAnime.isNotEmpty()) trendingAnime.take(4) else animeList.take(4)
    }
    var heroIndex by remember { mutableIntStateOf(0) }

    // Auto-cycle hero banner every 6.5s
    LaunchedEffect(heroList.size) {
        if (heroList.isNotEmpty()) {
            while (true) {
                delay(6500)
                heroIndex = (heroIndex + 1) % heroList.size
            }
        }
    }

    val currentHero = heroList.getOrNull(heroIndex) ?: animeList.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. App Header Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 48.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Logo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(listOf(SakuraLightPink, SakuraPinkGlow))
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp))
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SakuraStream",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.3.sp
                        )
                        Text(
                            text = "Next-Gen Anime Hub",
                            color = SakuraLightPink,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Header Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Offline indicator toggle
                    OfflineStatusPill(
                        isOffline = isOfflineMode,
                        onClick = onToggleOfflineMode
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Search Button
                    IconButton(
                        onClick = onOpenSearch,
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Anime",
                            tint = TextWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Google Profile Avatar
                    PressableScale(onClick = onOpenGoogleLogin) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (userProfile.isGoogleLoggedIn)
                                        Brush.linearGradient(
                                            listOf(Color(0xFF4285F4), Color(0xFFEA4335), Color(0xFFFBBC05), Color(0xFF34A853))
                                        )
                                    else Brush.linearGradient(
                                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile.displayName.firstOrNull()?.uppercase() ?: "U",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Green cloud sync dot
                            if (userProfile.isGoogleLoggedIn) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(10.dp)
                                        .background(StreamGoodGreen, CircleShape)
                                        .border(1.dp, MaterialTheme.colorScheme.background, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 1.5 AniList & MyAnimeList Live Sync Status Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSyncingWithAniList) SakuraLightPink.copy(alpha = 0.5f) else Color(0x33FFFFFF)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Pulsing status dot
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSyncingWithAniList) SakuraPinkGlow else StreamGoodGreen
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isSyncingWithAniList) "Connecting to AniList & MAL..." else "AniList & MAL Live Synced",
                                color = TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = syncStatusText,
                                color = SakuraGlintAqua,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Sync action button
                    PressableScale(onClick = onSyncWithAniList) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSyncingWithAniList) SakuraPinkGlow.copy(alpha = 0.4f) else Color(0x33FFFFFF),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, SakuraLightPink.copy(alpha = 0.4f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                if (isSyncingWithAniList) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        color = SakuraLightPink,
                                        strokeWidth = 1.5.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Sync",
                                        tint = SakuraLightPink,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSyncingWithAniList) "Syncing" else "Sync",
                                    color = TextWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Next-Gen Hero Banner Carousel
        if (currentHero != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(390.dp)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    // Hero Background Art
                    if (currentHero.bannerDrawableRes != null) {
                        Image(
                            painter = painterResource(id = currentHero.bannerDrawableRes),
                            contentDescription = currentHero.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = currentHero.bannerUrl.ifEmpty { currentHero.coverUrl },
                            contentDescription = currentHero.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Multi-layer Gradient Scrim for atmospheric depth
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x33000000),
                                        Color(0x55090611),
                                        Color(0xF0090611)
                                    )
                                )
                            )
                    )

                    // Hero Content Box
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(18.dp)
                    ) {
                        // Tags row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SakuraPinkGlow.copy(alpha = 0.9f)
                            ) {
                                Text(
                                    text = "FEATURED",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x99000000)
                            ) {
                                Text(
                                    text = "★ ${currentHero.score}% RATING",
                                    color = SakuraGlintAqua,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Title
                        Text(
                            text = currentHero.title,
                            color = TextWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 28.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = currentHero.genres.take(3).joinToString("  •  ") + "  •  " + currentHero.studio,
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                        )

                        // Action Buttons: Replacing Trailer with glowing Watch Button!
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            GlowingWatchButton(
                                label = "Watch Ep 1",
                                subtitle = "on ${currentServer.displayName.substringBefore(" ")}",
                                onClick = { onWatchAnime(currentHero) }
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            // Add to watchlist button
                            val isBookmarked = bookmarkedIds.contains(currentHero.id)
                            PressableScale(
                                onClick = { onToggleBookmark(currentHero) }
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(22.dp),
                                    color = if (isBookmarked) MaterialTheme.colorScheme.primaryContainer else Color(0x66150F22),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isBookmarked) SakuraLightPink else Color(0x66FFFFFF)
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = "Bookmark",
                                            tint = if (isBookmarked) SakuraLightPink else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isBookmarked) "In List" else "+ My List",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Details Info Button
                            IconButton(
                                onClick = { onSelectAnime(currentHero) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0x66150F22), CircleShape)
                                    .border(1.dp, Color(0x44FFFFFF), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Details",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Carousel Dots indicator
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        heroList.forEachIndexed { idx, _ ->
                            val isActive = idx == heroIndex
                            val width by animateFloatAsState(
                                targetValue = if (isActive) 22f else 6f,
                                animationSpec = tween(300, easing = FastOutSlowInEasing),
                                label = "dot_width"
                            )
                            Box(
                                modifier = Modifier
                                    .height(5.dp)
                                    .width(width.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (isActive) SakuraLightPink else Color.White.copy(alpha = 0.35f)
                                    )
                            )
                        }
                    }
                }
            }
        }

        // 3. Streaming Server Selector Quick-Bar (Anikoto, AniDB, ZoroCloud, etc.)
        item {
            Column(modifier = Modifier.padding(top = 18.dp)) {
                SectionHeader(
                    title = "Streaming Servers",
                    actionLabel = null,
                    onAction = null
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(StreamServer.entries) { server ->
                        ServerLatencyBadge(
                            server = server,
                            isSelected = server == currentServer,
                            onClick = { onSelectServer(server) }
                        )
                    }
                }
            }
        }

        // 4. Continue Watching (if any history exists)
        if (continueWatchingList.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 18.dp)) {
                    SectionHeader(
                        title = "Continue Watching",
                        actionLabel = null,
                        onAction = null
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(continueWatchingList) { history ->
                            ContinueWatchingCard(
                                history = history,
                                onClick = { onResumeWatch(history) }
                            )
                        }
                    }
                }
            }
        }

        // 5. Latest Releases (Airing Now on AniList & MAL)
        val displayLatest = if (latestReleases.isNotEmpty()) latestReleases else animeList.filter { it.status == "Airing" }
        item {
            Column(modifier = Modifier.padding(top = 22.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Latest Releases",
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SakuraPinkGlow.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, SakuraLightPink)
                        ) {
                            Text(
                                text = "LIVE AIRING",
                                color = SakuraLightPink,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "See all",
                        color = SakuraGlintAqua,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onOpenSearch)
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayLatest) { anime ->
                        AnimePosterCard(
                            anime = anime,
                            isBookmarked = bookmarkedIds.contains(anime.id),
                            onBookmarkToggle = { onToggleBookmark(anime) },
                            onClick = { onSelectAnime(anime) }
                        )
                    }
                }
            }
        }

        // 6. Trending on AniList
        val displayTrending = if (trendingAnime.isNotEmpty()) trendingAnime else animeList.sortedByDescending { it.score }
        item {
            Column(modifier = Modifier.padding(top = 22.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Trending on AniList",
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x3302A9FF),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFF02A9FF))
                        ) {
                            Text(
                                text = "ANILIST HOT",
                                color = Color(0xFF02A9FF),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "See all",
                        color = SakuraGlintAqua,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onOpenSearch)
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayTrending) { anime ->
                        AnimePosterCard(
                            anime = anime,
                            isBookmarked = bookmarkedIds.contains(anime.id),
                            onBookmarkToggle = { onToggleBookmark(anime) },
                            onClick = { onSelectAnime(anime) }
                        )
                    }
                }
            }
        }

        // 7. Top Rated Masterpieces (AniList & MAL)
        val displayTopRated = if (topRatedAnime.isNotEmpty()) topRatedAnime else animeList.sortedByDescending { it.score }
        item {
            Column(modifier = Modifier.padding(top = 22.dp)) {
                SectionHeader(
                    title = "Top Rated Masterpieces",
                    actionLabel = "See all",
                    onAction = onOpenSearch
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayTopRated) { anime ->
                        AnimePosterCard(
                            anime = anime,
                            isBookmarked = bookmarkedIds.contains(anime.id),
                            onBookmarkToggle = { onToggleBookmark(anime) },
                            onClick = { onSelectAnime(anime) }
                        )
                    }
                }
            }
        }

        // 8. Action & Shonen Hits
        item {
            Column(modifier = Modifier.padding(top = 22.dp)) {
                SectionHeader(
                    title = "Action & Shonen Legends",
                    actionLabel = "Explore",
                    onAction = onOpenSearch
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(animeList.filter { it.genres.contains("Action") }) { anime ->
                        AnimePosterCard(
                            anime = anime,
                            isBookmarked = bookmarkedIds.contains(anime.id),
                            onBookmarkToggle = { onToggleBookmark(anime) },
                            onClick = { onSelectAnime(anime) }
                        )
                    }
                }
            }
        }
    }
}
