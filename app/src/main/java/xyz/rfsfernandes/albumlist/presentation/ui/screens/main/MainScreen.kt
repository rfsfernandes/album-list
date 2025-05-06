package xyz.rfsfernandes.albumlist.presentation.ui.screens.main

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.getKoin
import xyz.rfsfernandes.albumlist.R
import xyz.rfsfernandes.albumlist.presentation.model.AlbumDataModel
import xyz.rfsfernandes.albumlist.presentation.navigation.SnackbarController
import xyz.rfsfernandes.albumlist.presentation.ui.composables.AlbumCard
import xyz.rfsfernandes.albumlist.presentation.ui.composables.LoadingCircle

@Composable
fun MainScreen(
    vm: MainScreenViewModel = koinViewModel(),
    snackbarController: SnackbarController = getKoin().get()
) {
    LaunchedEffect(Unit) {
        vm.initialize()
    }

    val viewState = vm.viewState.collectAsState().value
    val hasNetworkConnection = viewState.hasNetworkConnection
    val pagedAlbums = viewState.albums.collectAsLazyPagingItems()

    if (hasNetworkConnection == false) {
        snackbarController.show(
            stringResource(R.string.lost_network_connection)
        )
    }
    if (viewState.errorMessage != null) {
        snackbarController.show(
            stringResource(viewState.errorMessage)
        )
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (pagedAlbums.itemCount == 0) {
            LoadingCircle()
        } else {
            AlbumItems(pagedAlbums = pagedAlbums)
        }
    }
}

@Composable
fun AlbumItems(
    pagedAlbums: LazyPagingItems<AlbumDataModel>,
    snackbarController: SnackbarController = getKoin().get()
) {
    when (pagedAlbums.loadState.refresh) {
        is LoadState.NotLoading -> {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) } // top
                items(
                    count = pagedAlbums.itemCount,
                    key = pagedAlbums.itemKey { it.id!! },
                    contentType = pagedAlbums.itemContentType { "Launch item" }
                ) { index ->
                    var isZoomed by remember { mutableStateOf(false) }
                    pagedAlbums[index]?.let {
                        AlbumCard(
                            album = it,
                            onClick = { isZoomed = true },
                            isZoomed = isZoomed,
                            onZoomDismiss = { isZoomed = false },
                            modifier = Modifier.fillMaxWidth()
                    ) }
                }

                item {
                    if (pagedAlbums.loadState.append is LoadState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(8.dp)) } // bottom
            }
        }

        is LoadState.Loading -> {
            LoadingCircle()
        }

        is LoadState.Error -> {
            val context = LocalContext.current
            snackbarController.show(
                context.getString(
                    R.string.error_fetching_albums,
                    (pagedAlbums.loadState.refresh as LoadState.Error).error.message
                )
            )
        }
    }
}
