package xyz.rfsfernandes.albumlist.data.remote

import retrofit2.Response
import retrofit2.http.GET
import xyz.rfsfernandes.albumlist.data.remote.model.AlbumDto

interface LeBonCoinService {

    @GET("img/shared/technical-test.json")
    suspend fun getAlbums(): Response<List<AlbumDto>>
}
