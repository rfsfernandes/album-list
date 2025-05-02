package xyz.rfsfernandes.albumlist.domain.usecases

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import xyz.rfsfernandes.albumlist.data.local.LeBonCoinDAO
import xyz.rfsfernandes.albumlist.domain.mappers.toDomain
import xyz.rfsfernandes.albumlist.domain.model.Album

class GetAlbumListUseCase(
    private val leBonCoinDAO: LeBonCoinDAO,
) {

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

    operator fun invoke(): Flow<PagingData<Album>> {
        return flow
    }
}
