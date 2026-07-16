package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuBottomSheet(onDismiss: () -> Unit, onMenuItemClick: (Int) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        dragHandle = null
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MenuItem(titleResId = R.string.title_video, icon = Icons.Outlined.Videocam, onClick = { onMenuItemClick(R.string.title_video) })
                MenuItem(titleResId = R.string.title_favorites, icon = Icons.Outlined.FavoriteBorder, onClick = { onMenuItemClick(R.string.title_favorites) })
                MenuItem(titleResId = R.string.title_recent, icon = Icons.Outlined.Schedule, onClick = { onMenuItemClick(R.string.title_recent) })
                MenuItem(titleResId = R.string.title_settings, icon = Icons.Outlined.CameraAlt, onClick = { onMenuItemClick(R.string.title_settings) })
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MenuItem(titleResId = R.string.title_trash, icon = Icons.Outlined.Delete, onClick = { onMenuItemClick(R.string.title_trash) })
                MenuItem(titleResId = R.string.title_settings, icon = Icons.Outlined.Settings, onClick = { onMenuItemClick(R.string.title_settings) })
                Spacer(modifier = Modifier.weight(2f)) // Give spacer the rest
            }
        }
        }
    }
}

@Composable
fun RowScope.MenuItem(titleResId: Int, icon: ImageVector, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val title = stringResource(titleResId)
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2
        )
    }
}
