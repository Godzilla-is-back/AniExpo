package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.WatchlistEntity
import com.example.data.model.UserProfile
import com.example.ui.components.PressableScale
import com.example.ui.theme.SakuraGlintAqua
import com.example.ui.theme.SakuraLightPink
import com.example.ui.theme.SakuraPinkGlow
import com.example.ui.theme.SakuraStarGold
import com.example.ui.theme.StreamGoodGreen
import com.example.ui.theme.TextFaint
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun WatchlistScreen(
    watchlist: List<WatchlistEntity>,
    userProfile: UserProfile,
    onSelectAnimeId: (Int) -> Unit,
    onRemoveFromWatchlist: (Int) -> Unit,
    onOpenGoogleLogin: () -> Unit,
    onExploreAnime: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("All") }
    val tabs = listOf("All", "Watching", "Plan to Watch", "Completed")

    val filteredList = remember(watchlist, selectedTab) {
        if (selectedTab == "All") watchlist
        else watchlist.filter { it.status == selectedTab }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 48.dp)
            .testTag("watchlist_screen")
    ) {
        // 1. Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "My Watchlist",
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${watchlist.size} Titles Saved",
                    color = SakuraLightPink,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Google Sync Pill / Button
            PressableScale(onClick = onOpenGoogleLogin) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (userProfile.isGoogleLoggedIn) Color(0x2234D399) else Color(0x33FF80B5),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (userProfile.isGoogleLoggedIn) StreamGoodGreen.copy(alpha = 0.5f) else SakuraLightPink.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = if (userProfile.isGoogleLoggedIn) StreamGoodGreen else SakuraLightPink,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (userProfile.isGoogleLoggedIn) "Google Synced" else "Sign In",
                            color = if (userProfile.isGoogleLoggedIn) StreamGoodGreen else SakuraLightPink,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Filter Category Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEach { tabName ->
                val isSelected = tabName == selectedTab
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) SakuraLightPink else MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) SakuraLightPink else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.clickable { selectedTab = tabName }
                ) {
                    Text(
                        text = tabName,
                        color = if (isSelected) Color(0xFF1E0213) else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // 3. Grid of saved items or Empty State
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .border(1.dp, SakuraLightPink.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = SakuraLightPink,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your Watchlist is Empty",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Explore trending anime and tap '+ My List' to sync titles with your Google account.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                    )

                    Button(
                        onClick = onExploreAnime,
                        colors = ButtonDefaults.buttonColors(containerColor = SakuraLightPink),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "Explore Anime Catalog",
                            color = Color(0xFF1E0213),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.animeId }) { item ->
                    PressableScale(
                        onClick = { onSelectAnimeId(item.animeId) }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(2f / 3f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            ) {
                                AsyncImage(
                                    model = item.coverUrl,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Score badge top right
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .background(Color(0xCC090611), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${item.score}%",
                                        color = SakuraGlintAqua,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Remove button top left
                                IconButton(
                                    onClick = { onRemoveFromWatchlist(item.animeId) },
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(4.dp)
                                        .size(30.dp)
                                        .background(Color(0xCC090611), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remove",
                                        tint = Color(0xFFFB7185),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Quick Play button bottom right
                                Surface(
                                    shape = CircleShape,
                                    color = SakuraPinkGlow,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Stream",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = item.title,
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.genres,
                                color = TextMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
