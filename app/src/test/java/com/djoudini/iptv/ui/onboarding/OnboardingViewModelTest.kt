package com.djoudini.iptv.ui.onboarding

import com.djoudini.iptv.data.preferences.SettingsRepository
import com.djoudini.iptv.data.repository.IptvRepository
import com.djoudini.iptv.domain.model.ProviderType
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private lateinit var viewModel: OnboardingViewModel
    private val repository: IptvRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = OnboardingViewModel(repository, settingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selectProvider updates step to Login`() = runTest {
        viewModel.selectProvider(ProviderType.XTREAM)
        assertEquals(OnboardingStep.Login(ProviderType.XTREAM), viewModel.uiState.value.currentStep)
    }
}
