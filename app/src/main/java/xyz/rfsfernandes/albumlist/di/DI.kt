package xyz.rfsfernandes.albumlist.di

import androidx.compose.material3.SnackbarHostState
import androidx.room.Room
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import xyz.rfsfernandes.albumlist.BuildConfig
import xyz.rfsfernandes.albumlist.data.local.AppDatabase
import xyz.rfsfernandes.albumlist.data.network.NetworkConnectivityObserver
import xyz.rfsfernandes.albumlist.data.network.NetworkManager
import xyz.rfsfernandes.albumlist.data.remote.RetrofitBuilder
import xyz.rfsfernandes.albumlist.data.repository.Repository
import xyz.rfsfernandes.albumlist.data.repository.RepositoryImpl
import xyz.rfsfernandes.albumlist.domain.usecases.GetAlbumListUseCase
import xyz.rfsfernandes.albumlist.domain.usecases.ObserveNetworkStateUseCase
import xyz.rfsfernandes.albumlist.domain.usecases.RefreshAlbumsUseCase
import xyz.rfsfernandes.albumlist.presentation.navigation.SnackbarController
import xyz.rfsfernandes.albumlist.presentation.ui.screens.main.MainScreenViewModel

object DI {

    private val dataModule = module {
        single { Moshi.Builder().build() }
        single {
            Room.databaseBuilder(
                androidContext(),
                AppDatabase::class.java,
                BuildConfig.DB_NAME
            ).build()
        }
        single { NetworkConnectivityObserver(androidContext()) }
        single { NetworkManager(get()) }
        single {
            RetrofitBuilder(
                androidContext(),
                BuildConfig.API_ENDPOINT,
                get(),
                get()
            ).leBonCoinService
        }
        single { get<AppDatabase>().leBonCoinDAO }
        single<Repository> { RepositoryImpl(get(), get()) }
    }

    private val domainModule = module {
        single { GetAlbumListUseCase(get()) }
        single { ObserveNetworkStateUseCase(get()) }
        single { RefreshAlbumsUseCase(get()) }
    }

    private val viewModelModule = module {
        viewModelOf(::MainScreenViewModel)
    }

    private val presentationModule = module {
        single { SnackbarHostState() }
        single<CoroutineScope> { CoroutineScope(Dispatchers.Main + SupervisorJob()) }
        single { SnackbarController(get(), get()) }
    }

    val modules = listOf(dataModule, domainModule, viewModelModule, presentationModule)
}