package com.example.data.remote

import android.util.Log
import com.example.data.model.Anime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AnimeApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val anilistEndpoint = "https://graphql.anilist.co"

    /**
     * Fetches currently releasing (latest releases) anime from AniList GraphQL API.
     */
    suspend fun fetchLatestReleases(page: Int = 1, perPage: Int = 15): Result<List<Anime>> = withContext(Dispatchers.IO) {
        val query = """
            query {
              Page(page: $page, perPage: $perPage) {
                media(type: ANIME, status: RELEASING, sort: [POPULARITY_DESC, TRENDING_DESC]) {
                  id
                  title {
                    english
                    romaji
                  }
                  coverImage {
                    extraLarge
                    large
                  }
                  bannerImage
                  episodes
                  genres
                  averageScore
                  status
                  description
                  seasonYear
                  format
                  studios(isMain: true) {
                    nodes {
                      name
                    }
                  }
                }
              }
            }
        """.trimIndent()
        postAniListQuery(query)
    }

    /**
     * Fetches trending anime from AniList GraphQL API.
     */
    suspend fun fetchTrendingAnime(page: Int = 1, perPage: Int = 15): Result<List<Anime>> = withContext(Dispatchers.IO) {
        val query = """
            query {
              Page(page: $page, perPage: $perPage) {
                media(type: ANIME, sort: TRENDING_DESC) {
                  id
                  title {
                    english
                    romaji
                  }
                  coverImage {
                    extraLarge
                    large
                  }
                  bannerImage
                  episodes
                  genres
                  averageScore
                  status
                  description
                  seasonYear
                  format
                  studios(isMain: true) {
                    nodes {
                      name
                    }
                  }
                }
              }
            }
        """.trimIndent()
        postAniListQuery(query)
    }

    /**
     * Fetches all-time top rated anime from AniList / MyAnimeList catalog.
     */
    suspend fun fetchTopRatedAnime(page: Int = 1, perPage: Int = 15): Result<List<Anime>> = withContext(Dispatchers.IO) {
        val query = """
            query {
              Page(page: $page, perPage: $perPage) {
                media(type: ANIME, sort: SCORE_DESC) {
                  id
                  title {
                    english
                    romaji
                  }
                  coverImage {
                    extraLarge
                    large
                  }
                  bannerImage
                  episodes
                  genres
                  averageScore
                  status
                  description
                  seasonYear
                  format
                  studios(isMain: true) {
                    nodes {
                      name
                    }
                  }
                }
              }
            }
        """.trimIndent()
        postAniListQuery(query)
    }

    /**
     * Search anime on AniList by title query.
     */
    suspend fun searchAnime(searchQuery: String): Result<List<Anime>> = withContext(Dispatchers.IO) {
        val sanitized = searchQuery.replace("\"", "\\\"")
        val query = """
            query {
              Page(page: 1, perPage: 15) {
                media(type: ANIME, search: "$sanitized", sort: POPULARITY_DESC) {
                  id
                  title {
                    english
                    romaji
                  }
                  coverImage {
                    extraLarge
                    large
                  }
                  bannerImage
                  episodes
                  genres
                  averageScore
                  status
                  description
                  seasonYear
                  format
                  studios(isMain: true) {
                    nodes {
                      name
                    }
                  }
                }
              }
            }
        """.trimIndent()
        postAniListQuery(query)
    }

    /**
     * Fallback or supplementary fetch from MyAnimeList (via public Jikan v4 API).
     */
    suspend fun fetchMyAnimeListTop(): Result<List<Anime>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.jikan.moe/v4/top/anime?limit=10")
                .header("User-Agent", "SakuraStream/2.0 (Android)")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("MyAnimeList HTTP ${response.code}"))
                }
                val bodyStr = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))
                val json = JSONObject(bodyStr)
                val dataArray = json.optJSONArray("data") ?: JSONArray()
                val list = mutableListOf<Anime>()
                for (i in 0 until dataArray.length()) {
                    val item = dataArray.getJSONObject(i)
                    val id = item.optInt("mal_id")
                    val title = item.optString("title_english").takeIf { it.isNotBlank() }
                        ?: item.optString("title")
                    val romaji = item.optString("title_japanese", "")
                    val imagesObj = item.optJSONObject("images")?.optJSONObject("jpg")
                    val cover = imagesObj?.optString("large_image_url")
                        ?: imagesObj?.optString("image_url") ?: ""
                    val scoreDouble = item.optDouble("score", 8.5)
                    val score = (scoreDouble * 10).toInt().coerceIn(60, 99)
                    val synopsis = item.optString("synopsis", "")
                        .replace(Regex("\\[Written by MAL Rewrite\\]"), "").trim()
                    val episodes = item.optInt("episodes", 12).coerceAtLeast(1)
                    val year = item.optInt("year", 2025)
                    val statusStr = when (item.optString("status")) {
                        "Currently Airing" -> "Airing"
                        "Finished Airing" -> "Completed"
                        else -> "Airing"
                    }
                    val genresList = mutableListOf<String>()
                    val genresArr = item.optJSONArray("genres")
                    if (genresArr != null) {
                        for (g in 0 until genresArr.length()) {
                            genresList.add(genresArr.getJSONObject(g).optString("name"))
                        }
                    }
                    if (genresList.isEmpty()) genresList.addAll(listOf("Action", "Anime"))

                    list.add(
                        Anime(
                            id = id,
                            title = title,
                            romajiTitle = romaji,
                            coverUrl = cover,
                            bannerUrl = cover,
                            description = synopsis,
                            score = score,
                            genres = genresList,
                            episodesCount = episodes,
                            seasonYear = year,
                            status = statusStr,
                            studio = "MAL Verified Studio"
                        )
                    )
                }
                Result.success(list)
            }
        } catch (e: Exception) {
            Log.w("AnimeApiService", "MyAnimeList Jikan API error: ${e.message}")
            Result.failure(e)
        }
    }

    private fun postAniListQuery(graphqlQuery: String): Result<List<Anime>> {
        return try {
            val payload = JSONObject().apply {
                put("query", graphqlQuery)
            }.toString()

            val request = Request.Builder()
                .url(anilistEndpoint)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "SakuraStream/2.0 (Android; AniList Integration)")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val err = "AniList API HTTP ${response.code}: ${response.message}"
                    Log.e("AnimeApiService", err)
                    return Result.failure(Exception(err))
                }
                val bodyString = response.body?.string() ?: return Result.failure(Exception("Empty body from AniList"))
                val root = JSONObject(bodyString)
                val data = root.optJSONObject("data")
                val page = data?.optJSONObject("Page")
                val mediaArray = page?.optJSONArray("media") ?: JSONArray()

                val resultList = mutableListOf<Anime>()
                for (i in 0 until mediaArray.length()) {
                    val m = mediaArray.getJSONObject(i)
                    val id = m.optInt("id")
                    val titleObj = m.optJSONObject("title")
                    val englishTitle = titleObj?.optString("english")?.takeIf { it.isNotBlank() }
                    val romajiTitle = titleObj?.optString("romaji")?.takeIf { it.isNotBlank() } ?: ""
                    val title = englishTitle ?: romajiTitle.ifBlank { "Anime $id" }

                    val coverObj = m.optJSONObject("coverImage")
                    val cover = coverObj?.optString("extraLarge")?.takeIf { it.isNotBlank() }
                        ?: coverObj?.optString("large") ?: ""

                    val banner = m.optString("bannerImage", "").ifBlank { cover }
                    val rawDesc = m.optString("description", "")
                    val cleanDesc = rawDesc
                        .replace(Regex("<br\\s*/?>"), "\n")
                        .replace(Regex("<[^>]*>"), "")
                        .replace("&quot;", "\"")
                        .replace("&amp;", "&")
                        .replace("&#039;", "'")
                        .trim()

                    val score = m.optInt("averageScore", 85).coerceIn(50, 100)
                    val eps = m.optInt("episodes", 12).coerceAtLeast(1)
                    val year = m.optInt("seasonYear", 2026)
                    val format = m.optString("format", "TV Series")
                    val statusRaw = m.optString("status", "RELEASING")
                    val status = when (statusRaw) {
                        "RELEASING" -> "Airing"
                        "FINISHED" -> "Completed"
                        "NOT_YET_RELEASED" -> "Upcoming"
                        else -> "Airing"
                    }

                    val studio = m.optJSONObject("studios")
                        ?.optJSONArray("nodes")
                        ?.optJSONObject(0)
                        ?.optString("name")
                        ?: "Studio Animation"

                    val genresList = mutableListOf<String>()
                    val genresArr = m.optJSONArray("genres")
                    if (genresArr != null) {
                        for (g in 0 until genresArr.length()) {
                            genresList.add(genresArr.getString(g))
                        }
                    }
                    if (genresList.isEmpty()) {
                        genresList.addAll(listOf("Action", "Animation"))
                    }

                    resultList.add(
                        Anime(
                            id = id,
                            title = title,
                            romajiTitle = romajiTitle,
                            coverUrl = cover,
                            bannerUrl = banner,
                            description = cleanDesc.ifBlank { "Official synopsis coming soon from AniList." },
                            score = score,
                            genres = genresList,
                            episodesCount = eps,
                            seasonYear = year,
                            format = format,
                            status = status,
                            studio = studio
                        )
                    )
                }
                Result.success(resultList)
            }
        } catch (e: Exception) {
            Log.e("AnimeApiService", "Network error calling AniList", e)
            Result.failure(e)
        }
    }
}
