package xyz.rfsfernandes.albumlist.presentation.mappers

import xyz.rfsfernandes.albumlist.domain.model.Album
import xyz.rfsfernandes.albumlist.presentation.model.AlbumDataModel

fun Album.toDataModel(): AlbumDataModel {
    return AlbumDataModel(
        id = id,
        albumId = albumId,
        title = title,
        url = url,
        thumbnailUrl = thumbnailUrl
    )
}

fun AlbumDataModel.toAlbum(): Album {
    return Album(
        id = id,
        albumId = albumId,
        title = title,
        url = url,
        thumbnailUrl = thumbnailUrl
    )
}
