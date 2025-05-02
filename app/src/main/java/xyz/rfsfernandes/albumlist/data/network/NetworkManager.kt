package xyz.rfsfernandes.albumlist.data.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class NetworkManager(
    private val networkConnectivityObserver: NetworkConnectivityObserver,
) {
    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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