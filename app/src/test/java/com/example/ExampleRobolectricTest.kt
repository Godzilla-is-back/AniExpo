package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.AppThemeMode
import com.example.data.model.StreamServer
import com.example.data.repository.AnimeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context matches SakuraStream`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SakuraStream", appName)
  }

  @Test
  fun `test anime repository seeding and watchlist persistence`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val inMemoryDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    val repo = AnimeRepository(inMemoryDb.animeDao())

    // Initial check
    assertTrue(repo.allAnime.isNotEmpty())
    val firstAnime = repo.allAnime.first()
    assertNotNull(firstAnime)

    // Toggle watchlist
    repo.toggleWatchlist(firstAnime)
    val watchlist = inMemoryDb.animeDao().getAllWatchlist().first()
    assertEquals(1, watchlist.size)
    assertEquals(firstAnime.id, watchlist[0].animeId)

    // Test settings
    repo.updateSettings(repo.settings.value.copy(defaultServer = StreamServer.ANIDB, themeMode = AppThemeMode.CYBER_ROSE))
    assertEquals(StreamServer.ANIDB, repo.settings.value.defaultServer)
    assertEquals(AppThemeMode.CYBER_ROSE, repo.settings.value.themeMode)

    inMemoryDb.close()
  }
}

