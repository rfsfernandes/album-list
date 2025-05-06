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

@OptIn(FlowPreview::class)
class MainScreenViewModel(
    private val getAlbumListUseCase: GetAlbumListUseCase,
    private val observeNetworkStateUseCase: ObserveNetworkStateUseCase,
    private val refreshAlbumsUseCase: RefreshAlbumsUseCase,
) : ViewModel() {


    private val _viewState = MutableStateFlow(MainScreenViewState())
    val viewState = _viewState.asStateFlow()

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
                .debounce(5.seconds) // Just in case network flickers
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
}
