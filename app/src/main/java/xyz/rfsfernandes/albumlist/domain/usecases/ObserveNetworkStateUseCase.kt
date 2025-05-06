package xyz.rfsfernandes.albumlist.domain.usecases

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import xyz.rfsfernandes.albumlist.network.ConnectivityObserver
import xyz.rfsfernandes.albumlist.network.NetworkManager

/**
 * [ObserveNetworkStateUseCase]
 *
 * A use case class responsible for observing the current network connectivity status.
 * It leverages a [NetworkManager] to provide a [StateFlow] that emits the network status.
 *
 * @property networkManager An instance of [NetworkManager] used to interact with network connectivity information.
 */
class ObserveNetworkStateUseCase(
    private val networkManager: NetworkManager
) {

    companion object {
        private const val TAG = "ObserveNetworkStateUseCase"
    }

    /**
     *  Invokes the network connectivity observer to retrieve the current network status as a StateFlow.
     *
     *  This function provides a convenient way to access the real-time network connectivity status.
     *  It internally delegates the responsibility to the `networkManager.networkStatus` property,
     *  which represents a [StateFlow] emitting [ConnectivityObserver.Status] values.
     *
     *  @return A [StateFlow] that emits the current network status as a [ConnectivityObserver.Status].
     *          The emitted values represent the changes in network connectivity.
     *          Subscribing to this flow will provide updates on the network status whenever a change occurs.
     *          The flow will always have a current value.
     */
    operator fun invoke(): Flow<ConnectivityObserver.Status> {
        return networkManager.networkStatus.onEach {
            Log.d(TAG, "invoke: $it")
        }
    }

    fun onStop() {
        networkManager.onStop()
    }
}
