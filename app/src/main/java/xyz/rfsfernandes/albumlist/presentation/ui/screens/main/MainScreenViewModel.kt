package xyz.rfsfernandes.albumlist.presentation.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.rfsfernandes.albumlist.R
import xyz.rfsfernandes.albumlist.network.ConnectivityObserver
import xyz.rfsfernandes.albumlist.data.remote.RemoteException
import xyz.rfsfernandes.albumlist.data.util.CacheReason
import xyz.rfsfernandes.albumlist.data.util.Resource
import xyz.rfsfernandes.albumlist.domain.usecases.GetAlbumListUseCase
import xyz.rfsfernandes.albumlist.domain.usecases.ObserveNetworkStateUseCase
import xyz.rfsfernandes.albumlist.domain.usecases.RefreshAlbumsUseCase
import xyz.rfsfernandes.albumlist.presentation.mappers.toDataModel
import kotlin.time.Duration.Companion.seconds

/**
 * [MainScreenViewModel] is responsible for managing the state and business logic
 * of the main screen.
 *
 * It interacts with use cases to:
 *  - Retrieve and cache a list of albums.
 *  - Observe network connectivity changes.
 *  - Refresh the album list from the remote data source.
 *
 * It exposes a [viewState] to the UI, which represents the current state of the main screen.
 *
 * @property getAlbumListUseCase Use case to retrieve the list of albums.
 * @property observeNetworkStateUseCase Use case to observe changes in network connectivity.
 * @property refreshAlbumsUseCase Use case to refresh the album list from the remote data source.
 */
@OptIn(FlowPreview::class)
class MainScreenViewModel(
    private val getAlbumListUseCase: GetAlbumListUseCase,
    private val observeNetworkStateUseCase: ObserveNetworkStateUseCase,
    private val refreshAlbumsUseCase: RefreshAlbumsUseCase,
) : ViewModel() {


    private val _viewState = MutableStateFlow(MainScreenViewState())
    val viewState = _viewState.asStateFlow()

    /**
     * Initializes the view state and sets up network observation.
     *
     * This function performs the following tasks:
     * 1. **Fetches and loads initial album data:** It retrieves the list of albums using `getAlbumListUseCase()`,
     *    maps them to the data model, and caches the result in the provided [scope]. This ensures that the data
     *    is readily available and avoids repeated network requests. The result is then used to update the `albums`
     *    property of the [_viewState].
     * 2. **Observes network connectivity changes:** It starts observing the network state using
     *    `observeNetworkStateUseCase()`.
     * 3. **Debounces network state changes:** A debounce of 5 seconds is applied to the network state updates. This prevents
     *    rapid updates in case of network flickers, providing stability.
     * 4. **Triggers album fetch on network availability:** When the network becomes available
     *    (`ConnectivityObserver.Status.Available`), it calls the `fetchAlbums()` function to refresh the album data.
     * 5. **Updates network connection status:** It updates the `hasNetworkConnection` property of the [_viewState]
     *    based on the current network status.
     *
     * @param scope The [CoroutineScope] in which to launch the coroutines. Defaults to `viewModelScope`.
     */
    fun initialize(scope: CoroutineScope = viewModelScope) {
        scope.launch {
            _viewState.update {
                _viewState.value.copy(
                    albums = getAlbumListUseCase().cachedIn(scope)
                        .map { it.map { it.toDataModel() } }
                )
            }
        }

        scope.launch {
            observeNetworkStateUseCase()
                .filter { it != ConnectivityObserver.Status.Unknown }
                .debounce(2.seconds) // Just in case network flickers
                .collect { status ->
                    if (status == ConnectivityObserver.Status.Available) {
                        fetchAlbums(scope)
                    }

                    _viewState.update {
                        _viewState.value.copy(
                            hasNetworkConnection = status == ConnectivityObserver.Status.Available
                        )
                    }
                }
        }
    }

    /**
     * Fetches and refreshes the list of albums.
     *
     * This function initiates the process of fetching album data using the `refreshAlbumsUseCase`.
     * It handles various outcomes of the data retrieval process, including success, failure, and
     * cached data scenarios. The function updates the internal `_viewState` based on the results
     * to reflect the current state of the album data and potential errors to the UI.
     *
     * @param scope The CoroutineScope in which the data fetching operation will be launched.
     *              Defaults to `viewModelScope` if not provided. This allows for the operation to be tied
     *              to the lifecycle of the ViewModel.
     *
     * Operation Details:
     * - It uses `refreshAlbumsUseCase` which is responsible to get the data from network or from cache.
     * - It collects the result of the flow returned by `refreshAlbumsUseCase`.
     * - It updates the `_viewState` based on the `Resource` type received:
     *   - `Resource.CachedSuccess`: Indicates that data was retrieved from the cache.
     *     - `CacheReason.FROM_DB`: No error, data was successfully retrieved from the local database.
     *     - `CacheReason.EMPTY_BODY`: An error indicating an empty body response from the cache.
     *     - `CacheReason.NETWORK_ERROR`: An error indicating a network issue when attempting to cache data.
     *     - `CacheReason.UNKNOWN_ERROR` or null:  An error indicating an unknown issue when caching.
     *   - `Resource.Error`: Indicates an error during data retrieval.
     *     - `RemoteException.EmptyBodyException`: An error indicating an empty response body from the network.
     *     - `RemoteException.NetworkException`: An error indicating a network issue.
     */
    fun fetchAlbums(scope: CoroutineScope = viewModelScope) {
        scope.launch {
            refreshAlbumsUseCase().collect { result ->
                _viewState.update {
                    _viewState.value.copy(
                        errorMessage = when (result) {
                            is Resource.CachedSuccess<Unit> -> {
                                when (result.cacheReason) {
                                    CacheReason.FROM_DB -> null
                                    CacheReason.EMPTY_BODY -> R.string.cache_empty_body_response
                                    CacheReason.NETWORK_ERROR -> R.string.cache_network_error
                                    CacheReason.UNKNOWN_ERROR,
                                    null -> R.string.cache_unknown_error
                                }
                            }

                            is Resource.Error<Unit> -> {
                                when (result.exception) {
                                    is RemoteException.EmptyBodyException -> R.string.empty_body
                                    is RemoteException.NetworkException -> R.string.fetch_network_error
                                    is RemoteException.DefaultException,
                                    null -> R.string.unknown_error
                                }
                            }

                            is Resource.Success<Unit>,
                            is Resource.Default<Unit> -> null
                        }
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        observeNetworkStateUseCase.onStop()
    }
}
