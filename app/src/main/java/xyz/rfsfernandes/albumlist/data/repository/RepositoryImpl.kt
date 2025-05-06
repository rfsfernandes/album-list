package xyz.rfsfernandes.albumlist.data.repository

import android.util.Log
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import xyz.rfsfernandes.albumlist.data.local.LeBonCoinDAO
import xyz.rfsfernandes.albumlist.data.local.entities.AlbumEntity
import xyz.rfsfernandes.albumlist.data.mappers.toEntity
import xyz.rfsfernandes.albumlist.data.remote.LeBonCoinService
import xyz.rfsfernandes.albumlist.data.remote.RemoteException
import xyz.rfsfernandes.albumlist.data.util.CacheReason
import xyz.rfsfernandes.albumlist.data.util.Resource

/**
 * Concrete implementation of the [Repository] interface.
 *
 * This class handles the logic for interacting with both the remote data source (API)
 * and the local data source (database) for fetching and managing album data. It utilizes
 * the [LeBonCoinService] for network requests and the [LeBonCoinDAO] for database operations.
 *
 * @property leBonCoinService The service used for making API calls to fetch album data.
 * @property leBonCoinDAO The DAO (Data Access Object) used for database interactions.
 */
class RepositoryImpl(
    private val leBonCoinService: LeBonCoinService,
    private val leBonCoinDAO: LeBonCoinDAO,
) : Repository {

    companion object {
        private const val TAG = "RepositoryImpl"
    }

    /**
     * Checks if there is at least one album in the local data source.
     *
     * @return `true` if at least one album exists, `false` otherwise.
     */
    override suspend fun hasAnyAlbum(): Boolean = leBonCoinDAO.hasAnyAlbum()

    /**
     * Retrieves a [PagingSource] for fetching albums from the local database.
     *
     * This function leverages the underlying `leBonCoinDAO` (Data Access Object) to provide a
     * [PagingSource] that can be used with the Paging 3 library to efficiently load and display
     * paginated album data. The paging source handles loading data in chunks as needed, based on
     * scrolling or other user interactions.
     *
     * @return A [PagingSource] emitting [AlbumEntity] instances, keyed by page number (Int).
     *
     * @see PagingSource
     * @see AlbumEntity
     * @see leBonCoinDAO
     */
    override fun getAlbums(): PagingSource<Int, AlbumEntity> = leBonCoinDAO.albumsPagingSource()

    /**
     * Refreshes the list of albums from the remote data source and updates the local database.
     *
     * This function fetches albums from the `leBonCoinService`, compares them with existing albums in the
     * `leBonCoinDAO`, and performs the necessary database operations (delete, upsert) to synchronize the data.
     * It emits a [Flow] of [Resource] to indicate the success or failure of the operation and provide data.
     *
     * The flow emits the following states:
     * - [Resource.CachedSuccess]: When data is retrieved from the database. This is emitted if the db contains any albums before attempting to fetch from network.
     *    - `cacheReason`: Indicates why a cached version is returned. Possible values:
     *      - [CacheReason.FROM_DB]: When there is any album in db when the function is called.
     *      - [CacheReason.EMPTY_BODY]: When the network response is successful but the body is empty.
     *      - [CacheReason.NETWORK_ERROR]: When the network response is unsuccessful (e.g., 404, 500).
     *      - [CacheReason.UNKNOWN_ERROR]: When an exception occurs during the network request.
     * - [Resource.Success]: When the albums are successfully fetched from the network, processed, and updated in the database.
     * - [Resource.Error]: When an error occurs during the network request or data processing.
     *    - `message`: Error message (optional).
     *    - `exception`: The specific exception that occurred. Possible values:
     *      - [RemoteException.EmptyBodyException]: When the network response is successful but the body is empty and there is no album in db.
     *      - [RemoteException.NetworkException]: When the network response is unsuccessful (e.g., 404, 500) and there is no album in db.
     *      - [RemoteException.DefaultException]: When an unexpected exception occurs during the network request and there is no album in db.
     *
     * @return A [Flow] of [Resource] indicating the outcome of the refresh operation.
     */
    override fun refreshAlbums(): Flow<Resource<Unit>> = flow {
        val hasAnyAlbum = hasAnyAlbum()
        if (hasAnyAlbum) {
            emit(Resource.CachedSuccess(cacheReason = CacheReason.FROM_DB))
            Log.d(TAG, "refreshAlbums: emitted CachedSuccess FROM_DB")
        }
        try {
            val response = leBonCoinService.getAlbums()
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    val albums = body.map { it.toEntity() }
                    val existingIds = leBonCoinDAO.getAllIds()
                    val newIds = albums.map { it.id }.toSet()

                    val toDelete = existingIds - newIds
                    val toInsert = albums.filter { it.id !in existingIds }

                    leBonCoinDAO.deleteByIds(toDelete)
                    leBonCoinDAO.upsertAllAlbums(toInsert)
                    emit(Resource.Success())
                    Log.d(TAG, "refreshAlbums: emitted Success. ${albums.size} Items upserted")
                } ?: run {
                    emit(
                        if (hasAnyAlbum) {
                            Log.d(TAG, "refreshAlbums: emitted CachedSuccess EMPTY_BODY")
                            Resource.CachedSuccess(cacheReason = CacheReason.EMPTY_BODY)
                        } else {
                            Log.d(TAG, "refreshAlbums: emitted RemoteException EmptyBodyException")
                            Resource.Error(exception = RemoteException.EmptyBodyException())
                        }
                    )
                }
            } else {
                emit(
                    if (hasAnyAlbum) {
                        Log.d(TAG, "refreshAlbums: emitted CachedSuccess NETWORK_ERROR")
                        Resource.CachedSuccess(cacheReason = CacheReason.NETWORK_ERROR)
                    } else {
                        Log.d(TAG, "refreshAlbums: emitted RemoteException NetworkException")
                        Resource.Error(
                            response.message(),
                            exception = RemoteException.NetworkException()
                        )
                    }
                )
            }
        } catch (e: Exception) {
            emit(
                if (hasAnyAlbum) {
                    Log.d(TAG, "refreshAlbums: emitted CachedSuccess UNKNOWN_ERROR")
                    Resource.CachedSuccess(cacheReason = CacheReason.UNKNOWN_ERROR)
                } else {
                    Log.d(TAG, "refreshAlbums: emitted RemoteException DefaultException")
                    Resource.Error(
                        e.message.toString(),
                        exception = RemoteException.DefaultException()
                    )
                }
            )
        }
    }
}
