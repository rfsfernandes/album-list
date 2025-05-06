package xyz.rfsfernandes.albumlist.domain.usecases

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import xyz.rfsfernandes.albumlist.data.local.LeBonCoinDAO
import xyz.rfsfernandes.albumlist.data.local.entities.AlbumEntity
import xyz.rfsfernandes.albumlist.domain.mappers.toDomain
import xyz.rfsfernandes.albumlist.domain.model.Album
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class GetAlbumListUseCaseTest {

    // Mocked DAO and the use case under test
    private val dao: LeBonCoinDAO = mockk()
    private lateinit var getAlbumListUseCase: GetAlbumListUseCase

    // Define test coroutine dispatchers (for AsyncPagingDataDiffer)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher) // Use test dispatcher as Main
        getAlbumListUseCase = GetAlbumListUseCase(dao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Helper: Fake PagingSource that returns a predefined list
    class FakeAlbumPagingSource(
        private val albums: List<AlbumEntity>
    ) : PagingSource<Int, AlbumEntity>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AlbumEntity> {
            return LoadResult.Page(
                data = albums,
                prevKey = null,
                nextKey = null
            )
        }

        override fun getRefreshKey(state: PagingState<Int, AlbumEntity>): Int? = null
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `flow emits PagingData with expected domain albums`() = runTest {
        // Arrange
        val dbAlbums = listOf(
            AlbumEntity(id = 1, title = "Album One"),
            AlbumEntity(id = 2, title = "Album Two")
        )
        val expectedDomainAlbums = dbAlbums.map { it.toDomain() }

        every { dao.albumsPagingSource() } returns FakeAlbumPagingSource(dbAlbums)

        val flow = getAlbumListUseCase()

        val differ = AsyncPagingDataDiffer(
            diffCallback = ALBUM_ITEM_CALLBACK,
            updateCallback = NoopListCallback(),
            mainDispatcher = testDispatcher,
            workerDispatcher = testDispatcher
        )

        val job = launch {
            flow.collectLatest { pagingData ->
                differ.submitData(pagingData)
            }
        }

        advanceUntilIdle()

        assertEquals(expectedDomainAlbums, differ.snapshot().items)

        job.cancel()
    }

    @Test
    fun `toDomain maps AlbumEntity to Album correctly`() {
        val entity = AlbumEntity(id = 42, title = "Test Title", url = "http://example.com/img.png")
        val domain = entity.toDomain()
        // Verify each field is mapped properly
        assertEquals(entity.id, domain.id)
        assertEquals(entity.title, domain.title)
        assertEquals(entity.url, domain.url)
    }
}

// DiffUtil callback for Album objects (assuming Album defines equality by id or content)
private val ALBUM_ITEM_CALLBACK = object : DiffUtil.ItemCallback<Album>() {
    override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
        return oldItem.id == newItem.id  // compare unique IDs
    }

    override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
        return oldItem == newItem       // if Album is data class, this checks all fields
    }
}

// No-op ListUpdateCallback (does nothing on changes, used by AsyncPagingDataDiffer)
private class NoopListCallback : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) = Unit
    override fun onRemoved(position: Int, count: Int) = Unit
    override fun onMoved(fromPosition: Int, toPosition: Int) = Unit
    override fun onChanged(position: Int, count: Int, payload: Any?) = Unit
}