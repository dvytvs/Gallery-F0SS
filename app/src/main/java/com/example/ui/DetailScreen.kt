package com.example.ui

import android.app.Activity
import android.content.Intent
import com.example.R
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectVerticalDragGestures

import androidx.compose.ui.input.pointer.positionChanged

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.calculatePan

import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.data.MediaItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    initialMediaId: Long,
    viewModel: GalleryViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val mediaItems = remember(uiState) {
        if (uiState is GalleryState.Success) {
            (uiState as GalleryState.Success).mediaByDate.values.flatten()
        } else {
            emptyList()
        }
    }

    if (mediaItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val initialPage = remember(mediaItems, initialMediaId) {
        val index = mediaItems.indexOfFirst { it.id == initialMediaId }
        if (index >= 0) index else 0
    }

    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { mediaItems.size })
    val currentMediaItem = mediaItems.getOrNull(pagerState.currentPage)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sharedPrefs = context.getSharedPreferences("GalleryPrefs", android.content.Context.MODE_PRIVATE)

    var showMenu by remember { mutableStateOf(false) }

    if (currentMediaItem == null) return

    val isFavorite = remember(currentMediaItem) { mutableStateOf(sharedPrefs.getBoolean("fav_${currentMediaItem.id}", false)) }
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
                        Toast.makeText(context, context.getString(R.string.toast_image_saved), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, context.getString(R.string.toast_save_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, context.getString(R.string.toast_file_deleted), Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    var isFullScreen by remember { mutableStateOf(false) }
    var isZoomed by remember { mutableStateOf(false) }
    var isGlobalMuted by remember { mutableStateOf(true) }

    val dragOffsetY = remember { androidx.compose.animation.core.Animatable(0f) }
    val alpha = (1f - (Math.abs(dragOffsetY.value) / 1000f)).coerceIn(0f, 1f)

    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = alpha))) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = !isZoomed
        ) { page ->
            val item = mediaItems[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { androidx.compose.ui.unit.IntOffset(0, if (page == pagerState.currentPage) dragOffsetY.value.toInt() else 0) }
                    .pointerInput(isZoomed) {
                        if (!isZoomed) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    if (dragOffsetY.value > 300f) {
                                        onNavigateBack()
                                    } else {
                                        coroutineScope.launch { dragOffsetY.animateTo(0f) }
                                    }
                                },
                                onDragCancel = {
                                    coroutineScope.launch { dragOffsetY.animateTo(0f) }
                                },
                                onVerticalDrag = { change, dragAmount ->
                                    if (dragOffsetY.value + dragAmount > 0) {
                                        coroutineScope.launch { dragOffsetY.snapTo(dragOffsetY.value + dragAmount) }
                                        change.consume()
                                    }
                                }
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (item.isVideo) {
                    CustomVideoPlayer(
                        uri = item.uri, 
                        onSingleTap = { isFullScreen = !isFullScreen }, 
                        onZoomChanged = { isZoomed = it }, 
                        showOverlay = !isFullScreen,
                        isActive = page == pagerState.currentPage,
                        isMuted = isGlobalMuted,
                        onMuteChanged = { isGlobalMuted = it }
                    )
                } else {
                    ZoomableImage(uri = item.uri, onSingleTap = { isFullScreen = !isFullScreen }, onZoomChanged = { isZoomed = it })
                }
            }
        }

        // Единый слой UI
        androidx.compose.animation.AnimatedVisibility(
            visible = !isFullScreen,
            modifier = Modifier.fillMaxSize(),
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                        ))
                ) {
                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        },
                        actions = {
                            if (currentMediaItem.isVideo) {
                                IconButton(onClick = { Toast.makeText(context, "Smart View", Toast.LENGTH_SHORT).show() }) {
                                    Icon(Icons.Outlined.Cast, contentDescription = "Cast", tint = Color.White)
                                }
                                IconButton(onClick = { Toast.makeText(context, context.getString(R.string.toast_rotate_screen), Toast.LENGTH_SHORT).show() }) {
                                    Icon(Icons.Outlined.ScreenRotation, contentDescription = "Rotate", tint = Color.White)
                                }
                                Box {
                                    IconButton(onClick = { showMenu = true }) {
                                        Icon(Icons.Outlined.MoreVert, contentDescription = "More", tint = Color.White)
                                    }
                                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                        DropdownMenuItem(
                                            text = { Text(context.getString(R.string.open_in_video_player)) },
                                            onClick = { 
                                                showMenu = false
                                                Toast.makeText(context, context.getString(R.string.in_development), Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(context.getString(R.string.set_as_wallpaper)) },
                                            onClick = { 
                                                showMenu = false
                                                val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
                                                    setDataAndType(currentMediaItem.uri, "video/*")
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                try {
                                                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.set_as_wallpaper)))
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, context.getString(R.string.no_suitable_app), Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent
                        )
                    )
                }

                // Bottom Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        ))
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                ) {
                    // Thumbnail Strip
                val listState = rememberLazyListState()
                LaunchedEffect(pagerState.currentPage) {
                    listState.animateScrollToItem(pagerState.currentPage.coerceAtLeast(0))
                }
                LazyRow(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(mediaItems) { index, item ->
                        val isSelected = index == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 56.dp else 48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .then(if (isSelected) Modifier.border(1.5.dp, Color.White, RoundedCornerShape(8.dp)) else Modifier)
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        ) {
                            if (item.isVideo) {
                                VideoThumbnail(uri = item.uri, modifier = Modifier.fillMaxSize())
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(item.uri)
                                        .size(coil.size.Size(150, 150))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Thumbnail",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
                
                // Existing bottom action bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF2E2E2E).copy(alpha = 0.9f))
                        .padding(vertical = 4.dp, horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(modifier = Modifier.weight(1f), onClick = { 
                            val newVal = !isFavorite.value
                            isFavorite.value = newVal
                            sharedPrefs.edit().putBoolean("fav_${currentMediaItem.id}", newVal).apply()
                        }) {
                            Icon(
                                if (isFavorite.value) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite.value) Color.Red else Color.White
                            )
                        }
                        IconButton(modifier = Modifier.weight(1f), onClick = {
                            if (currentMediaItem.isVideo) {
                                Toast.makeText(context, context.getString(R.string.video_edit_unavailable), Toast.LENGTH_SHORT).show()
                            } else {
                                cropImageLauncher.launch(
                                    com.canhub.cropper.CropImageContractOptions(
                                        uri = currentMediaItem.uri,
                                        cropImageOptions = com.canhub.cropper.CropImageOptions(
                                            imageSourceIncludeGallery = false,
                                            imageSourceIncludeCamera = false
                                        )
                                    )
                                )
                            }
                        }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = Color.White)
                        }
                        IconButton(modifier = Modifier.weight(1f), onClick = { showInfoDialog = true }) {
                            Icon(Icons.Outlined.Info, contentDescription = "Info", tint = Color.White)
                        }
                        IconButton(modifier = Modifier.weight(1f), onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = if (currentMediaItem.isVideo) "video/*" else "image/*"
                                putExtra(Intent.EXTRA_STREAM, currentMediaItem.uri)
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }
                            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share)))
                        }) {
                            Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Color.White)
                        }
                        IconButton(modifier = Modifier.weight(1f), onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                try {
                                    val trashRequest = MediaStore.createTrashRequest(context.contentResolver, listOf(currentMediaItem.uri), true)
                                    deleteLauncher.launch(IntentSenderRequest.Builder(trashRequest.intentSender).build())
                                } catch (e: Exception) {
                                    Toast.makeText(context, context.getString(R.string.error_move_trash), Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                try {
                                    context.contentResolver.delete(currentMediaItem.uri, null, null)
                                    Toast.makeText(context, context.getString(R.string.toast_file_deleted), Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                } catch (e: SecurityException) {
                                    Toast.makeText(context, context.getString(R.string.cannot_delete_file), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Trash", tint = Color.White)
                        }
                    }
                }
            }
        }
    } // Closes Box inside AnimatedVisibility
} // Closes AnimatedVisibility

if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = { Text(context.getString(R.string.details)) },
                text = {
                    Column {
                        Text("${context.getString(R.string.name)} ${currentMediaItem.name}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${context.getString(R.string.album)} ${currentMediaItem.albumName ?: context.getString(R.string.unknown)}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        val dateStr = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(currentMediaItem.dateAdded))
                        Text("${context.getString(R.string.date)} $dateStr", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showInfoDialog = false }) {
                        Text(context.getString(R.string.ok))
                    }
                }
            )
        }
    }

@Composable
fun ZoomableImage(uri: android.net.Uri, onSingleTap: () -> Unit, onZoomChanged: (Boolean) -> Unit = {}) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    
    LaunchedEffect(uri) {
        scale = 1f
        offset = androidx.compose.ui.geometry.Offset.Zero
        onZoomChanged(false)
    }
    
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(uri)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onSingleTap() },
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = androidx.compose.ui.geometry.Offset.Zero
                        } else {
                            scale = 2.5f
                            offset = androidx.compose.ui.geometry.Offset.Zero
                        }
                        onZoomChanged(scale > 1.05f)
                    }
                )
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    do {
                        val event = awaitPointerEvent()
                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()
                        
                        if (zoom != 1f || pan != androidx.compose.ui.geometry.Offset.Zero) {
                            val newScale = (scale * zoom).coerceIn(1f, 5f)
                            if (newScale > 1.05f) {
                                scale = newScale
                                offset += pan
                                // Consume only if we are zoomed in to prevent pager from swiping
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                            } else {
                                scale = 1f
                                offset = androidx.compose.ui.geometry.Offset.Zero
                            }
                            onZoomChanged(scale > 1.05f)
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
    )
}
