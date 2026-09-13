package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.Anime
import com.example.data.model.StreamServer
import com.example.ui.theme.SakuraGlintAqua
import com.example.ui.theme.SakuraLightPink
import com.example.ui.theme.SakuraPinkGlow
import com.example.ui.theme.SakuraStarGold
import com.example.ui.theme.StreamGoodGreen
import com.example.ui.theme.StreamWarningOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

/**
 * Buttery smooth spring-scale pressable wrapper
 */
@Composable
fun PressableScale(
    modifier: Modifier = Modifier,
    scaleDownTo: Float = 0.94f,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDownTo else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "press_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        content()
    }
}

/**
 * Next-Gen Glowing Watch Button (Replaces Trailer Button)
 */
@Composable
fun GlowingWatchButton(
    modifier: Modifier = Modifier,
    label: String = "Watch Now",
    subtitle: String? = null,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "watch_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val gradient = Brush.horizontalGradient(
        colors = listOf(
            SakuraLightPink,
            SakuraPinkGlow,
            MaterialTheme.colorScheme.secondary
        )
    )

    PressableScale(
        modifier = modifier
            .testTag("watch_button")
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = SakuraPinkGlow.copy(alpha = glowAlpha),
                spotColor = SakuraLightPink.copy(alpha = glowAlpha)
            ),
        scaleDownTo = 0.95f,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .background(gradient, shape = RoundedCornerShape(26.dp))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 24.dp, vertical = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.22f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Watch",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.3.sp
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modern Anime Poster Card
 */
@Composable
fun AnimePosterCard(
    anime: Anime,
    modifier: Modifier = Modifier,
    isBookmarked: Boolean = false,
    onBookmarkToggle: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    PressableScale(
        modifier = modifier.width(140.dp),
        onClick = onClick
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(18.dp)
                    )
            ) {
                AsyncImage(
                    model = anime.coverUrl,
                    contentDescription = anime.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top right score pill
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            Color(0xCC090611),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .border(
                            width = 0.8.dp,
                            color = SakuraGlintAqua.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = SakuraStarGold,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${anime.score}%",
                            color = SakuraGlintAqua,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Top left bookmark action
                if (onBookmarkToggle != null) {
                    IconButton(
                        onClick = onBookmarkToggle,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .size(32.dp)
                            .background(
                                Color(0xCC090611),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save to Watchlist",
                            tint = if (isBookmarked) SakuraStarGold else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Bottom gradient scrim
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(35.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xDD090611))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = anime.title,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = anime.genres.take(2).joinToString(" • "),
                color = TextMuted,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Continue Watching Card with progress track
 */
@Composable
fun ContinueWatchingCard(
    history: WatchHistoryEntity,
    onClick: () -> Unit
) {
    val progress = if (history.durationMs > 0) {
        (history.positionMs.toFloat() / history.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0.35f

    PressableScale(
        modifier = Modifier.width(180.dp),
        onClick = onClick
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(105.dp)
                ) {
                    AsyncImage(
                        model = history.coverUrl,
                        contentDescription = history.animeTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Play icon bubble
                    Surface(
                        shape = CircleShape,
                        color = SakuraPinkGlow.copy(alpha = 0.88f),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Server tag
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xCC090611),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = history.serverName,
                            color = SakuraLightPink,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Progress indicator
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = SakuraLightPink,
                    trackColor = Color(0xFF221538),
                )

                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = history.animeTitle,
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Ep ${history.episodeNum} • ${history.episodeTitle}",
                        color = TextMuted,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Server Latency Badge with pulsing ping indicator
 */
@Composable
fun ServerLatencyBadge(
    server: StreamServer,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val pingColor = when {
        server.pingMs < 35 -> StreamGoodGreen
        server.pingMs < 60 -> StreamWarningOrange
        else -> MaterialTheme.colorScheme.primary
    }

    PressableScale(onClick = onClick) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
            border = androidx.compose.foundation.BorderStroke(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(pingColor, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = server.displayName,
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${server.pingMs}ms",
                            color = pingColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = server.tag,
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Section Header with pink bar
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = "See all",
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(18.dp)
                    .background(
                        Brush.verticalGradient(listOf(SakuraLightPink, SakuraPinkGlow)),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.2.sp
            )
        }

        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                color = SakuraLightPink,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

/**
 * Header status pill for Offline / Online
 */
@Composable
fun OfflineStatusPill(
    isOffline: Boolean,
    onClick: () -> Unit
) {
    PressableScale(onClick = onClick) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isOffline) Color(0x33F43F5E) else Color(0x2234D399),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isOffline) Color(0x66F43F5E) else Color(0x4434D399)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (isOffline) Icons.Default.CloudOff else Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = if (isOffline) Color(0xFFFB7185) else StreamGoodGreen,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (isOffline) "Offline Mode" else "Online",
                    color = if (isOffline) Color(0xFFFB7185) else StreamGoodGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
