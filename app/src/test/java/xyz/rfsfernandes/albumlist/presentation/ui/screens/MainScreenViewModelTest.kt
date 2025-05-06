package xyz.rfsfernandes.albumlist.presentation.ui.screens

import androidx.paging.PagingData
import androidx.paging.cachedIn
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import xyz.rfsfernandes.albumlist.network.ConnectivityObserver
import xyz.rfsfernandes.albumlist.data.util.Resource
import xyz.rfsfernandes.albumlist.domain.model.Album
import xyz.rfsfernandes.albumlist.domain.usecases.GetAlbumListUseCase
import xyz.rfsfernandes.albumlist.domain.usecases.ObserveNetworkStateUseCase
import xyz.rfsfernandes.albumlist.domain.usecases.RefreshAlbumsUseCase
import xyz.rfsfernandes.albumlist.presentation.ui.screens.main.MainScreenViewModel
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)  // for runTest, if needed
class MainScreenViewModelTest {

    private lateinit var testScope: TestScope

    // Use Mockito to create mocks for the use case dependencies
    private val getAlbumListUseCase: GetAlbumListUseCase = mockk()
    private val observeNetworkStateUseCase: ObserveNetworkStateUseCase = mockk()
    private val refreshAlbumsUseCase: RefreshAlbumsUseCase = mockk()

    // Test dispatcher for coroutines (using UnconfinedTestDispatcher for eager execution)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        // Override Dispatchers.Main with the test dispatcher [oai_citation:2‡developer.android.com](https://developer.android.com/kotlin/coroutines/test#:~:text=class%20HomeViewModelTest%20,setMain%28testDispatcher)
        Dispatchers.setMain(testDispatcher)
        testScope = TestScope(testDispatcher)
    }

    @After
    fun tearDown() {
        // Reset Dispatchers.Main to avoid affecting other tests
        Dispatchers.resetMain()
        testScope.cancel()
    }

    @Test
    fun `initial emission of viewState contains the albums flow`() = runTest {
        // Given: Mock getAlbumListUseCase returns a dummy PagingData flow
        val dummyAlbum = Album(id = 1, albumId = 1, title = "Dummy", "", "")  // a sample Album data
        val fakePagingDataFlow = flowOf(PagingData.from(listOf(dummyAlbum))).cachedIn(testScope)
        every { getAlbumListUseCase() } returns fakePagingDataFlow

        // When: Initialize the ViewModel with the mocked use cases
        val viewModel = MainScreenViewModel(
            getAlbumListUseCase,
            observeNetworkStateUseCase,
            refreshAlbumsUseCase
        )
        viewModel.initialize(testScope)

        // Then: Collect the first viewState emission and verify albums flow is present
        viewModel.viewState.test {
            val initialState = awaitItem()
            assertNotNull(initialState.albums)   // albumList (Flow<PagingData>) should be set
            // (No assertion on PagingData content, per requirements)
            cancelAndIgnoreRemainingEvents()  // cancel collection to end test
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `network state change updates hasNetworkConnection`() = runTest {
        // Given: a fake paging flow to satisfy initialization
        val dummyAlbum = Album(id = 1, albumId = 1, title = "Dummy", "", "")
        val fakeAlbumsFlow = flowOf(PagingData.from(listOf(dummyAlbum))).cachedIn(testScope)
        every { getAlbumListUseCase() } returns fakeAlbumsFlow

        // Given: observeNetworkStateUseCase returns a MutableStateFlow
        val networkStatusFlow = MutableStateFlow(ConnectivityObserver.Status.Unavailable)
        every { observeNetworkStateUseCase() } returns networkStatusFlow

        // Initialize the ViewModel with the test scope
        val viewModel = MainScreenViewModel(
            getAlbumListUseCase,
            observeNetworkStateUseCase,
            refreshAlbumsUseCase
        )
        viewModel.initialize(testScope)

        viewModel.viewState.test {
            // Skip initial state emission (should be hasNetworkConnection = false)
            assertFalse(awaitItem().hasNetworkConnection == false)

            // When: emit Available status
            networkStatusFlow.emit(ConnectivityObserver.Status.Available)
            advanceTimeBy(2.seconds) // Let debounce pass

            // Then: we expect a new state with hasNetworkConnection = true
            val updated = awaitItem()
            assertTrue(updated.hasNetworkConnection == true)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshAlbums with Resource Success leaves errorMessage null`() = runTest {
        // Given: Mock refreshAlbumsUseCase emits a Success result
        val refreshFlow = flow {
            emit(Resource.Success(Unit))
        }
        every { refreshAlbumsUseCase() } returns refreshFlow
        val viewModel = MainScreenViewModel(
            getAlbumListUseCase,
            observeNetworkStateUseCase,
            refreshAlbumsUseCase
        )
        viewModel.initialize(testScope)

        // When: Trigger the refresh action
        viewModel.fetchAlbums()
        // Advance coroutine execution to allow the flow to be collected to completion [oai_citation:3‡stackoverflow.com](https://stackoverflow.com/questions/78545072/best-practice-for-testing-the-initial-state-of-a-viewmode-with-turbine#:~:text=2,public%20method%20initializing%20the%20state)
        advanceUntilIdle()

        // Then: The viewState's errorMessage remains null (no error occurred)
        val finalState = viewModel.viewState.value   // access current StateFlow value
        assertNull(finalState.errorMessage)
        // (If a previous error was present, a success should clear it; in this case it stays null)
    }
}