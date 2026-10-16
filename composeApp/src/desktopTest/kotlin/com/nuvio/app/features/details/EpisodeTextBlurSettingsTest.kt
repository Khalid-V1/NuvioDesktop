package com.nuvio.app.features.details

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EpisodeTextBlurSettingsTest {
    @BeforeTest
    fun setUp() {
        MetaScreenSettingsStorage.savePayload("{}")
        MetaScreenSettingsRepository.clearLocalState()
    }

    @AfterTest
    fun tearDown() {
        MetaScreenSettingsStorage.savePayload("{}")
        MetaScreenSettingsRepository.clearLocalState()
    }

    @Test
    fun oldSettingsKeepTextBlurDisabled() {
        MetaScreenSettingsStorage.savePayload("""{"blur_unwatched_episodes":true}""")
        MetaScreenSettingsRepository.onProfileChanged()
        assertTrue(MetaScreenSettingsRepository.uiState.value.blurUnwatchedEpisodes)
        assertFalse(MetaScreenSettingsRepository.uiState.value.blurUnwatchedEpisodeText)
    }

    @Test
    fun textBlurPersistsIndependentlyAndResetsToOff() {
        MetaScreenSettingsRepository.setBlurUnwatchedEpisodeText(true)
        MetaScreenSettingsRepository.onProfileChanged()
        assertTrue(MetaScreenSettingsRepository.uiState.value.blurUnwatchedEpisodeText)
        assertFalse(MetaScreenSettingsRepository.uiState.value.blurUnwatchedEpisodes)

        MetaScreenSettingsRepository.setBlurUnwatchedEpisodes(true)
        MetaScreenSettingsRepository.setBlurUnwatchedEpisodeText(false)
        MetaScreenSettingsRepository.onProfileChanged()
        assertFalse(MetaScreenSettingsRepository.uiState.value.blurUnwatchedEpisodeText)
        assertTrue(MetaScreenSettingsRepository.uiState.value.blurUnwatchedEpisodes)

        MetaScreenSettingsRepository.setBlurUnwatchedEpisodeText(true)
        MetaScreenSettingsRepository.resetToDefaults()
        MetaScreenSettingsRepository.onProfileChanged()
        assertFalse(MetaScreenSettingsRepository.uiState.value.blurUnwatchedEpisodeText)
    }

    @Test
    fun loadingAnotherProfilesPayloadDoesNotRetainTextBlur() {
        MetaScreenSettingsRepository.setBlurUnwatchedEpisodeText(true)
        val protectedProfile = MetaScreenSettingsStorage.loadPayload()!!
        MetaScreenSettingsStorage.savePayload("{}")
        MetaScreenSettingsRepository.onProfileChanged()
        assertFalse(MetaScreenSettingsRepository.uiState.value.blurUnwatchedEpisodeText)
        MetaScreenSettingsStorage.savePayload(protectedProfile)
        MetaScreenSettingsRepository.onProfileChanged()
        assertTrue(MetaScreenSettingsRepository.uiState.value.blurUnwatchedEpisodeText)
    }
}
