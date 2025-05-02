package xyz.rfsfernandes.albumlist.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.compose.getKoin
import xyz.rfsfernandes.albumlist.presentation.navigation.routes.CoreNavigationRoutes
import xyz.rfsfernandes.albumlist.presentation.ui.screens.main.MainScreen

@Composable
fun NavigationGraph(
    snackbarHostState: SnackbarHostState = getKoin().get()
) {
    val navigationController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        NavHost(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            navController = navigationController,
            startDestination = CoreNavigationRoutes.MainScreen.route,
        ) {
            composable(CoreNavigationRoutes.MainScreen.route) {
                MainScreen()
            }
        }
    }
}
