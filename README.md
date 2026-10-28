# KHGui - Screenshot-Based GUI Framework for Minecraft

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green.svg)](https://minecraft.net)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**KHGui** is a modern GUI framework for Minecraft mods that replicates VS Code's interface using screenshot-based rendering. Built for the [KasHub](https://github.com/KasperStudios/Kashub) scripting mod.

## ✨ Features

- 🎨 **Screenshot-Based Rendering** - Pixel-perfect VS Code UI from texture atlas
- 🔧 **Multiloader Support** - Works on both Fabric and NeoForge
- ⌨️ **Full Text Editing** - Complete code editor with syntax highlighting
- 🖱️ **Interactive UI** - Click handling, scrolling, and keyboard shortcuts
- 🎯 **Extensible API** - Easy integration for other mods
- 📦 **Library, Not App** - Provides framework, you provide data

## 🚀 Quick Start

### For Mod Developers (Libary Embedding)

KHGui is designed to be embedded in your mod using "Jar-in-Jar" (Fabric) or shadowing techniques.

#### Fabric (JiJ)

Add the jar to your `libs` folder and configure `build.gradle`:

```gradle
dependencies {
    // Include the library as a dependency
    implementation files("libs/khgui-fabric-1.0.0.jar")
    // Bundle it inside your mod jar
    include files("libs/khgui-fabric-1.0.0.jar")
}
```

#### NeoForge (Shadowing)

Use the Shadow plugin to bundle KHGui:

```gradle
dependencies {
    implementation files("libs/khgui-neoforge-1.0.0.jar")
}

shadowJar {
    relocate 'kasperstudios.khgui', 'yourmod.embedded.khgui'
}
```

### Basic Usage

```java
import kasperstudios.khgui.KHGuiScreens;
import kasperstudios.khgui.api.IEditorContext;
import net.minecraft.client.Minecraft;

// Create your custom context (implements IEditorContext)
IEditorContext context = new YourModEditorContext();

// Open the editor
Screen editor = KHGuiScreens.createCodeEditorScreen(context);
Minecraft.getInstance().setScreen(editor);
```

## 📚 API Overview

### IEditorContext

Main interface for providing editor data:

```java
public interface IEditorContext {
    String getTitleText();           // "MyMod - script.lua"
    List<String> getOpenTabs();      // ["script.lua", "config.json"]
    int getActiveTabIndex();         // 0
    String getSidebarTitle();        // "EXPLORER"
    String getStatusBarText();       // "Ln 1, Col 1  UTF-8  Lua"
    String getLanguage();            // "Lua"
}
```

### IEditorPanel

Create custom sidebar panels:

```java
public interface IEditorPanel {
    String getId();                  // "explorer"
    String getDisplayName();         // "EXPLORER"
    String getIcon();                // "📁"
    void onOpen();                   // Called when panel opens
}
```

### Other Interfaces

- `IFileProvider` - File tree and file operations
- `IDebugProvider` - Debug information and variables
- `IConsoleProvider` - Console messages
- `ICommandProvider` - Command palette
- `IScriptEnvironment` - Autocomplete data

## 🎨 How It Works

1. **Screenshot** - Take clean VS Code screenshot
2. **Slice** - Cut into regions (title bar, sidebar, etc.)
3. **Atlas** - Pack into texture atlas with JSON mapping
4. **Runtime** - Blit regions based on layout
5. **Overlay** - Render text, cursor, selection on top

## 🛠️ Building

```bash
# Build all platforms
./gradlew build

# Run Fabric client
./gradlew :fabric:runClient

# Run NeoForge client
./gradlew :neoforge:runClient
```

## 📖 Documentation

- [AI Guide](AI_GUIDE.md) - Complete technical documentation
- [Dev Notes](devnotes.md) - Development status and notes
- [Examples](examples/) - Integration examples (coming soon)

## 🎯 Current Status

### ✅ Implemented
- Screenshot-based rendering with atlas system
- Layout system (activity bar, sidebar, editor, status bar)
- Code display with line numbers and syntax highlighting
- Text input and editing
- Keyboard shortcuts (arrows, home, end, backspace, delete, etc.)
- Mouse interaction (click handling, scrolling)
- Hit-map system for UI zones
- Full Fabric and NeoForge support

### 🚧 In Progress
- Panel system (file tree, debug, console)
- Advanced text selection
- Clipboard operations (copy/paste)
- Undo/redo system
- Theme system
- Command palette

## 🤝 Contributing

Contributions welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) first.

## 📄 License

MIT License - see [LICENSE](LICENSE) for details.

## 🔗 Links

- [KasHub Mod](https://github.com/KasperStudios/Kashub) - Main scripting mod using KHGui
- [Issues](https://github.com/KasperStudios/KHGui/issues) - Bug reports and feature requests
- [Discord](https://discord.gg/your-server) - Community support

## 💡 Credits

Created by KasperStudios for the KasHub project.

Inspired by VS Code's beautiful interface.
