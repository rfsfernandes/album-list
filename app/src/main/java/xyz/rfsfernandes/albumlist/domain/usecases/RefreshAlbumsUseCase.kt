package xyz.rfsfernandes.albumlist.domain.usecases

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import xyz.rfsfernandes.albumlist.data.repository.Repository
import xyz.rfsfernandes.albumlist.data.util.Resource

/**
 * A use case responsible for refreshing the list of albums.
 *
 * This class encapsulates the logic for refreshing album data. It interacts with the
 * [Repository] to perform the actual data refresh operation.
 *
 * @property repository The repository used to interact with the data source and perform the refresh.
 */
class RefreshAlbumsUseCase(
    private val repository: Repository
) {

    companion object {
        private const val TAG = "RefreshAlbumsUseCase"
    }

    /**
     * Invokes the repository's refreshAlbums method to trigger a refresh of album data.
     *
     * This function acts as a convenient way to initiate a data refresh operation.
     * It delegates the actual refreshing logic to the underlying repository.
     *
     * @return A [Flow] emitting [Resource] wrapping a [Unit].
     *         - The [Flow] represents the asynchronous stream of updates regarding the refresh operation.
     *         - [Resource] is a wrapper type that can represent various states of the refresh operation, such as:
     *           - Loading: Indicating that the refresh is in progress.
     *           - Success: Indicating that the refresh completed successfully.
     *           - Error: Indicating that an error occurred during the refresh.
     *         - [Unit] represents the absence of a specific data payload in the success state. It simply means the refresh operation was successful.
     *
     * Example usage:
     * ```kotlin
     * someObject.invoke().collect { resource ->
     *     when (resource) {
     *         is Resource.Loading -> {
     *             // Show loading indicator
     *         }
     *         is Resource.Success -> {
     *             // Data refresh completed successfully
     *         }
     *         is Resource.Error -> {
     *             // Handle error, e.g., show error message
     *             println("Error refreshing albums: ${resource.message}")
     *         }
     *     }
     * }
     * ```
     */
    operator fun invoke(): Flow<Resource<Unit>> {
        return repository.refreshAlbums().onEach {
            Log.d(TAG, "invoke: $it")
        }
    }
}
