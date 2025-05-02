package xyz.rfsfernandes.albumlist.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import xyz.rfsfernandes.albumlist.data.local.entities.AlbumEntity

@Dao
interface LeBonCoinDAO {
    @Upsert
    suspend fun upsertAllAlbums(albums: List<AlbumEntity>)

    @Query("SELECT * FROM AlbumEntity ORDER BY id ASC")
    fun albumsPagingSource(): PagingSource<Int, AlbumEntity>

    @Query("DELETE FROM AlbumEntity")
    suspend fun clearAllAlbums()

    @Query("SELECT EXISTS(SELECT 1 FROM AlbumEntity LIMIT 1)")
    suspend fun hasAnyAlbum(): Boolean

    @Query("SELECT id FROM AlbumEntity ORDER BY id ASC")
    suspend fun getAllIds(): List<Int>

    @Query("DELETE FROM AlbumEntity WHERE id IN (:toDelete)")
    suspend fun deleteByIds(toDelete: List<Int?>)

}
