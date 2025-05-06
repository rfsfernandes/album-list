package xyz.rfsfernandes.albumlist.data.repository

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import xyz.rfsfernandes.albumlist.data.local.LeBonCoinDAO
import xyz.rfsfernandes.albumlist.data.mappers.toEntity
import xyz.rfsfernandes.albumlist.data.remote.LeBonCoinService
import xyz.rfsfernandes.albumlist.data.remote.RemoteException
import xyz.rfsfernandes.albumlist.data.remote.model.AlbumDto
import xyz.rfsfernandes.albumlist.data.util.CacheReason
import xyz.rfsfernandes.albumlist.data.util.Resource
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RepositoryImplTest {

    private lateinit var repository: RepositoryImpl
    private val leBonCoinService: LeBonCoinService = mockk()
    private val leBonCoinDAO: LeBonCoinDAO = mockk()

    @Before
    fun setup() {
        repository = RepositoryImpl(leBonCoinService, leBonCoinDAO)
    }

    @Test
    fun `refreshAlbums emits CachedSuccess with FROM_DB when DB has albums and then CachedSuccess with UNKNOWN_ERROR when API call fails`() =
        runTest {
            // Arrange
            coEvery { leBonCoinDAO.hasAnyAlbum() } returns true
            coEvery { leBonCoinService.getAlbums() } throws RuntimeException("Service unavailable")

            // Act & Assert
            repository.refreshAlbums().test {
                // First emission: CachedSuccess from DB check
                val firstItem = awaitItem()
                assertIs<Resource.CachedSuccess<Unit>>(firstItem)
                assertEquals(CacheReason.FROM_DB, firstItem.cacheReason)
                assertNull(firstItem.data)
                assertNull(firstItem.message)

                // Second emission: CachedSuccess from exception
                val secondItem = awaitItem()
                assertIs<Resource.CachedSuccess<Unit>>(secondItem)
                assertEquals(CacheReason.UNKNOWN_ERROR, secondItem.cacheReason)
                assertNull(secondItem.data)
                assertNull(secondItem.message)

                awaitComplete()
            }
        }

    @Test
    fun `refreshAlbums emits Success when API call is successful and updates DB`() = runTest {
        // Arrange
        val albumsFromApi = listOf(
            AlbumDto(id = 1, title = "Album 1"),
            AlbumDto(id = 2, title = "Album 2")
        )
        val albumEntities = albumsFromApi.map { it.toEntity() }
        coEvery { leBonCoinDAO.hasAnyAlbum() } returns false
        coEvery { leBonCoinService.getAlbums() } returns Response.success(albumsFromApi)
        coEvery { leBonCoinDAO.getAllIds() } returns emptyList()
        coEvery { leBonCoinDAO.deleteByIds(any()) } returns Unit
        coEvery { leBonCoinDAO.upsertAllAlbums(any()) } returns Unit

        // Act & Assert
        repository.refreshAlbums().test {
            val item = awaitItem()
            assertIs<Resource.Success<Unit>>(item)
            assertNull(item.data)
            assertNull(item.message)

            awaitComplete()
        }

        // Verify DB operations
        coVerify { leBonCoinDAO.upsertAllAlbums(match { it.containsAll(albumEntities) }) }
        coVerify { leBonCoinDAO.deleteByIds(emptyList()) }
    }

    @Test
    fun `refreshAlbums emits CachedSuccess on empty API response when DB has albums`() = runTest {
        // Arrange
        coEvery { leBonCoinDAO.hasAnyAlbum() } returns true
        coEvery { leBonCoinService.getAlbums() } returns Response.success(null)

        // Act & Assert
        repository.refreshAlbums().test {
            val firstItem = awaitItem()
            assertIs<Resource.CachedSuccess<Unit>>(firstItem)
            assertEquals(CacheReason.FROM_DB, firstItem.cacheReason)
            assertNull(firstItem.data)
            assertNull(firstItem.message)

            val secondItem = awaitItem()
            assertIs<Resource.CachedSuccess<Unit>>(secondItem)
            assertEquals(CacheReason.EMPTY_BODY, secondItem.cacheReason)
            assertNull(secondItem.data)
            assertNull(secondItem.message)

            awaitComplete()
        }
    }

    @Test
    fun `refreshAlbums emits Error on empty API response when DB is empty`() = runTest {
        // Arrange
        coEvery { leBonCoinDAO.hasAnyAlbum() } returns false
        coEvery { leBonCoinService.getAlbums() } returns Response.success(null)

        // Act & Assert
        repository.refreshAlbums().test {
            val item = awaitItem()
            assertIs<Resource.Error<Unit>>(item)
            assertNull(item.data)
            assertNull(item.message)
            assertIs<RemoteException.EmptyBodyException>(item.exception)
            awaitComplete()
        }
    }

    @Test
    fun `refreshAlbums emits CachedSuccess on network error when DB has albums`() = runTest {
        // Arrange
        coEvery { leBonCoinDAO.hasAnyAlbum() } returns true
        coEvery { leBonCoinService.getAlbums() } returns Response.error(500, "".toResponseBody())

        // Act & Assert
        repository.refreshAlbums().test {
            val firstItem = awaitItem()
            assertIs<Resource.CachedSuccess<Unit>>(firstItem)
            assertEquals(CacheReason.FROM_DB, firstItem.cacheReason)
            assertNull(firstItem.data)
            assertNull(firstItem.message)

            val secondItem = awaitItem()
            assertIs<Resource.CachedSuccess<Unit>>(secondItem)
            assertEquals(CacheReason.NETWORK_ERROR, secondItem.cacheReason)
            assertNull(secondItem.data)
            assertNull(secondItem.message)
            awaitComplete()
        }
    }

    @Test
    fun `refreshAlbums emits Error on network error when DB is empty`() = runTest {
        // Arrange
        coEvery { leBonCoinDAO.hasAnyAlbum() } returns false
        coEvery { leBonCoinService.getAlbums() } returns Response.error(500, "".toResponseBody())

        // Act & Assert
        repository.refreshAlbums().test {
            val item = awaitItem()
            assertIs<Resource.Error<Unit>>(item)
            assertNull(item.data)
            assertIs<RemoteException.NetworkException>(item.exception)
            awaitComplete()
        }
    }

    @Test
    fun `refreshAlbums emits CachedSuccess on exception when DB has albums`() = runTest {
        // Arrange
        coEvery { leBonCoinDAO.hasAnyAlbum() } returns true
        coEvery { leBonCoinService.getAlbums() } throws RuntimeException("Unknown error")

        // Act & Assert
        repository.refreshAlbums().test {
            val firstItem = awaitItem()
            assertIs<Resource.CachedSuccess<Unit>>(firstItem)
            assertEquals(CacheReason.FROM_DB, firstItem.cacheReason)
            assertNull(firstItem.data)
            assertNull(firstItem.message)

            val secondItem = awaitItem()
            assertIs<Resource.CachedSuccess<Unit>>(secondItem)
            assertEquals(CacheReason.UNKNOWN_ERROR, secondItem.cacheReason)
            assertNull(secondItem.data)
            assertNull(secondItem.message)
            awaitComplete()
        }
    }

    @Test
    fun `refreshAlbums emits Error on exception when DB is empty`() = runTest {
        // Arrange
        coEvery { leBonCoinDAO.hasAnyAlbum() } returns false
        coEvery { leBonCoinService.getAlbums() } throws RuntimeException("Unknown error")

        // Act & Assert
        repository.refreshAlbums().test {
            val item = awaitItem()
            assertIs<Resource.Error<Unit>>(item)
            assertNull(item.data)
            assertEquals("Unknown error", item.message)
            assertIs<RemoteException.DefaultException>(item.exception)
            awaitComplete()
        }
    }

}
