sed -i 's/"Изображение сохранено"/context.getString(R.string.toast_image_saved)/g' app/src/main/java/com/example/ui/DetailScreen.kt
sed -i 's/"Ошибка сохранения"/context.getString(R.string.toast_save_error)/g' app/src/main/java/com/example/ui/DetailScreen.kt
sed -i 's/"Файл удален"/context.getString(R.string.toast_file_deleted)/g' app/src/main/java/com/example/ui/DetailScreen.kt
sed -i 's/"Поворот экрана"/context.getString(R.string.toast_rotate_screen)/g' app/src/main/java/com/example/ui/DetailScreen.kt
sed -i 's/"Открыть в видеоплеере"/context.getString(R.string.open_in_video_player)/g' app/src/main/java/com/example/ui/DetailScreen.kt
sed -i 's/"В разработке"/context.getString(R.string.in_development)/g' app/src/main/java/com/example/ui/DetailScreen.kt
sed -i 's/"Установить как обои"/context.getString(R.string.set_as_wallpaper)/g' app/src/main/java/com/example/ui/DetailScreen.kt
sed -i 's/"Нет подходящего приложения"/context.getString(R.string.no_suitable_app)/g' app/src/main/java/com/example/ui/DetailScreen.kt
sed -i 's/"Редактирование видео недоступно"/context.getString(R.string.video_edit_unavailable)/g' app/src/main/java/com/example/ui/DetailScreen.kt
sed -i 's/"Поделиться"/context.getString(R.string.share)/g' app/src/main/java/com/example/ui/DetailScreen.kt
sed -i 's/"Ошибка перемещения в корзину"/context.getString(R.string.error_move_trash)/g' app/src/main/java/com/example/ui/DetailScreen.kt
sed -i 's/"Невозможно удалить файл"/context.getString(R.string.cannot_delete_file)/g' app/src/main/java/com/example/ui/DetailScreen.kt
sed -i 's/"Сведения"/context.getString(R.string.details)/g' app/src/main/java/com/example/ui/DetailScreen.kt
sed -i 's/"Имя: ${currentMediaItem.name}"/"${context.getString(R.string.name)} ${currentMediaItem.name}"/g' app/src/main/java/com/example/ui/DetailScreen.kt
sed -i 's/"Альбом: ${currentMediaItem.albumName ?: \\"Неизвестно\\"}"/"${context.getString(R.string.album)} ${currentMediaItem.albumName ?: context.getString(R.string.unknown)}"/g' app/src/main/java/com/example/ui/DetailScreen.kt
sed -i 's/"Дата: $dateStr"/"${context.getString(R.string.date)} $dateStr"/g' app/src/main/java/com/example/ui/DetailScreen.kt
sed -i 's/"ОК"/context.getString(R.string.ok)/g' app/src/main/java/com/example/ui/DetailScreen.kt
