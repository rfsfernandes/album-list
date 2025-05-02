package xyz.rfsfernandes.albumlist.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import xyz.rfsfernandes.albumlist.presentation.navigation.NavigationGraph
import xyz.rfsfernandes.albumlist.presentation.ui.theme.AlbumListTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlbumListTheme {
                NavigationGraph()
            }
        }
    }
}
