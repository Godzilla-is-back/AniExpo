package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.model.Anime
import com.example.data.model.Episode
import com.example.data.model.StreamQuality
import com.example.data.model.StreamServer
import com.example.ui.components.GlowingWatchButton
import com.example.ui.components.PressableScale
import com.example.ui.components.ServerLatencyBadge
import com.example.ui.theme.SakuraGlintAqua
import com.example.ui.theme.SakuraLightPink
import com.example.ui.theme.SakuraPinkGlow
import com.example.ui.theme.SakuraStarGold
import com.example.ui.theme.StreamGoodGreen
import com.example.ui.theme.TextFaint
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun DetailScreen(
    anime: Anime,
    episodes: List<Episode>,
    isBookmarked: Boolean,
    downloadedEpisodeIds: Set<String>,
    currentServer: StreamServer,
    onBack: () -> Unit,
    onWatchEpisode: (Episode) -> Unit,
    onToggleBookmark: () -> Unit,
    onSelectServer: (StreamServer) -> Unit,
    onDownloadEpisode: (Episode, StreamServer, StreamQuality) -> Unit
) {
    var isExpandedSynopsis by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("detail_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 1. Cinematic Backdrop Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                ) {
                    if (anime.bannerDrawableRes != null) {
                        Image(
                            painter = painterResource(id = anime.bannerDrawableRes),
                            contentDescription = anime.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = anime.bannerUrl.ifEmpty { anime.coverUrl },
                            contentDescription = anime.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Scrim gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x77000000),
                                        Color(0x99090611),
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                    )

                    // Back & Share buttons in header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 44.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0x77090611), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0x77090611), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) SakuraStarGold else Color.White
                            )
                        }
                    }
                }
            }

            // 2. Poster + Title + Rating Meta Sheet
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // High-res Poster Thumbnail
                        AsyncImage(
                            model = anime.coverUrl,
                            contentDescription = anime.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(115.dp)
                                .height(165.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.5.dp, SakuraLightPink.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Title and badges
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SakuraGlintAqua.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SakuraGlintAqua.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = SakuraStarGold,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${anime.score}% Rating",
                                        color = SakuraGlintAqua,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = anime.title,
                                color = TextWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                lineHeight = 24.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (anime.romajiTitle.isNotEmpty()) {
                                Text(
                                    text = anime.romajiTitle,
                                    color = SakuraLightPink,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Text(
                                text = "${anime.format} • ${anime.seasonYear} • ${anime.studio}",
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Action: Instead of View Trailer -> NEXT GEN GLOWING WATCH BUTTON!
                    val firstEp = episodes.firstOrNull()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlowingWatchButton(
                            modifier = Modifier.weight(1f),
                            label = "Watch Now (Ep 1)",
                            subtitle = "Streaming on ${currentServer.displayName.substringBefore(" ")}",
                            onClick = {
                                if (firstEp != null) onWatchEpisode(firstEp)
                            }
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Bookmark toggle
                        PressableScale(onClick = onToggleBookmark) {
                            Surface(
                                shape = RoundedCornerShape(22.dp),
                                color = if (isBookmarked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isBookmarked) SakuraLightPink else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = "Watchlist",
                                        tint = if (isBookmarked) SakuraLightPink else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBookmarked) "Saved" else "+ Watchlist",
                                        color = TextWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Genres pill row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(anime.genres) { genre ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Text(
                                    text = genre,
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Expandable Synopsis
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        Text(
                            text = "Synopsis",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = anime.description,
                            color = TextMuted,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            maxLines = if (isExpandedSynopsis) 100 else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isExpandedSynopsis) "Show Less" else "Read More",
                            color = SakuraLightPink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { isExpandedSynopsis = !isExpandedSynopsis }
                                .padding(top = 4.dp, bottom = 12.dp)
                        )
                    }

                    // Server Selection Bar
                    Text(
                        text = "Streaming Server (${currentServer.displayName})",
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
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

            // 3. Episodes List with Download Actions
            item {
                Column(modifier = Modifier.padding(top = 24.dp, start = 18.dp, end = 18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Episodes (${episodes.size})",
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Season 1 • 1080p",
                            color = SakuraLightPink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            items(episodes) { ep ->
                val isDownloaded = downloadedEpisodeIds.contains(ep.id)

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                        .clickable { onWatchEpisode(ep) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Episode Thumbnail with play badge
                        Box(
                            modifier = Modifier
                                .width(96.dp)
                                .height(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            AsyncImage(
                                model = ep.thumbnail,
                                contentDescription = ep.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0x55000000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Episode Meta
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Episode ${ep.episodeNum}",
                                color = SakuraLightPink,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = ep.title,
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${ep.durationMinutes} min • Subbed",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        // Download Button for offline mode
                        IconButton(
                            onClick = {
                                if (!isDownloaded) {
                                    onDownloadEpisode(ep, currentServer, StreamQuality.Q1080P)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                                contentDescription = if (isDownloaded) "Downloaded" else "Download Episode",
                                tint = if (isDownloaded) StreamGoodGreen else SakuraLightPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
