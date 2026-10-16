package com.nuvio.app.features.details

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performMouseInput
import com.nuvio.app.core.ui.NuvioTheme
import com.nuvio.app.core.i18n.localizedSeasonEpisodeCode
import com.nuvio.app.features.details.components.DetailSeriesContent
import com.nuvio.app.features.details.components.DetailSeriesListEpisode
import com.nuvio.app.features.watched.watchedItemKeys
import com.nuvio.app.features.watchprogress.WatchProgressEntry
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class EpisodeTextBlurTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun episodeTextCanBeRevealedWithoutPlayingInEveryBrowsingLayout() {
        val enabled = mutableStateOf(false)
        val thumbnailBlur = mutableStateOf(false)
        val watchedKeys = mutableStateOf(emptySet<String>())
        val progress = mutableStateOf(emptyMap<String, WatchProgressEntry>())
        val layout = mutableStateOf(0)
        var playbackClicks = 0
        val episode = MetaVideo(
            id = "tt1234567:1:1",
            title = "An unexpected betrayal",
            overview = "The trusted ally reveals the secret.",
            season = 1,
            episode = 1,
        )
        val meta = MetaDetails("tt1234567", "series", "Series", videos = listOf(episode))
        val reveal = "Reveal episode title and description"
        val blur = "Blur episode title and description"
        val episodeCode = localizedSeasonEpisodeCode(1, 1)!!
        compose.setContent {
            NuvioTheme {
                if (layout.value == 2) {
                    DetailSeriesListEpisode(
                        meta = meta,
                        episode = episode,
                        progressByVideoId = progress.value,
                        watchedKeys = watchedKeys.value,
                        episodeRatings = emptyMap(),
                        episodeRatingsVisibility = EpisodeRatingsVisibility.SHOW_ALL,
                        blurUnwatchedEpisodes = thumbnailBlur.value,
                        blurUnwatchedEpisodeText = enabled.value,
                        onEpisodeClick = { playbackClicks++ },
                        onEpisodeLongPress = null,
                    )
                } else {
                    DetailSeriesContent(
                        meta = meta,
                        episodeCardStyle = if (layout.value == 0) MetaEpisodeCardStyle.Horizontal else MetaEpisodeCardStyle.List,
                        progressByVideoId = progress.value,
                        watchedKeys = watchedKeys.value,
                        blurUnwatchedEpisodes = thumbnailBlur.value,
                        blurUnwatchedEpisodeText = enabled.value,
                        onEpisodeClick = { playbackClicks++ },
                    )
                }
            }
        }
        for (currentLayout in 0..2) {
            compose.runOnIdle {
                layout.value = currentLayout
                enabled.value = false
                thumbnailBlur.value = true
                watchedKeys.value = emptySet()
                progress.value = emptyMap()
            }
            // Thumbnail blur does not opt a user into text blur.
            compose.onNodeWithText(episode.title).assertIsDisplayed()
            compose.onNodeWithText(episode.overview!!).assertIsDisplayed()
            compose.onNodeWithContentDescription(reveal).assertDoesNotExist()

            compose.runOnIdle {
                enabled.value = true
                thumbnailBlur.value = false
            }
            // Text is removed from accessibility semantics while visually blurred.
            compose.onNodeWithText(episode.title).assertDoesNotExist()
            compose.onNodeWithText(episode.overview!!).assertDoesNotExist()
            compose.onNodeWithText(episodeCode).assertIsDisplayed()
            compose.onNodeWithContentDescription(reveal).assertIsDisplayed().performMouseInput { click() }
            compose.onNodeWithText(episode.title).assertIsDisplayed()
            compose.onNodeWithText(episode.overview!!).assertIsDisplayed()
            compose.onNodeWithContentDescription(blur).performMouseInput { click() }
            compose.onNodeWithText(episode.title).assertDoesNotExist()
            compose.runOnIdle { assertEquals(0, playbackClicks) }

            compose.runOnIdle { watchedKeys.value = watchedItemKeys(meta.type, meta.id, 1, 1).toSet() }
            compose.onNodeWithText(episode.title).assertIsDisplayed()
            compose.onNodeWithContentDescription(reveal).assertDoesNotExist()

            compose.runOnIdle { watchedKeys.value = emptySet() }
            compose.onNodeWithText(episode.title).assertDoesNotExist()
            compose.onNodeWithContentDescription(reveal).performMouseInput { click() }
            compose.runOnIdle { enabled.value = false }
            compose.onNodeWithText(episode.title).assertIsDisplayed()
            compose.runOnIdle { enabled.value = true }
            // Turning protection back on resets an earlier temporary reveal.
            compose.onNodeWithText(episode.title).assertDoesNotExist()

            compose.runOnIdle {
                progress.value = mapOf(episode.id to WatchProgressEntry(
                    contentType = meta.type,
                    parentMetaId = meta.id,
                    parentMetaType = meta.type,
                    videoId = episode.id,
                    title = meta.name,
                    seasonNumber = 1,
                    episodeNumber = 1,
                    lastPositionMs = 1000,
                    durationMs = 1000,
                    isCompleted = true,
                    lastUpdatedEpochMs = 1,
                ))
            }
            compose.onNodeWithText(episode.title).assertIsDisplayed()
            compose.onNodeWithContentDescription(reveal).assertDoesNotExist()
        }
    }
}
