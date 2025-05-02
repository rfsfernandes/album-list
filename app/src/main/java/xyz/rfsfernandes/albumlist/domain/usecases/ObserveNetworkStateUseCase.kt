package xyz.rfsfernandes.albumlist.domain.usecases

import kotlinx.coroutines.flow.StateFlow
import xyz.rfsfernandes.albumlist.data.network.ConnectivityObserver
import xyz.rfsfernandes.albumlist.data.network.NetworkManager

class ObserveNetworkStateUseCase(
    private val networkManager: NetworkManager
) {
    operator fun invoke(): StateFlow<ConnectivityObserver.Status> {
        return networkManager.networkStatus
    }
}
