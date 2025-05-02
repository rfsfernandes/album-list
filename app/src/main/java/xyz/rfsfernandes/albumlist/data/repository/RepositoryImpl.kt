package xyz.rfsfernandes.albumlist.data.repository

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

class RepositoryImpl(
    private val leBonCoinService: LeBonCoinService,
    private val leBonCoinDAO: LeBonCoinDAO,
) : Repository {
    override fun getAlbums(): PagingSource<Int, AlbumEntity> = leBonCoinDAO.albumsPagingSource()

    override suspend fun refreshAlbums(): Flow<Resource<Unit>> = flow {
        val hasAnyAlbum = leBonCoinDAO.hasAnyAlbum()
        if (hasAnyAlbum) emit(Resource.CachedSuccess(cacheReason = CacheReason.FROM_DB))
        try {
            val response = leBonCoinService.getAlbums()
            if (response.isSuccessful && response.body() != null) {
                response.body()?.let { body ->
                    val albums = body.map { it.toEntity() }
                    val existingIds = leBonCoinDAO.getAllIds()
                    val newIds = albums.map { it.id }.toSet()

                    val toDelete = existingIds - newIds
                    val toInsert = albums.filter { it.id !in existingIds }

                    leBonCoinDAO.deleteByIds(toDelete)
                    leBonCoinDAO.upsertAllAlbums(toInsert)
                    emit(Resource.Success())
                } ?: run {
                    emit(
                        if (hasAnyAlbum) {
                            Resource.CachedSuccess(cacheReason = CacheReason.EMPTY_BODY)
                        } else {
                            Resource.Error(exception = RemoteException.EmptyBodyException())
                        }
                    )
                }
            } else {
                emit(
                    if (hasAnyAlbum) {
                        Resource.CachedSuccess(cacheReason = CacheReason.NETWORK_ERROR)
                    } else {
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
                    Resource.CachedSuccess(cacheReason = CacheReason.UNKNOWN_ERROR)
                } else {
                    Resource.Error(
                        e.message.toString(),
                        exception = RemoteException.DefaultException()
                    )
                }
            )
        }
    }
}
