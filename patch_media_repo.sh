cat << 'INNER_EOF' > app/src/main/java/com/example/data/MediaRepository.kt
package com.example.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val dateAdded: Long,
    val isVideo: Boolean,
    val albumName: String?,
    val dataPath: String,
    val isTrashed: Boolean = false
)

class MediaRepository(private val context: Context) {
    suspend fun getMediaItems(): List<MediaItem> = withContext(Dispatchers.IO) {
        val mediaItems = mutableListOf<MediaItem>()
        
        val projection = mutableListOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            projection.add(MediaStore.MediaColumns.IS_TRASHED)
        }

        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

        val query = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bundle = android.os.Bundle().apply {
                putString(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                putStringArray(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
                putString(android.content.ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder)
                putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_TRASHED)
            }
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection.toTypedArray(),
                bundle,
                null
            )
        } else {
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection.toTypedArray(),
                selection,
                selectionArgs,
                sortOrder
            )
        }

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
            val mediaTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val isTrashedColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                cursor.getColumnIndex(MediaStore.MediaColumns.IS_TRASHED)
            } else -1

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Unknown"
                val dateAdded = cursor.getLong(dateAddedColumn)
                val type = cursor.getInt(mediaTypeColumn)
                val bucketName = cursor.getString(bucketNameColumn)
                val dataPath = cursor.getString(dataColumn) ?: ""
                
                val isTrashed = if (isTrashedColumn != -1) {
                    cursor.getInt(isTrashedColumn) == 1
                } else false

                val contentUri = ContentUris.withAppendedId(
                    if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) 
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI 
                    else 
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                mediaItems.add(
                    MediaItem(
                        id = id,
                        uri = contentUri,
                        name = name,
                        dateAdded = dateAdded * 1000L,
                        isVideo = type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                        albumName = bucketName,
                        dataPath = dataPath,
                        isTrashed = isTrashed
                    )
                )
            }
        }
        mediaItems
    }
}
INNER_EOF
