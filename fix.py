import re

with open('app/src/main/java/com/example/ui/GalleryScreen.kt', 'r') as f:
    content = f.read()

# The sed replacement replaced 'Text(' with a block of text ending with 'Text('
# Let's find this specific block and replace it back to 'Text('

block = """val filterTitle = when (filterMode) {
                        "Trash" -> stringResource(R.string.filter_trash)
                        "Video" -> stringResource(R.string.filter_video)
                        "Favorites" -> stringResource(R.string.filter_favorites)
                        "Recent" -> stringResource(R.string.filter_recent)
                        "Camera" -> stringResource(R.string.filter_camera)
                        "Screenshots" -> stringResource(R.string.filter_screenshot)
                        "Downloads" -> stringResource(R.string.filter_downloads)
                        else -> filterMode
                    }
                    Text("""

content = content.replace(block, "Text(")
# There might be some indentation variations
content = re.sub(r' *val filterTitle = when \(filterMode\) \{[\s\S]*?else -> filterMode\n *\}\n *Text\(', 'Text(', content)
content = re.sub(r'android.widget.Toast.makeval filterTitle =.*?\n *\}\n *Text\(', 'android.widget.Toast.makeText(', content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/GalleryScreen.kt', 'w') as f:
    f.write(content)
