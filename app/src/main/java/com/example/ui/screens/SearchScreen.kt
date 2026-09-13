package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Anime
import com.example.ui.components.AnimePosterCard
import com.example.ui.theme.SakuraLightPink
import com.example.ui.theme.TextFaint
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    animeList: List<Anime>,
    bookmarkedIds: Set<Int>,
    onSelectAnime: (Anime) -> Unit,
    onToggleBookmark: (Anime) -> Unit,
    onSearchOnline: (suspend (String) -> List<Anime>)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("All") }
    var isSearchingOnline by remember { mutableStateOf(false) }
    var onlineResults by remember { mutableStateOf<List<Anime>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    val genres = listOf("All", "Action", "Fantasy", "Supernatural", "Adventure", "Sci-Fi", "Drama", "Comedy", "Romance")
    val popularSuggestions = listOf(
        "Rich Girl Caretaker",
        "Solo Leveling",
        "Demon Slayer",
        "Jujutsu Kaisen",
        "Naruto",
        "Bleach",
        "One Piece",
        "Attack on Titan",
        "Frieren"
    )

    // Trigger online search whenever searchQuery changes
    LaunchedEffect(searchQuery) {
        val query = searchQuery.trim()
        if (query.length >= 2 && onSearchOnline != null) {
            searchJob?.cancel()
            searchJob = coroutineScope.launch {
                delay(350) // debounce
                isSearchingOnline = true
                try {
                    val res = onSearchOnline(query)
                    onlineResults = res
                } catch (_: Exception) {
                } finally {
                    isSearchingOnline = false
                }
            }
        } else if (query.isEmpty()) {
            onlineResults = emptyList()
            isSearchingOnline = false
        }
    }

    // Merge in-memory anime with live online search results
    val combinedPool = remember(animeList, onlineResults) {
        (animeList + onlineResults).distinctBy { it.id }
    }

    val filteredResults = remember(combinedPool, searchQuery, selectedGenre) {
        val trimmed = searchQuery.trim()
        combinedPool.filter { anime ->
            val matchesQuery = trimmed.isBlank() ||
                anime.title.contains(trimmed, ignoreCase = true) ||
                anime.romajiTitle.contains(trimmed, ignoreCase = true) ||
                anime.genres.any { it.contains(trimmed, ignoreCase = true) }
            val matchesGenre = selectedGenre == "All" || anime.genres.contains(selectedGenre)
            matchesQuery && matchesGenre
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 48.dp)
            .testTag("search_screen")
    ) {
        // Search Header
        Text(
            text = "Discover Anime",
            color = TextWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
        )

        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search titles, genres, studios...", color = TextFaint) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = SakuraLightPink
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = TextMuted
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SakuraLightPink,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
        )

        // Genre Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(genres) { genre ->
                val isSelected = genre == selectedGenre
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) SakuraLightPink else MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) SakuraLightPink else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.clickable { selectedGenre = genre }
                ) {
                    Text(
                        text = genre,
                        color = if (isSelected) Color(0xFF1E0213) else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredResults.size} Titles Found",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (isSearchingOnline) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = SakuraLightPink,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Searching AniList live...",
                        color = SakuraLightPink,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Quick Suggestions bar
        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(popularSuggestions) { sug ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x331F1735),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFB2D9)),
                    modifier = Modifier.clickable { searchQuery = sug }
                ) {
                    Text(
                        text = "🔍 $sug",
                        color = TextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Results Grid
        if (filteredResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = SakuraLightPink,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (isSearchingOnline) "Searching AniList & AniDB..." else "No Anime Matches Found",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Try tapping any popular title above or search by English or Japanese name",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        popularSuggestions.take(3).forEach { sug ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SakuraLightPink.copy(alpha = 0.5f)),
                                modifier = Modifier.clickable { searchQuery = sug }
                            ) {
                                Text(
                                    text = sug,
                                    color = SakuraLightPink,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
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
                items(filteredResults, key = { it.id }) { anime ->
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
