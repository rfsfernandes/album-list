package xyz.rfsfernandes.albumlist.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * `NetworkManager` is a class responsible for observing and managing the network connectivity status.
 *
 * It utilizes a `NetworkConnectivityObserver` to track changes in the network's state and
 * exposes this information through a `StateFlow`.
 *
 * This class is designed to be used within a lifecycle-aware context, allowing for automatic
 * coroutine cancellation when the associated lifecycle ends.
 *
 * @param networkConnectivityObserver An instance of `NetworkConnectivityObserver` used to detect
 *                                    network connectivity changes.
 */
class NetworkManager(
    private val networkConnectivityObserver: NetworkConnectivityObserver,
) {
    var coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _networkStatus = MutableStateFlow(ConnectivityObserver.Status.Unavailable)
    val networkStatus: StateFlow<ConnectivityObserver.Status> = _networkStatus

    init {
        networkConnectivityObserver.observe().onEach {
            _networkStatus.value = it
        }.launchIn(coroutineScope)
    }

    fun onStop() {
        coroutineScope.cancel()
    }
}