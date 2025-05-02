package xyz.rfsfernandes.albumlist.presentation.ui.screens.main

import androidx.annotation.StringRes
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import xyz.rfsfernandes.albumlist.presentation.model.AlbumDataModel

data class MainScreenViewState(
    val hasNetworkConnection: Boolean = false,
    @StringRes val errorMessage: Int? = null,
    val albums: Flow<PagingData<AlbumDataModel>> = emptyFlow()
)
