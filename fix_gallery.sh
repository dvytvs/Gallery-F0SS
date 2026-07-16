sed -i '/val filterTitle = when (filterMode) {/,/else -> filterMode/d' app/src/main/java/com/example/ui/GalleryScreen.kt
sed -i '/}/!b;n;/Text(/!b;d' app/src/main/java/com/example/ui/GalleryScreen.kt
