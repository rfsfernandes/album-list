package xyz.rfsfernandes.albumlist.presentation.navigation

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * A controller class for managing and displaying snackbars.
 *
 * This class provides a way to queue and display snackbars using a [SnackbarHostState].
 * It ensures that only one snackbar is visible at a time by dismissing the current snackbar
 * before showing a new one. It utilizes a [Channel] to manage the snackbar queue and a
 * [CoroutineScope] to launch the snackbar dispatcher.
 *
 * @property snackbarHostState The [SnackbarHostState] used to display the snackbars.
 * @property coroutineScope The [CoroutineScope] used to launch the snackbar dispatcher.
 */
class SnackbarController(
    private val snackbarHostState: SnackbarHostState,
    private val coroutineScope: CoroutineScope
) {
    private val snackbarQueue = Channel<SnackbarData>(capacity = Channel.CONFLATED)

    init {
        startSnackbarDispatcher()
    }

    /**
     * Data class representing a Snackbar to be shown.
     *
     * @property message The text message to display in the Snackbar.
     * @property actionLabel The optional label for the action button. If null, no action button is shown.
     * @property duration The duration the Snackbar should be displayed.
     */
    fun show(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        snackbarQueue.trySend(SnackbarData(message, actionLabel, duration))
    }

    /**
     * Represents data for a Snackbar to be displayed.
     *
     * @property message The message to display in the Snackbar.
     * @property actionLabel The text for the optional action button in the Snackbar. Null if no action is needed.
     * @property duration The duration the Snackbar should be visible.
     */
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

    /**
     * Data class representing the information needed to display a Snackbar.
     *
     * @property message The text message to be displayed in the Snackbar. This is the main content
     *                   of the Snackbar and should convey the primary information to the user.
     * @property actionLabel An optional label for an action button associated with the Snackbar.
     *                       If provided, a button with this label will be displayed.
     *                       If null, no action button will be shown.
     * @property duration The duration for which the Snackbar will be visible. It defaults to
     *                    [SnackbarDuration.Short].  You can choose between [SnackbarDuration.Short],
     *                    [SnackbarDuration.Long], and [SnackbarDuration.Indefinite].
     */
    private data class SnackbarData(
        val message: String,
        val actionLabel: String?,
        val duration: SnackbarDuration = SnackbarDuration.Short
    )
}
