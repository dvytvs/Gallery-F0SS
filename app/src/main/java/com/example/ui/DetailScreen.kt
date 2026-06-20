package com.example.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.MediaItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    mediaItem: MediaItem?,
    onNavigateBack: () -> Unit
) {
    if (mediaItem == null) return

    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("GalleryPrefs", android.content.Context.MODE_PRIVATE)
    var isFavorite by remember { mutableStateOf(sharedPrefs.getBoolean("fav_${mediaItem.id}", false)) }
    val isTrashed = remember { mutableStateOf(sharedPrefs.getBoolean("trash_${mediaItem.id}", false)) }
    var showInfoDialog by remember { mutableStateOf(false) }

    val cropImageLauncher = rememberLauncherForActivityResult(com.canhub.cropper.CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent = result.uriContent
            if (uriContent != null) {
                try {
                    val resolver = context.contentResolver
                    val contentValues = android.content.ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "Edited_Image_${System.currentTimeMillis()}.jpg")
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.Images.Media.IS_PENDING, 1)
                        }
                    }
                    val destUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    if (destUri != null) {
                        resolver.openInputStream(uriContent)?.use { input ->
                            resolver.openOutputStream(destUri)?.use { output ->
                                input.copyTo(output)
                            }
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            contentValues.clear()
                            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                            resolver.update(destUri, contentValues, null, null)
                        }
                        Toast.makeText(context, "Изображение сохранено", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            val exception = result.error
            if (exception != null) {
                Toast.makeText(context, "Ошибка редактирования: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "Файл удален", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF2E2E2E).copy(alpha = 0.9f))
                    .padding(vertical = 4.dp, horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isTrashed.value) {
                        IconButton(onClick = { 
                            sharedPrefs.edit().putBoolean("trash_${mediaItem.id}", false).apply()
                            isTrashed.value = false
                            Toast.makeText(context, "Восстановлено из корзины", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }) {
                            Icon(Icons.Default.RestoreFromTrash, contentDescription = "Restore", tint = Color.White)
                        }
                        IconButton(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                val deleteRequest = MediaStore.createDeleteRequest(context.contentResolver, listOf(mediaItem.uri))
                                deleteLauncher.launch(IntentSenderRequest.Builder(deleteRequest.intentSender).build())
                            } else {
                                try {
                                    context.contentResolver.delete(mediaItem.uri, null, null)
                                    Toast.makeText(context, "Файл удален навсегда", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                } catch (e: SecurityException) {
                                    Toast.makeText(context, "Невозможно удалить: недостаточно прав", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Delete Forever", tint = Color.Red)
                        }
                    } else {
                        IconButton(onClick = { 
                            isFavorite = !isFavorite
                            sharedPrefs.edit().putBoolean("fav_${mediaItem.id}", isFavorite).apply()
                        }) {
                            Icon(
                                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color.Red else Color.White
                            )
                        }
                        IconButton(onClick = {
                            if (mediaItem.isVideo) {
                                Toast.makeText(context, "Редактирование видео пока недоступно", Toast.LENGTH_SHORT).show()
                            } else {
                                cropImageLauncher.launch(
                                    com.canhub.cropper.CropImageContractOptions(
                                        uri = mediaItem.uri,
                                        cropImageOptions = com.canhub.cropper.CropImageOptions(
                                            imageSourceIncludeGallery = false,
                                            imageSourceIncludeCamera = false
                                        )
                                    )
                                )
                            }
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                        }
                        IconButton(onClick = { showInfoDialog = true }) {
                            Icon(Icons.Outlined.Info, contentDescription = "Info", tint = Color.White)
                        }
                        IconButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = if (mediaItem.isVideo) "video/*" else "image/*"
                                putExtra(Intent.EXTRA_STREAM, mediaItem.uri)
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Поделиться"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                        }
                        IconButton(onClick = {
                            sharedPrefs.edit().putBoolean("trash_${mediaItem.id}", true).apply()
                            isTrashed.value = true
                            Toast.makeText(context, "Перемещено в корзину", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Trash", tint = Color.White)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(mediaItem.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = mediaItem.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            
            if (showInfoDialog) {
                AlertDialog(
                    onDismissRequest = { showInfoDialog = false },
                    title = { Text("Сведения") },
                    text = {
                        Column {
                            Text("Имя: ${mediaItem.name}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Альбом: ${mediaItem.albumName ?: "Неизвестно"}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            val dateStr = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(mediaItem.dateAdded))
                            Text("Дата: $dateStr", style = MaterialTheme.typography.bodyMedium)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showInfoDialog = false }) {
                            Text("ОК")
                        }
                    }
                )
            }
        }
    }
}
