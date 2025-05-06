package xyz.rfsfernandes.albumlist.presentation.ui.screens

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import xyz.rfsfernandes.albumlist.R
import xyz.rfsfernandes.albumlist.data.remote.RemoteException
import xyz.rfsfernandes.albumlist.data.util.CacheReason
import xyz.rfsfernandes.albumlist.data.util.Resource
import xyz.rfsfernandes.albumlist.domain.usecases.GetAlbumListUseCase
import xyz.rfsfernandes.albumlist.domain.usecases.ObserveNetworkStateUseCase
import xyz.rfsfernandes.albumlist.domain.usecases.RefreshAlbumsUseCase
import xyz.rfsfernandes.albumlist.presentation.ui.screens.main.MainScreenViewModel
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModelTest {

    private lateinit var getAlbumListUseCase: GetAlbumListUseCase
    private lateinit var observeNetworkStateUseCase: ObserveNetworkStateUseCase
    private lateinit var refreshAlbumsUseCase: RefreshAlbumsUseCase
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getAlbumListUseCase = mockk()
        observeNetworkStateUseCase = mockk()
        refreshAlbumsUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun refreshAlbumsWithSuccessDoesntSetErrorMessageInState() = runTest {
        // Given: Mock refreshAlbumsUseCase emits a Loading followed by an Error result
        val refreshFlow = flow<Resource.Success<Unit>> {
            emit(Resource.Success())     // then emit an error result
        }
        every { refreshAlbumsUseCase() } returns refreshFlow
        val viewModel = MainScreenViewModel(
            getAlbumListUseCase,
            observeNetworkStateUseCase,
            refreshAlbumsUseCase
        )
        viewModel.initialize(testScope)
        // When: Collect viewState and trigger the refresh action
        viewModel.viewState.test {
            // Initial state should have no error
            val initial = awaitItem()
            assertNull(initial.errorMessage)

            // Trigger refreshAlbums (starts collecting the refreshFlow)
            viewModel.fetchAlbums()

            advanceUntilIdle()

            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun refreshAlbumsWithResourceErrorNetworkExceptionSetsErrorMessageInState() = runTest {
        // Given: Mock refreshAlbumsUseCase emits a Loading followed by an Error result
        val errorMessage = R.string.fetch_network_error
        val refreshFlow = flow<Resource.Error<Unit>> {
            emit(Resource.Error(context.getString(errorMessage), RemoteException.NetworkException()))     // then emit an error result
        }
        every { refreshAlbumsUseCase() } returns refreshFlow
        val viewModel = MainScreenViewModel(
            getAlbumListUseCase,
            observeNetworkStateUseCase,
            refreshAlbumsUseCase
        )
        viewModel.initialize(testScope)
        // When: Collect viewState and trigger the refresh action
        viewModel.viewState.test {
            // Initial state should have no error
            val initial = awaitItem()
            assertNull(initial.errorMessage)

            // Trigger refreshAlbums (starts collecting the refreshFlow)
            viewModel.fetchAlbums()

            val nextState = awaitItem()
            assertEquals(context.getString(errorMessage), context.getString(nextState.errorMessage!!))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun refreshAlbumsWithCachedSuccessNetworkErrorSetsErrorMessageInState() = runTest {
        // Given: Mock refreshAlbumsUseCase emits a Loading followed by an Error result
        val errorMessage = R.string.cache_network_error
        val refreshFlow = flow<Resource.CachedSuccess<Unit>> {
            emit(Resource.CachedSuccess(cacheReason = CacheReason.NETWORK_ERROR))     // then emit an error result
        }
        every { refreshAlbumsUseCase() } returns refreshFlow
        val viewModel = MainScreenViewModel(
            getAlbumListUseCase,
            observeNetworkStateUseCase,
            refreshAlbumsUseCase
        )
        viewModel.initialize(testScope)
        // When: Collect viewState and trigger the refresh action
        viewModel.viewState.test {
            // Initial state should have no error
            val initial = awaitItem()
            assertNull(initial.errorMessage)

            // Trigger refreshAlbums (starts collecting the refreshFlow)
            viewModel.fetchAlbums()

            val nextState = awaitItem()
            assertEquals(context.getString(errorMessage), context.getString(nextState.errorMessage!!))
            cancelAndIgnoreRemainingEvents()
        }
    }
}