package xyz.rfsfernandes.albumlist.presentation.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import xyz.rfsfernandes.albumlist.presentation.model.AlbumDataModel

@Composable
fun AlbumCard(
    albumDataModel: AlbumDataModel
) {
    Card(Modifier.padding(horizontal = 16.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            AsyncImage(
                model = albumDataModel.thumbnailUrl,
                contentDescription = albumDataModel.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier.weight(0.3f)
            )
            Column(
                modifier = Modifier.weight(0.7f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                albumDataModel.title?.let {
                    Text(it)
                }
            }
        }
    }
}
