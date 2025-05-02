package xyz.rfsfernandes.albumlist.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val albumId: Int? = null,
    val title: String? = null,
    val url: String? = null,
    val thumbnailUrl: String? = null
)