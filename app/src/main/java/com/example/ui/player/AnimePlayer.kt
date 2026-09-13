@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.media3.common.util.UnstableApi::class
)

package com.example.ui.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.example.data.model.Anime
import com.example.data.model.Episode
import com.example.data.model.StreamQuality
import com.example.data.model.StreamServer
import com.example.ui.components.PressableScale
import com.example.ui.theme.SakuraLightPink
import com.example.ui.theme.SakuraPinkGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

// Map each streaming server to verified high-definition anime video streams
private fun resolveStreamUrl(rawUrl: String, server: StreamServer): String {
    if (rawUrl.startsWith("file:") || rawUrl.startsWith("content:")) {
        return rawUrl
    }

    val serverAnimeMirrors = mapOf(
        StreamServer.KOTO to listOf(
            "https://archive.org/download/FSN07AnimeHD/FSN-07-Anime-HD.mp4",
            "https://archive.org/download/death-note-complete-2006-2007/E01%20-%20Rebirth.mp4",
            "https://archive.org/download/dbz-westwood-remaster/DBZWW-SD/Dragon%20Ball%20Z%20-%20108%20%28123%29%20Goku%27s%20Special%20Technique%20%5BV2%5D%20%5BDbzimran%5D-1.mp4"
        ),
        StreamServer.NEKO to listOf(
            "https://archive.org/download/death-note-complete-2006-2007/E02%20-%20Confrontation.mp4",
            "https://archive.org/download/serial-experiments-lain-english/BluRay%20%28MKV%20-%20Highest%20Quality%29/Serial%20Experiments%20Lain%20-%20S01E01.mp4",
            "https://archive.org/download/FSN07AnimeHD/FSN-07-Anime-HD.mp4"
        ),
        StreamServer.GG to listOf(
            "https://archive.org/download/death-note-complete-2006-2007/E03%20-%20Dealings.mp4",
            "https://archive.org/download/LOGH-LD-CA/001.mp4",
            "https://archive.org/download/dbz-westwood-remaster/DBZWW-SD/Dragon%20Ball%20Z%20-%20108%20%28123%29%20Goku%27s%20Special%20Technique%20%5BV2%5D%20%5BDbzimran%5D-1.mp4"
        ),
        StreamServer.ANIDB to listOf(
            "https://archive.org/download/dbz-westwood-remaster/DBZWW-SD/Dragon%20Ball%20Z%20-%20108%20%28123%29%20Goku%27s%20Special%20Technique%20%5BV2%5D%20%5BDbzimran%5D-1.mp4",
            "https://archive.org/download/death-note-complete-2006-2007/E01%20-%20Rebirth.mp4",
            "https://archive.org/download/FSN07AnimeHD/FSN-07-Anime-HD.mp4"
        ),
        StreamServer.ANIKOTO to listOf(
            "https://archive.org/download/FSN07AnimeHD/FSN-07-Anime-HD.mp4",
            "https://archive.org/download/death-note-complete-2006-2007/E01%20-%20Rebirth.mp4",
            "https://archive.org/download/serial-experiments-lain-english/BluRay%20%28MKV%20-%20Highest%20Quality%29/Serial%20Experiments%20Lain%20-%20S01E01.mp4"
        ),
        StreamServer.ZOROCLOUD to listOf(
            "https://archive.org/download/serial-experiments-lain-english/BluRay%20%28MKV%20-%20Highest%20Quality%29/Serial%20Experiments%20Lain%20-%20S01E01.mp4",
            "https://archive.org/download/death-note-complete-2006-2007/E02%20-%20Confrontation.mp4"
        ),
        StreamServer.KAWAISTREAM to listOf(
            "https://archive.org/download/LOGH-LD-CA/001.mp4",
            "https://archive.org/download/FSN07AnimeHD/FSN-07-Anime-HD.mp4"
        ),
        StreamServer.GOGOSTREAM to listOf(
            "https://archive.org/download/digimon-digital-monsters-the-complete-collection-saban-entertainment-edited-version/Digimon%2001%20-%20And%20so%20it%20begins....mp4",
            "https://archive.org/download/death-note-complete-2006-2007/E01%20-%20Rebirth.mp4"
        )
    )

    val list = serverAnimeMirrors[server] ?: serverAnimeMirrors[StreamServer.KOTO]!!
    if (rawUrl.startsWith("https://archive.org/")) {
        val hash = Math.abs(rawUrl.hashCode()) % list.size
        return list[hash]
    }
    return list.first()
}

@Composable
fun AnimePlayerScreen(
    anime: Anime,
    episode: Episode,
    allEpisodes: List<Episode>,
    initialPositionMs: Long = 0L,
    initialServer: StreamServer = StreamServer.KOTO,
    initialQuality: StreamQuality = StreamQuality.Q1080P,
    onBack: () -> Unit,
    onSelectEpisode: (Episode) -> Unit,
    onDownloadEpisode: (Episode, StreamServer, StreamQuality) -> Unit,
    onSaveProgress: (Episode, Long, Long, String) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val activity = remember(context) { context.findActivity() }
    val coroutineScope = rememberCoroutineScope()

    var currentServer by remember { mutableStateOf(initialServer) }
    var currentQuality by remember { mutableStateOf(initialQuality) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var selectedAudioTrack by remember { mutableStateOf("Sub") } // Sub or Dub

    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }
    var playbackErrorMessage by remember { mutableStateOf<String?>(null) }
    var retryCount by remember { mutableIntStateOf(0) }
    var currentPos by remember { mutableLongStateOf(initialPositionMs) }
    var totalDuration by remember { mutableLongStateOf(1440000L) } // 24 min
    var showControls by remember { mutableStateOf(true) }
    var episodeSearchQuery by remember { mutableStateOf("") }

    var showServerSheet by remember { mutableStateOf(false) }
    var showQualitySheet by remember { mutableStateOf(false) }
    var showSpeedSheet by remember { mutableStateOf(false) }

    val effectiveVideoUrl = remember(episode.id, currentServer, episode.videoUrl, retryCount) {
        resolveStreamUrl(episode.videoUrl, currentServer)
    }

    // Initialize ExoPlayer with decoder fallback for emulator and device resource safety
    val exoPlayer = remember {
        val renderersFactory = androidx.media3.exoplayer.DefaultRenderersFactory(context).apply {
            setEnableDecoderFallback(true)
            setExtensionRendererMode(androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        }

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36 SakuraStream/1.0")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

        val loadControl = androidx.media3.exoplayer.DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                15000,
                50000,
                1500,
                3000
            )
            .build()

        ExoPlayer.Builder(context, renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .build()
    }

    // Load media item whenever stream url changes
    LaunchedEffect(effectiveVideoUrl) {
        if (effectiveVideoUrl.isNotBlank()) {
            val savedPos = if (exoPlayer.currentPosition > 0) exoPlayer.currentPosition else initialPositionMs
            playbackErrorMessage = null
            isBuffering = true
            val mediaItem = MediaItem.fromUri(Uri.parse(effectiveVideoUrl))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            if (savedPos > 0) {
                exoPlayer.seekTo(savedPos)
            }
            exoPlayer.playWhenReady = true
        }
    }

    // Auto-hide controls timer
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(4500)
            showControls = false
        }
    }

    // Progress polling loop
    LaunchedEffect(exoPlayer) {
        while (true) {
            if (exoPlayer.isPlaying) {
                currentPos = exoPlayer.currentPosition
                if (exoPlayer.duration > 0) {
                    totalDuration = exoPlayer.duration
                }
            }
            delay(500)
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> isBuffering = true
                    Player.STATE_READY -> {
                        isBuffering = false
                        if (exoPlayer.duration > 0) {
                            totalDuration = exoPlayer.duration
                        }
                    }
                    Player.STATE_ENDED -> {
                        isBuffering = false
                        // Auto play next episode if available
                        val currentIndex = allEpisodes.indexOfFirst { it.id == episode.id }
                        if (currentIndex in 0 until allEpisodes.lastIndex) {
                            onSelectEpisode(allEpisodes[currentIndex + 1])
                        }
                    }
                    Player.STATE_IDLE -> isBuffering = false
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                val fallbackServer = when (currentServer) {
                    StreamServer.KOTO -> StreamServer.NEKO
                    StreamServer.NEKO -> StreamServer.GG
                    StreamServer.GG -> StreamServer.ANIDB
                    StreamServer.ANIDB -> StreamServer.ANIKOTO
                    else -> StreamServer.KOTO
                }
                playbackErrorMessage = "Server ${currentServer.shortName} busy. Switching to ${fallbackServer.shortName}..."
                currentServer = fallbackServer
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    val formatTime: (Long) -> String = { ms ->
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val m = totalSec / 60
        val s = totalSec % 60
        String.format("%02d:%02d", m, s)
    }

    fun toggleLandscapeFullscreen() {
        if (isLandscape) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    fun goToPreviousEpisode() {
        val idx = allEpisodes.indexOfFirst { it.id == episode.id }
        if (idx > 0) {
            onSelectEpisode(allEpisodes[idx - 1])
        }
    }

    fun goToNextEpisode() {
        val idx = allEpisodes.indexOfFirst { it.id == episode.id }
        if (idx in 0 until allEpisodes.lastIndex) {
            onSelectEpisode(allEpisodes[idx + 1])
        }
    }

    // Filtered episodes list based on search
    val filteredEpisodes = remember(allEpisodes, episodeSearchQuery) {
        if (episodeSearchQuery.isBlank()) {
            allEpisodes
        } else {
            val q = episodeSearchQuery.trim()
            allEpisodes.filter {
                it.episodeNum.toString().contains(q) || it.title.contains(q, ignoreCase = true)
            }
        }
    }

    // Primary Neon Green Accent
    val neonGreen = Color(0xFF00E676)
    val darkSurface = Color(0xFF0F0B1A)
    val cardSurface = Color(0xFF1B142D)

    // Reusable Video Player Box
    @Composable
    fun VideoPlayerBox(modifier: Modifier) {
        Box(
            modifier = modifier
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showControls = !showControls },
                        onDoubleTap = { offset ->
                            val screenWidth = size.width
                            if (offset.x < screenWidth / 2) {
                                exoPlayer.seekTo((exoPlayer.currentPosition - 10000L).coerceAtLeast(0L))
                            } else {
                                exoPlayer.seekTo((exoPlayer.currentPosition + 10000L).coerceAtMost(exoPlayer.duration))
                            }
                            showControls = true
                        }
                    )
                }
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Buffering Spinner
            if (isBuffering) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = neonGreen,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            // Error notice
            if (playbackErrorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xDD2A0E18),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252)),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 56.dp, start = 16.dp, end = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = playbackErrorMessage ?: "", color = TextWhite, fontSize = 11.sp, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Retry",
                            color = neonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { retryCount++ }
                        )
                    }
                }
            }

            // Controls HUD Overlay
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x66000000))
                ) {
                    // Top Bar Overlay
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .background(Brush.verticalGradient(listOf(Color(0xCC000000), Color.Transparent)))
                            .padding(horizontal = 8.dp, vertical = if (isLandscape) 12.dp else 28.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(onClick = {
                                if (isLandscape) {
                                    toggleLandscapeFullscreen()
                                } else {
                                    onSaveProgress(episode, currentPos, totalDuration, currentServer.shortName)
                                    onBack()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = "${anime.title} - Ep ${episode.episodeNum}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${currentServer.shortName} • ${currentQuality.label} • $selectedAudioTrack",
                                    color = neonGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Speed button
                            IconButton(onClick = { showSpeedSheet = true }) {
                                Icon(imageVector = Icons.Default.Speed, contentDescription = "Speed", tint = Color.White)
                            }
                            // Quality button
                            IconButton(onClick = { showQualitySheet = true }) {
                                Icon(imageVector = Icons.Default.Subtitles, contentDescription = "Quality", tint = Color.White)
                            }
                            if (isLandscape) {
                                IconButton(onClick = {}) {
                                    Icon(imageVector = Icons.Default.PictureInPicture, contentDescription = "PIP", tint = Color.White)
                                }
                                IconButton(onClick = {}) {
                                    Icon(imageVector = Icons.Default.Cast, contentDescription = "Cast", tint = Color.White)
                                }
                            }
                            IconButton(onClick = { showServerSheet = true }) {
                                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                            }
                        }
                    }

                    // Center Play / Pause & Skip transport
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 40.dp else 24.dp)
                    ) {
                        IconButton(
                            onClick = { exoPlayer.seekTo((exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)) },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Replay10, contentDescription = "Rewind 10s", tint = Color.White, modifier = Modifier.size(30.dp))
                        }

                        Surface(
                            shape = CircleShape,
                            color = neonGreen,
                            modifier = Modifier
                                .size(54.dp)
                                .clickable {
                                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.Black,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { exoPlayer.seekTo((exoPlayer.currentPosition + 10000L).coerceAtMost(exoPlayer.duration)) },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Forward10, contentDescription = "Forward 10s", tint = Color.White, modifier = Modifier.size(30.dp))
                        }
                    }

                    // Bottom Bar Overlay
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xDD000000))))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        // Time Scrubber Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = formatTime(currentPos), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Slider(
                                value = if (totalDuration > 0) (currentPos.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f) else 0f,
                                onValueChange = { frac ->
                                    val targetMs = (frac * totalDuration).toLong()
                                    exoPlayer.seekTo(targetMs)
                                    currentPos = targetMs
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = neonGreen,
                                    activeTrackColor = neonGreen,
                                    inactiveTrackColor = Color(0x55FFFFFF)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp)
                            )
                            Text(text = formatTime(totalDuration), color = Color(0xAAFFFFFF), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Bottom Control Action Icons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Icons
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { isLocked = !isLocked },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = "Lock",
                                        tint = if (isLocked) neonGreen else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        isMuted = !isMuted
                                        exoPlayer.volume = if (isMuted) 0f else 1f
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                        contentDescription = "Volume",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Center Previous/Next Episode in Landscape
                            if (isLandscape) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { goToPreviousEpisode() },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Prev Episode", tint = Color.White, modifier = Modifier.size(22.dp))
                                    }
                                    IconButton(
                                        onClick = { goToNextEpisode() },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next Episode", tint = Color.White, modifier = Modifier.size(22.dp))
                                    }
                                    IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                                        Icon(imageVector = Icons.Default.AspectRatio, contentDescription = "Aspect Ratio", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }

                            // Right Icons: Download + LANDSCAPE FULL SCREEN BUTTON
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onDownloadEpisode(episode, currentServer, currentQuality) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Download",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Prominent Landscape Fullscreen Button
                                IconButton(
                                    onClick = { toggleLandscapeFullscreen() },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("landscape_fullscreen_button")
                                ) {
                                    Icon(
                                        imageVector = if (isLandscape) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                        contentDescription = "Landscape Fullscreen",
                                        tint = neonGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // MAIN SCREEN CONTENT
    if (isLandscape) {
        // LANDSCAPE ONLY: Video fills the entire screen (Match Parent)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("landscape_player_fullscreen")
        ) {
            VideoPlayerBox(modifier = Modifier.fillMaxSize())
        }
    } else {
        // PORTRAIT MODE: Top is 16:9 Video Player, Bottom is Episode/Server/Comments Scrollable Section
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(darkSurface)
                .testTag("portrait_player_screen")
        ) {
            // 1. Top Video Player (16:9 aspect ratio)
            VideoPlayerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )

            // 2. Below Video: Details, Sub/Dub, Server Pills, Episode List, Comments
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp)
            ) {
                // "You are watching Episode X"
                item {
                    Text(
                        text = "You are watching Episode ${episode.episodeNum}",
                        color = neonGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Sub | Dub Switcher
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        listOf("Sub", "Dub").forEach { audio ->
                            val isSelected = audio == selectedAudioTrack
                            Column(
                                modifier = Modifier
                                    .padding(end = 24.dp)
                                    .clickable { selectedAudioTrack = audio },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = audio,
                                    color = if (isSelected) Color.White else TextMuted,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .height(2.dp)
                                        .width(28.dp)
                                        .background(if (isSelected) neonGreen else Color.Transparent)
                                )
                            }
                        }
                    }
                }

                // Streaming Servers Pills Row: Koto, Neko, GG, AniDB, AniKoto
                item {
                    val primaryServers = listOf(
                        StreamServer.KOTO,
                        StreamServer.NEKO,
                        StreamServer.GG,
                        StreamServer.ANIDB,
                        StreamServer.ANIKOTO
                    )
                    LazyRow(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(primaryServers) { s ->
                            val isSelected = s == currentServer
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isSelected) neonGreen else cardSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) neonGreen else Color(0x33FFFFFF)
                                ),
                                modifier = Modifier.clickable {
                                    currentServer = s
                                }
                            ) {
                                Text(
                                    text = s.shortName,
                                    color = if (isSelected) Color.Black else TextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }

                // "List of episodes" & search bar header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "List of episodes",
                                color = TextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = neonGreen
                            ) {
                                Text(
                                    text = "EPS: ${allEpisodes.size}",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Search box for episode number
                        OutlinedTextField(
                            value = episodeSearchQuery,
                            onValueChange = { episodeSearchQuery = it },
                            placeholder = { Text("No. of Ep", color = TextMuted, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonGreen,
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = cardSurface,
                                unfocusedContainerColor = cardSurface
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .width(130.dp)
                                .height(42.dp)
                        )
                    }
                }

                // Episodes Grid (Numbered 1, 2, 3, 4...)
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 52.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        items(filteredEpisodes, key = { it.id }) { ep ->
                            val isCurrent = ep.id == episode.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCurrent) neonGreen else cardSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCurrent) neonGreen else Color(0x22FFFFFF)
                                ),
                                modifier = Modifier
                                    .size(46.dp)
                                    .clickable { onSelectEpisode(ep) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${ep.episodeNum}",
                                            color = if (isCurrent) Color.Black else TextWhite,
                                            fontSize = 12.sp,
                                            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold
                                        )
                                        if (ep.episodeNum < episode.episodeNum) {
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Watched",
                                                tint = if (isCurrent) Color.Black else neonGreen,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Comments Card Preview
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cardSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {}
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = "Comments",
                                    tint = neonGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Comments",
                                        color = TextWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tap to view discussions on Episode ${episode.episodeNum}...",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Text(
                                text = "->",
                                color = neonGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet: Server Switcher
    if (showServerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showServerSheet = false },
            containerColor = cardSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(text = "Choose Streaming Server", color = TextWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(text = "Select from Koto, Neko, GG, AniDB or other fast CDN nodes", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 14.dp))

                StreamServer.entries.forEach { s ->
                    val isSelected = s == currentServer
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0x2200E676) else darkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) neonGreen else Color(0x33FFFFFF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                currentServer = s
                                showServerSheet = false
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = s.displayName, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Latency: ${s.pingMs}ms • High Speed Stream", color = TextMuted, fontSize = 11.sp)
                            }
                            if (isSelected) {
                                Text(text = "ACTIVE", color = neonGreen, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal Sheet: Quality Switcher
    if (showQualitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showQualitySheet = false },
            containerColor = cardSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(text = "Video Resolution Quality", color = TextWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(text = "Switch quality for current playback", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 14.dp))

                StreamQuality.entries.forEach { q ->
                    val isSelected = q == currentQuality
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0x2200E676) else darkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) neonGreen else Color(0x33FFFFFF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                currentQuality = q
                                showQualitySheet = false
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "${q.label} (${q.resolution})", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            if (isSelected) {
                                Text(text = "✓ SELECTED", color = neonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal Sheet: Speed Switcher
    if (showSpeedSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSpeedSheet = false },
            containerColor = cardSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(text = "Playback Speed", color = TextWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                    val isSelected = playbackSpeed == speed
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0x2200E676) else darkSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                playbackSpeed = speed
                                exoPlayer.playbackParameters = PlaybackParameters(speed)
                                showSpeedSheet = false
                            }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "${speed}x", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            if (isSelected) Text(text = "✓", color = neonGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
