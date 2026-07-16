sed -i 's/"Видео"/"Video"/g' app/src/main/java/com/example/ui/GalleryScreen.kt
sed -i 's/"Избранное"/"Favorites"/g' app/src/main/java/com/example/ui/GalleryScreen.kt
sed -i 's/"Корзина"/"Trash"/g' app/src/main/java/com/example/ui/GalleryScreen.kt
sed -i 's/"Последние"/"Recent"/g' app/src/main/java/com/example/ui/GalleryScreen.kt
sed -i 's/"Камера"/"Camera"/g' app/src/main/java/com/example/ui/GalleryScreen.kt
sed -i 's/"Скриншот"/"Screenshots"/g' app/src/main/java/com/example/ui/GalleryScreen.kt
sed -i 's/"Загрузки"/"Downloads"/g' app/src/main/java/com/example/ui/GalleryScreen.kt
sed -i 's/"В разработке"/context.getString(R.string.in_development)/g' app/src/main/java/com/example/ui/GalleryScreen.kt
sed -i 's/"Скрыть"/stringResource(R.string.hide)/g' app/src/main/java/com/example/ui/GalleryScreen.kt
sed -i 's/"Просмотреть все"/stringResource(R.string.view_all)/g' app/src/main/java/com/example/ui/GalleryScreen.kt
sed -i 's/Text("Основные альбомы"/Text(stringResource(R.string.main_albums)/g' app/src/main/java/com/example/ui/GalleryScreen.kt
