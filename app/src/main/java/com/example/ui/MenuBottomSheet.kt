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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuBottomSheet(onDismiss: () -> Unit, onMenuItemClick: (String) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
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
                MenuItem(title = "Видео", icon = Icons.Outlined.Videocam, onClick = { onMenuItemClick("Видео") })
                MenuItem(title = "Избранное", icon = Icons.Outlined.FavoriteBorder, onClick = { onMenuItemClick("Избранное") })
                MenuItem(title = "Последние", icon = Icons.Outlined.Schedule, onClick = { onMenuItemClick("Последние") })
                MenuItem(title = "Тип съемки", icon = Icons.Outlined.CameraAlt, onClick = { onMenuItemClick("Тип съемки") })
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MenuItem(title = "Личный альбом", icon = Icons.Outlined.Lock, onClick = { onMenuItemClick("Личный альбом") })
                MenuItem(title = "Корзина", icon = Icons.Outlined.Delete, onClick = { onMenuItemClick("Корзина") })
                MenuItem(title = "Настройки", icon = Icons.Outlined.Settings, onClick = { onMenuItemClick("Настройки") })
                // Empty spacer for the 4th column to maintain alignment
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun RowScope.MenuItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
