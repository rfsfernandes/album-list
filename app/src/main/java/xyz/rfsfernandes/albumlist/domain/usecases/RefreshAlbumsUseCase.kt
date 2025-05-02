package xyz.rfsfernandes.albumlist.domain.usecases

import kotlinx.coroutines.flow.Flow
import xyz.rfsfernandes.albumlist.data.repository.Repository
import xyz.rfsfernandes.albumlist.data.util.Resource

class RefreshAlbumsUseCase(
    private val repository: Repository
) {
    suspend operator fun invoke(): Flow<Resource<Unit>> {
        return repository.refreshAlbums()
    }
}
