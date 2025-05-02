package xyz.rfsfernandes.albumlist.data.mappers

import xyz.rfsfernandes.albumlist.data.local.entities.AlbumEntity
import xyz.rfsfernandes.albumlist.data.remote.model.AlbumDto

fun AlbumDto.toEntity(): AlbumEntity {
    return AlbumEntity(
        id = id,
        albumId = albumId,
        title = title,
        url = url,
        thumbnailUrl = thumbnailUrl
    )
}

fun AlbumEntity.toDto(): AlbumDto {
    return AlbumDto(
        id = id,
        albumId = albumId,
        title = title,
        url = url,
        thumbnailUrl = thumbnailUrl
    )
}
