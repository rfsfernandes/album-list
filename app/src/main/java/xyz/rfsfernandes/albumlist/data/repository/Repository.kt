package xyz.rfsfernandes.albumlist.data.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import xyz.rfsfernandes.albumlist.data.local.entities.AlbumEntity
import xyz.rfsfernandes.albumlist.data.util.Resource

interface Repository {
    fun getAlbums(): PagingSource<Int, AlbumEntity>
    suspend fun refreshAlbums(): Flow<Resource<Unit>>
}
