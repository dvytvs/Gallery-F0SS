import re

with open('app/src/main/java/com/example/ui/VideoPlayer.kt', 'r') as f:
    content = f.read()

replacement = """    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, isActive) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    exoPlayer.playWhenReady = false
                    exoPlayer.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (isActive) {
                        exoPlayer.playWhenReady = true
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        if (isActive && lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            exoPlayer.playWhenReady = true
        } else {
            exoPlayer.playWhenReady = false
            exoPlayer.pause()
        }
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }"""

content = re.sub(r'    LaunchedEffect\(isActive\) \{\s+if \(isActive\) \{\s+exoPlayer\.playWhenReady = true\s+\} else \{\s+exoPlayer\.playWhenReady = false\s+exoPlayer\.pause\(\)\s+\}\s+\}', replacement, content)

with open('app/src/main/java/com/example/ui/VideoPlayer.kt', 'w') as f:
    f.write(content)
