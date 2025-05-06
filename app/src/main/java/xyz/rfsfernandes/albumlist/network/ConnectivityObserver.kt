package xyz.rfsfernandes.albumlist.network

import kotlinx.coroutines.flow.Flow

/**
 * Interface for observing the network connectivity status of the device.
 *
 * This interface provides a way to monitor changes in network availability,
 * including when the network becomes available, unavailable, is in the process
 * of losing connectivity, or has completely lost connectivity.
 */
interface ConnectivityObserver {

    fun observe(): Flow<Status>

    enum class Status {
        Available, Unavailable, Losing, Lost, Unknown
    }
}
