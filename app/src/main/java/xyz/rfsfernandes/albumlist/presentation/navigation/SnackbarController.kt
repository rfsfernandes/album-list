package xyz.rfsfernandes.albumlist.presentation.navigation

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class SnackbarController(
    private val snackbarHostState: SnackbarHostState,
    private val coroutineScope: CoroutineScope
) {
    private val snackbarQueue = Channel<SnackbarData>(capacity = Channel.CONFLATED)

    init {
        startSnackbarDispatcher()
    }

    fun show(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        snackbarQueue.trySend(SnackbarData(message, actionLabel, duration))
    }

    private fun startSnackbarDispatcher() {
        coroutineScope.launch {
            for (data in snackbarQueue) {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = data.message,
                    actionLabel = data.actionLabel,
                    duration = data.duration
                )
            }
        }
    }

    private data class SnackbarData(
        val message: String,
        val actionLabel: String?,
        val duration: SnackbarDuration = SnackbarDuration.Short
    )
}
