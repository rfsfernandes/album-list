package xyz.rfsfernandes.albumlist.domain.usecases

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import xyz.rfsfernandes.albumlist.data.remote.RemoteException
import xyz.rfsfernandes.albumlist.data.repository.Repository
import xyz.rfsfernandes.albumlist.data.util.CacheReason
import xyz.rfsfernandes.albumlist.data.util.Resource
import kotlin.test.assertIs


@OptIn(ExperimentalCoroutinesApi::class)
class RefreshAlbumsUseCaseTest {

    private lateinit var useCase: RefreshAlbumsUseCase
    private val repository: Repository = mockk()

    @Before
    fun setup() {
        useCase = RefreshAlbumsUseCase(repository)
    }

    @Test
    fun `fetched albums successfully from service then returns Resource Success`() = runTest {
        // Arrange
        every { repository.refreshAlbums() } returns flowOf(Resource.Success())

        // Act & Assert
        useCase().test {
            assertIs<Resource.Success<Unit>>(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `fetched albums successfully but albums where cached then returns Resource CachedSuccess`() =
        runTest {
            // Arrange
            every { repository.refreshAlbums() } returns flowOf(
                Resource.CachedSuccess(cacheReason = CacheReason.FROM_DB)
            )

            coEvery { repository.hasAnyAlbum() } returns true

            // Act & Assert
            useCase().test {
                val actualItem = awaitItem()
                assertIs<Resource.CachedSuccess<Unit>>(actualItem)
                assertEquals(CacheReason.FROM_DB, actualItem.cacheReason)
                awaitComplete()
            }
        }

    @Test
    fun `fetched albums unsuccessfully then returns Resource Error`() = runTest {
        // Arrange
        every { repository.refreshAlbums() } returns flowOf(
            Resource.Error("Error", exception = RemoteException.NetworkException())
        )

        // Act & Assert
        useCase().test {
            assertIs<Resource.Error<Unit>>(
                awaitItem()
            )
            awaitComplete()
        }
    }
}
