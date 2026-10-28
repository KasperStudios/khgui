# KHGui v1.0.0 - Release Notes

## 🎉 Initial Release

KHGui is now ready for production use! This is a complete GUI framework for Minecraft mods that replicates VS Code's interface using screenshot-based rendering.

## ✨ Features

### Core Functionality
- ✅ **Custom Primitive-Based Rendering** - Professional VS Code UI via graphics primitives, layouts, and themed panels
- ✅ **Multiloader Support** - Full parity between Fabric and NeoForge
- ✅ **Code Editor** - Complete text editing with syntax highlighting
- ✅ **Interactive UI** - Click handling, scrolling, keyboard shortcuts
- ✅ **Panel System** - Extensible sidebar panels (Explorer, Debug, Console)
- ✅ **Layout System** - Responsive layout with collapsible sidebar

### Text Editing
- ✅ Character input and typing
- ✅ Backspace and Delete
- ✅ Enter with auto-indentation
- ✅ Tab (4 spaces)
- ✅ Arrow key navigation
- ✅ Home/End keys
- ✅ Ctrl+A, C, V, X, Z, Y shortcuts (placeholders for future implementation)

### UI Components
- ✅ Title bar with dynamic text
- ✅ Activity bar with panel icons
- ✅ Collapsible sidebar
- ✅ Tab bar with multiple tabs
- ✅ Status bar with cursor position
- ✅ Line numbers with active line highlighting
- ✅ Blinking cursor

### Developer API
- ✅ `IEditorContext` - Provide editor data
- ✅ `IEditorPanel` - Create custom panels
- ✅ `IFileProvider` - File operations
- ✅ `IDebugProvider` - Debug information
- ✅ `IConsoleProvider` - Console messages
- ✅ `ICommandProvider` - Command palette
- ✅ `IScriptEnvironment` - Autocomplete data

### Technical
- ✅ Hit-map system for precise click detection
- ✅ Input handler with keyboard shortcuts
- ✅ Panel manager for sidebar panels
- ✅ Atlas system with 9-slice scaling
- ✅ Nearest-neighbor texture filtering (no blur)
- ✅ Scissor test for proper clipping

## 📦 What's Included

### Modules
- `common` - Shared code and API
- `fabric` - Fabric implementation
- `neoforge` - NeoForge implementation

### Default Panels
- 📁 **Explorer** - File tree panel (framework ready)
- 🐛 **Debug** - Debug variables panel (framework ready)
- 📟 **Console** - Console output panel (framework ready)

### Demo Mode
Press `H` in-game to open the demo editor with mock data.

## 🚀 Getting Started

### For Mod Developers

```gradle
dependencies {
    modImplementation "kasperstudios.khgui:khgui-fabric:1.0.0"
    // or
    modImplementation "kasperstudios.khgui:khgui-neoforge:1.0.0"
}
```

### Basic Usage

```java
IEditorContext context = new MyEditorContext();
Screen editor = KHGuiScreens.createCodeEditorScreen(context);
Minecraft.getInstance().setScreen(editor);
```

See [INTEGRATION_EXAMPLE.md](INTEGRATION_EXAMPLE.md) for complete integration guide.

## 📋 Known Limitations

These features are planned for future releases:

- Text selection with mouse/shift
- Clipboard operations (copy/paste/cut)
- Undo/redo system
- Autocomplete popup
- Theme switching
- Context menus
- Tab management (close, reorder)
- Split view
- Minimap
- Search and replace

## 🔧 Requirements

- Minecraft 1.21.1
- Java 21
- Fabric Loader 0.18.4+ or NeoForge 21.1.215+

## 📚 Documentation

- [README.md](README.md) - Overview and quick start
- [AI_GUIDE.md](AI_GUIDE.md) - Complete technical documentation
- [INTEGRATION_EXAMPLE.md](INTEGRATION_EXAMPLE.md) - Integration guide
- [devnotes.md](devnotes.md) - Development notes

## 🤝 Contributing

Contributions are welcome! The framework is designed to be extensible.

## 📄 License

MIT License - see [LICENSE](LICENSE) for details.

## 🙏 Credits

Created by KasperStudios for the KasHub scripting mod.

Inspired by VS Code's beautiful interface.

---

**Made for KasHub** - A powerful scripting engine for Minecraft
