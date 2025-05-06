package xyz.rfsfernandes.albumlist.domain.usecases

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import xyz.rfsfernandes.albumlist.data.local.LeBonCoinDAO
import xyz.rfsfernandes.albumlist.domain.mappers.toDomain
import xyz.rfsfernandes.albumlist.domain.model.Album

/**
 * `GetAlbumListUseCase` is a Use Case class responsible for retrieving a paginated list of Albums.
 *
 * It utilizes the `LeBonCoinDAO` to fetch the data and leverages the Android Paging library
 * to efficiently load and manage large datasets.
 *
 * The class exposes a `Flow` of `PagingData<Album>`, which can be collected by the UI
 * to display the albums in a list.
 *
 * @property leBonCoinDAO The Data Access Object (DAO) responsible for interacting with the data source
 *                       (e.g., a local database or a remote API). It should provide a `albumsPagingSource` method
 *                       to create a PagingSource for albums.
 */
class GetAlbumListUseCase(
    private val leBonCoinDAO: LeBonCoinDAO,
) {

    companion object {
        private const val TAG = "GetAlbumListUseCase"
    }

    private val flow = Pager(
        config = PagingConfig(
            pageSize = 40,
            enablePlaceholders = false,
            initialLoadSize = 40
        ),
        pagingSourceFactory = {
            leBonCoinDAO.albumsPagingSource()
        }
    ).flow.map {
        it.map { it.toDomain() }
    }

    /**
     * Invokes the data source and returns a [Flow] of [PagingData] containing [Album] objects.
     *
     * This function is designed to be used as a streamlined way to access the paginated data.
     * It effectively acts as a trigger for loading and emitting data.
     *
     * @return A [Flow] that emits [PagingData] instances, each containing a page of [Album] data.
     *         This [Flow] will continue to emit new [PagingData] objects as new pages are loaded.
     */
    operator fun invoke(): Flow<PagingData<Album>> {
        return flow.onEach {
            Log.d(TAG, "invoke: $it")
        }
    }
}
