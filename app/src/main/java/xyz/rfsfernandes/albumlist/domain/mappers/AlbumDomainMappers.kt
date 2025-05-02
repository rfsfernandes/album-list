package xyz.rfsfernandes.albumlist.domain.mappers

import xyz.rfsfernandes.albumlist.data.local.entities.AlbumEntity
import xyz.rfsfernandes.albumlist.domain.model.Album

fun AlbumEntity.toDomain(): Album {
    return Album(
        id = id,
        albumId = albumId,
        title = title,
        url = url,
        thumbnailUrl = thumbnailUrl
    )
}

fun Album.toEntity(): AlbumEntity {
    return AlbumEntity(
        id = id,
        albumId = albumId,
        title = title,
        url = url,
        thumbnailUrl = thumbnailUrl
    )
}
