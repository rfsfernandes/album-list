package xyz.rfsfernandes.albumlist.presentation.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsProperties.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import coil.compose.AsyncImage
import coil.request.ImageRequest
import xyz.rfsfernandes.albumlist.R
import xyz.rfsfernandes.albumlist.presentation.model.AlbumDataModel

@Composable
fun AlbumCard(
    album: AlbumDataModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isZoomed: Boolean = false,
    onZoomDismiss: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .padding(horizontal = Dimens.CardHorizontalPadding)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClickLabel = stringResource(R.string.accessibility_open_album, album.title ?: "")
            ) { onClick() },
        shape = RoundedCornerShape(Dimens.CardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.CardElevation)
    ) {
        AlbumCardContent(album = album)
    }

    if (isZoomed) {
        ZoomedImageDialog(
            imageUrl = album.url,
            title = album.title,
            onDismiss = onZoomDismiss
        )
    }
}

@Composable
private fun AlbumCardContent(album: AlbumDataModel) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.ContentSpacing),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(Dimens.CardPadding)
            .testTag("AlbumCardContent")
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(album.thumbnailUrl)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(
                R.string.accessibility_album_thumbnail,
                album.title ?: "Unknown"
            ),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .weight(0.3f)
                .clip(RoundedCornerShape(Dimens.ImageCornerRadius))
                .testTag("AlbumThumbnail")
        )
        Column(
            modifier = Modifier
                .weight(0.7f)
                .testTag("AlbumDetails"),
            verticalArrangement = Arrangement.spacedBy(Dimens.ContentSpacing)
        ) {
            album.title?.let { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.testTag("AlbumTitle")
                )
            }
        }
    }
}

@Composable
private fun ZoomedImageDialog(
    imageUrl: String?,
    title: String?,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClickLabel = stringResource(R.string.accessibility_dismiss_zoom)
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                modifier = Modifier.padding(Dimens.CardPadding),
                visible = true,
                enter = scaleIn(animationSpec = tween(durationMillis = 300)) + fadeIn(),
                exit = scaleOut(animationSpec = tween(durationMillis = 300)) + fadeOut()
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(
                        R.string.accessibility_zoomed_image,
                        title ?: "Unknown"
                    ),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxHeight(0.6f)
                        .clip(RoundedCornerShape(Dimens.ImageCornerRadius))
                        .testTag("ZoomedImage")
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Dimens.ContentSpacing)
                    .testTag("DismissButton")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.accessibility_close_zoom),
                    tint = Color.White
                )
            }
        }
    }
}

// Constants for styling
object Dimens {
    val CardHorizontalPadding = 16.dp
    val CardPadding = 16.dp
    val CardCornerRadius = 8.dp
    val CardElevation = 4.dp
    val ContentSpacing = 16.dp
    val ImageCornerRadius = 8.dp
}
