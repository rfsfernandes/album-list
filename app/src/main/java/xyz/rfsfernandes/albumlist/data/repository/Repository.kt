package xyz.rfsfernandes.albumlist.data.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import xyz.rfsfernandes.albumlist.data.local.entities.AlbumEntity
import xyz.rfsfernandes.albumlist.data.util.Resource

interface Repository {
    suspend fun hasAnyAlbum(): Boolean
    fun getAlbums(): PagingSource<Int, AlbumEntity>
    fun refreshAlbums(): Flow<Resource<Unit>>
}
