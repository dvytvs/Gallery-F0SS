# Gallery

A local, lightweight, and private gallery application for Android. 

## Description
This project is a clean, native Android gallery built with Kotlin and Jetpack Compose. It focuses on privacy and performance, ensuring your media stays on your device. There is no analytics, no network tracking, and no AI integrations. 

## Features
* **Photo & Video Viewing:** Seamlessly view your local photos and watch videos.
* **Built-in ExoPlayer:** Smooth video playback with smart audio state retention (mutes/unmutes dynamically).
* **Zoom Gestures:** Support for double-tap to zoom and pinch-to-zoom gestures.
* **Trash Integration:** Fully integrates with Android 11+ system MediaStore Trash for safe deletion and recovery.
* **Full Localization:** Supports English, Russian, and Ukrainian system languages.

## Build Instructions
To build the release APK using Gradle, run the following command from the root of the project:
```bash
./gradlew clean assembleRelease
```

## License
This project is distributed under the [GPL-3.0 License](LICENSE).

---

# Галерея

Локальная, легковесная и приватная галерея для Android.

## Описание проекта
Этот проект представляет собой чистую нативную галерею для Android, написанную на Kotlin и Jetpack Compose. Основной упор сделан на приватность и производительность — все ваши медиафайлы остаются строго на устройстве. Приложение не содержит аналитики, сетевого отслеживания и модулей искусственного интеллекта.

## Основные возможности
* **Просмотр фото и видео:** Плавный и быстрый просмотр локальных медиафайлов.
* **Встроенный ExoPlayer:** Качественное воспроизведение видео с умным сохранением состояния звука.
* **Поддержка жестов масштабирования:** Двойной тап для приближения и pinch-to-zoom (щипок).
* **Интеграция с системной корзиной:** Полная поддержка системной корзины Android 11+ (MediaStore) для безопасного удаления.
* **Полная локализация:** Поддержка английского, русского и украинского языков системы.

## Инструкция по сборке
Для сборки релизного APK через Gradle выполните следующую команду в корне проекта:
```bash
./gradlew clean assembleRelease
```

## Информация о лицензии
Проект распространяется под свободной лицензией [GPL-3.0](LICENSE).
