package xyz.rfsfernandes.albumlist.domain.usecases

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import xyz.rfsfernandes.albumlist.network.ConnectivityObserver
import xyz.rfsfernandes.albumlist.network.NetworkConnectivityObserver
import xyz.rfsfernandes.albumlist.network.NetworkManager
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ObserveNetworkStateUseCaseTest {

    private lateinit var useCase: ObserveNetworkStateUseCase
    private lateinit var networkManager: NetworkManager
    private val networkConnectivityObserver: NetworkConnectivityObserver = mockk()

    @Before
    fun setup() {
        // Initialize mocks and use case
        networkManager = mockk()
        useCase = ObserveNetworkStateUseCase(networkManager)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke returns networkStatus StateFlow from NetworkManager`() = runTest {
        // Arrange
        val statusFlow = MutableStateFlow(ConnectivityObserver.Status.Unavailable)
        every { networkManager.networkStatus } returns statusFlow

        // Act
        val resultFlow = useCase()

        // Assert
        assertSame(statusFlow, resultFlow) // Verify the exact StateFlow is returned

        // Test emission
        resultFlow.test {
            assertEquals(ConnectivityObserver.Status.Unavailable, awaitItem())

            // Simulate status change
            statusFlow.value = ConnectivityObserver.Status.Available
            assertEquals(ConnectivityObserver.Status.Available, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `invoke emits status changes from NetworkManager initialization`() = runTest {
        // Arrange: Set up real NetworkManager with mocked observer
        val statusFlow = MutableStateFlow(ConnectivityObserver.Status.Unavailable)
        coEvery { networkConnectivityObserver.observe() } returns statusFlow

        // Initialize NetworkManager with test coroutine scope

        networkManager = NetworkManager(networkConnectivityObserver)

        useCase = ObserveNetworkStateUseCase(networkManager)

        // Advance dispatcher to process initial status
        advanceUntilIdle()

        // Act & Assert
        useCase().test {
            val firstItem = awaitItem()
            assertEquals(ConnectivityObserver.Status.Unavailable, firstItem)

            // Simulate status change
            statusFlow.emit(ConnectivityObserver.Status.Available)
            advanceUntilIdle() // Advance to process new status
            val secondItem = awaitItem()
            assertEquals(ConnectivityObserver.Status.Available, secondItem)

            cancelAndIgnoreRemainingEvents()
        }

        // Clean up
        networkManager.onStop()
    }

    @Test
    fun `invoke handles multiple status transitions correctly`() = runTest {
        // Arrange
        val statusFlow = MutableStateFlow(ConnectivityObserver.Status.Unavailable)
        every { networkManager.networkStatus } returns statusFlow

        // Act & Assert
        useCase().test {
            assertEquals(ConnectivityObserver.Status.Unavailable, awaitItem())

            // Simulate multiple status changes
            statusFlow.value = ConnectivityObserver.Status.Losing
            assertEquals(ConnectivityObserver.Status.Losing, awaitItem())

            statusFlow.value = ConnectivityObserver.Status.Lost
            assertEquals(ConnectivityObserver.Status.Lost, awaitItem())

            statusFlow.value = ConnectivityObserver.Status.Available
            assertEquals(ConnectivityObserver.Status.Available, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}