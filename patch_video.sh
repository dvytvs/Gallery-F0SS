sed -i -e '40,51d' \
-e '94,103c\            .pointerInput(Unit) {\n                detectTapGestures(\n                    onTap = { onSingleTap() }\n                )\n            }' \
-e '109a\                    setOnTouchListener { _, _ -> false }\n                    isClickable = false\n                    isFocusable = false' \
-e '112,117d' \
app/src/main/java/com/example/ui/VideoPlayer.kt
