package com.example.ui

import android.Manifest
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.data.MediaItem
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(viewModel: GalleryViewModel) {
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val permissionState = rememberMultiplePermissionsState(permissions)
    val updateTrigger = remember { mutableStateOf(0) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, permissionState.allPermissionsGranted) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (permissionState.allPermissionsGranted) {
                    viewModel.loadMedia()
                    updateTrigger.value++
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            viewModel.loadMedia()
        }
    }

    if (!permissionState.allPermissionsGranted) {
        PermissionRequestScreen(permissionState)
        return
    }

    GalleryNavHost(viewModel, updateTrigger.value)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestScreen(permissionState: MultiplePermissionsState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.permission_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.permission_desc),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { permissionState.launchMultiplePermissionRequest() },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(stringResource(R.string.permission_allow))
            }
        }
    }
}

enum class GalleryTab(val titleResId: Int, val icon: ImageVector) {
    Pictures(R.string.tab_pictures, Icons.Default.Image),
    Albums(R.string.tab_albums, Icons.Default.PhotoAlbum),
    Collections(R.string.tab_collections, Icons.Default.Star),
    Menu(R.string.tab_menu, Icons.Default.Menu)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(viewModel: GalleryViewModel, updateTrigger: Int, onMediaClick: (Long) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("GalleryPrefs", android.content.Context.MODE_PRIVATE) }
    
    var selectedTab by remember {
        val saved = sharedPrefs.getString("selectedTab", GalleryTab.Pictures.name)
        mutableStateOf(GalleryTab.valueOf(saved ?: GalleryTab.Pictures.name))
    }
    
    LaunchedEffect(selectedTab) {
        sharedPrefs.edit().putString("selectedTab", selectedTab.name).apply()
    }

    var showMenuBottomSheet by remember { mutableStateOf(false) }
    var filterMode by remember { mutableStateOf<String?>(null) }

    androidx.activity.compose.BackHandler(enabled = filterMode != null || showMenuBottomSheet) {
        if (showMenuBottomSheet) {
            showMenuBottomSheet = false
        } else if (filterMode != null) {
            filterMode = null
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    val filterTitle = when (filterMode) {
                        "Trash" -> stringResource(R.string.filter_trash)
                        "Video" -> stringResource(R.string.filter_video)
                        "Favorites" -> stringResource(R.string.filter_favorites)
                        "Recent" -> stringResource(R.string.filter_recent)
                        "Camera" -> stringResource(R.string.filter_camera)
                        "Screenshots" -> stringResource(R.string.filter_screenshot)
                        "Downloads" -> stringResource(R.string.filter_downloads)
                        else -> filterMode
                    }
                    Text(
                        filterTitle ?: stringResource(selectedTab.titleResId), 
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 16.dp)
                    ) 
                },
                navigationIcon = {
                    if (filterMode != null) {
                        IconButton(onClick = { filterMode = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is GalleryState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is GalleryState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is GalleryState.Success -> {
                    Crossfade(
                        targetState = selectedTab,
                        label = "tab_crossfade",
                        animationSpec = tween(durationMillis = 300)
                    ) { tab ->
                        when (tab) {
                            GalleryTab.Pictures -> PicturesGrid(state.mediaByDate, state.allMediaCount, filterMode, updateTrigger, onMediaClick)
                            GalleryTab.Albums -> AlbumsGrid(state.albums, state.mediaByDate, updateTrigger) { albumName ->
                                filterMode = albumName
                                selectedTab = GalleryTab.Pictures
                            }
                            GalleryTab.Collections -> EmptyState(stringResource(R.string.empty_collections))
                            GalleryTab.Menu -> {
                                PicturesGrid(state.mediaByDate, state.allMediaCount, filterMode, updateTrigger, onMediaClick)
                            }
                        }
                    }
                }
            }
            
            if (filterMode == null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .wrapContentWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GalleryTab.values().forEach { tab ->
                            val isSelected = selectedTab == tab && tab != GalleryTab.Menu
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.surface.copy(alpha = 0.4f) else Color.Transparent)
                                    .clickable {
                                        if (tab == GalleryTab.Menu) {
                                            showMenuBottomSheet = true
                                        } else {
                                            selectedTab = tab
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val tabTitle = stringResource(tab.titleResId)
                                    Icon(tab.icon, contentDescription = tabTitle, modifier = Modifier.size(20.dp))
                                    Text(tabTitle, fontSize = 9.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
            
            if (showMenuBottomSheet) {
                MenuBottomSheet(
                    onDismiss = { showMenuBottomSheet = false },
                    onMenuItemClick = { titleResId ->
                        showMenuBottomSheet = false
                        when (titleResId) {
                            R.string.title_video, R.string.title_favorites -> {
                                // We need string representation for filterMode to match what PicturesGrid expects
                                filterMode = if (titleResId == R.string.title_video) "Video" else "Favorites"
                                selectedTab = GalleryTab.Pictures
                            }
                            R.string.title_trash -> {
                                filterMode = "Trash"
                                selectedTab = GalleryTab.Pictures
                            }
                            R.string.title_recent -> {
                                filterMode = null
                                selectedTab = GalleryTab.Pictures
                            }
                            R.string.title_settings -> {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = android.net.Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }
                            else -> android.widget.Toast.makeText(context, context.getString(R.string.in_development), android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PicturesGrid(mediaByDate: Map<String, List<MediaItem>>, totalCount: Int, filterMode: String?, updateTrigger: Int, onMediaClick: (Long) -> Unit) {
    if (totalCount == 0) {
        EmptyState(stringResource(R.string.empty_pictures))
        return
    }

    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("GalleryPrefs", android.content.Context.MODE_PRIVATE) }

    val filteredMediaByDate = remember(mediaByDate, filterMode, updateTrigger) {
        mediaByDate.mapValues { (_, items) ->
            items.filter { item ->
                when (filterMode) {
                    "Trash" -> item.isTrashed
                    "Video" -> !item.isTrashed && item.isVideo
                    "Favorites" -> !item.isTrashed && sharedPrefs.getBoolean("fav_${item.id}", false)
                    "Recent" -> !item.isTrashed
                    "Camera" -> !item.isTrashed && (item.albumName == "Camera" || item.dataPath.contains("DCIM/Camera", ignoreCase = true))
                    "Screenshots" -> !item.isTrashed && (item.albumName == "Screenshots" || item.dataPath.contains("Screenshots", ignoreCase = true))
                    "Downloads" -> !item.isTrashed && (item.albumName == "Download" || item.albumName == "Downloads" || item.dataPath.contains("Download", ignoreCase = true))
                    null -> !item.isTrashed
                    else -> !item.isTrashed && item.albumName == filterMode
                }
            }
        }.filterValues { it.isNotEmpty() }
    }

    if (filteredMediaByDate.isEmpty()) {
        EmptyState(stringResource(R.string.empty_search))
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        filteredMediaByDate.forEach { (date, items) ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                )
            }
            items(items, key = { it.id }) { item ->
                MediaItemCell(item = item, onClick = { onMediaClick(item.id) })
            }
        }
    }
}

@Composable
fun AlbumsGrid(
    albums: Map<String, List<MediaItem>>,
    mediaByDate: Map<String, List<MediaItem>>,
    updateTrigger: Int,
    onAlbumClick: (String) -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("GalleryPrefs", android.content.Context.MODE_PRIVATE) }
    
    var showAllAlbums by remember { mutableStateOf(false) }

    val allMedia = remember(mediaByDate, updateTrigger) {
        mediaByDate.values.flatten().filter { !it.isTrashed }
    }
    
    val favorites = remember(allMedia, updateTrigger) {
        allMedia.filter { sharedPrefs.getBoolean("fav_${it.id}", false) }
    }
    
    val customAlbums = remember(albums, allMedia, favorites) {
        val map = mutableMapOf<String, List<MediaItem>>()
        map["Recent"] = allMedia
        if (favorites.isNotEmpty()) {
            map["Favorites"] = favorites
        }
        val targetOrder = listOf("Camera", "Screenshots", "Downloads", "Download")

        val cameraItems = allMedia.filter { it.albumName == "Camera" || it.dataPath.contains("DCIM/Camera", ignoreCase = true) }
        if (cameraItems.isNotEmpty()) map["Camera"] = cameraItems

        val screenshotsItems = allMedia.filter { it.albumName == "Screenshots" || it.dataPath.contains("Screenshots", ignoreCase = true) }
        if (screenshotsItems.isNotEmpty()) map["Screenshots"] = screenshotsItems

        val downloadsItems = allMedia.filter { it.albumName == "Download" || it.albumName == "Downloads" || it.dataPath.contains("Download", ignoreCase = true) }
        if (downloadsItems.isNotEmpty()) map["Downloads"] = downloadsItems
        
        albums.keys.filter { it !in targetOrder }.forEach { key ->
            val items = albums[key]?.filter { !it.isTrashed }
            if (!items.isNullOrEmpty()) {
                map[key] = items
            }
        }
        map
    }

    val hasMoreAlbums = remember(customAlbums) {
        val layout = listOf("Recent", "Favorites", "Camera", "Screenshots", "Downloads")
        customAlbums.keys.any { it !in layout }
    }

    val displayKeys = remember(customAlbums, showAllAlbums) {
        if (showAllAlbums) {
            customAlbums.keys.toList()
        } else {
            val list = mutableListOf<String>()
            val layout = listOf("Recent", "Favorites", "Camera", "Screenshots", "Downloads")
            layout.forEach { name ->
                if (customAlbums.containsKey(name)) {
                    list.add(name)
                }
            }
            list
        }
    }

    if (customAlbums.isEmpty()) {
        EmptyState(stringResource(R.string.empty_albums))
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.main_albums), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (hasMoreAlbums) {
                Text(
                    text = if (showAllAlbums) stringResource(R.string.hide) else stringResource(R.string.view_all), 
                    color = MaterialTheme.colorScheme.primary, 
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { showAllAlbums = !showAllAlbums }
                )
            }
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(displayKeys) { albumName ->
                val albumItems = customAlbums[albumName] ?: emptyList()
                val thumbnail = albumItems.firstOrNull()
                
                Column(
                    modifier = Modifier.fillMaxWidth().testTag("album_card_$albumName"),
                    horizontalAlignment = Alignment.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onAlbumClick(albumName) }
                    ) {
                        if (thumbnail != null) {
                            if (thumbnail.isVideo) {
                                VideoThumbnail(uri = thumbnail.uri, modifier = Modifier.fillMaxSize())
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(thumbnail.uri)
                                        .size(coil.size.Size(256, 256))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Thumbnail for $albumName",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = albumName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = "${albumItems.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VideoThumbnail(uri: android.net.Uri, modifier: Modifier = Modifier) {
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    val context = LocalContext.current
    LaunchedEffect(uri) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    bitmap = context.contentResolver.loadThumbnail(uri, android.util.Size(256, 256), null)
                } else {
                    val retriever = android.media.MediaMetadataRetriever()
                    retriever.setDataSource(context, uri)
                    bitmap = retriever.getFrameAtTime(0)
                    retriever.release()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    if (bitmap != null) {
        androidx.compose.foundation.Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(modifier = modifier.background(Color.DarkGray))
    }
}

@Composable
fun MediaItemCell(item: MediaItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
    ) {
        if (item.isVideo) {
            VideoThumbnail(uri = item.uri, modifier = Modifier.fillMaxSize())
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.uri)
                    .size(coil.size.Size(256, 256))
                    .crossfade(true)
                    .build(),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (item.isVideo) {
            Icon(
                imageVector = Icons.Default.PlayCircleOutline,
                contentDescription = "Video",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(20.dp)
            )
        }
    }
}
