package xyz.rfsfernandes.albumlist.presentation.navigation.routes

sealed class CoreNavigationRoutes(val route: String) {
    data object MainScreen : CoreNavigationRoutes(route = "home_screen")
}
