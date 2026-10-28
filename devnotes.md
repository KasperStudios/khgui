# 📋 KHGui Development Notes

## 🎯 Project Overview

**KHGui** is a custom-rendered GUI framework for Minecraft mods that replicates the VS Code interface.

**Core Concept:** KHGui uses custom rendering primitives (rectangles, borders, text, icons) to replicate the original VS Code UI reference, matching its visual style without relying on static screenshot atlases.

**Target platforms:** Fabric, NeoForge (via Architectury)  
**Minecraft:** 1.21+  
**Java:** 21+

**Made for:** [KasHub mod](https://github.com/KasperStudios/Kashub) - located in `../kashub`

---

## 🏗️ Architecture

### Primitive-Based Rendering Approach

1. **Primitive Layer** - Custom rendering logic for rectangles, borders, text, and standalone icons
2. **Layout Layer** - Defines panel positions and sizes
3. **Hit-Map Layer** - Maps clicks to interactive zones
4. **Overlay Layer** - Text, cursor, selection rendered over UI panels

### Dependency Direction

```
KasHub (mod) → depends on → KHGui (library)
     ↓
Implements interfaces:
  - IEditorContext (title, tabs, status text, code completions)
  - IFileProvider (file tree)
  - IDebugProvider (debug data)
  - IConsoleProvider (console output)
  - IEditorPanel (custom panels)
```

---

## 🔌 API & Initialization

### IEditorContext

The host mod must provide an implementation of `IEditorContext` to populate the editor with data.

```java
public interface IEditorContext {
    String getTitleText();           // "KasHub - script.lua"
    List<String> getOpenTabs();      // ["script.lua", "config.json"]
    int getActiveTabIndex();         // 0
    String getStatusBarText();       // "Ln 1, Col 1  UTF-8  Lua"
    Map<String, String> getEnvironmentVariables(); // For autocomplete
    List<String> getUserVariables();               // For autocomplete
    List<String> getObjectMembers(String objectName); // Contextual autocomplete
}
```

### Library Initialization

Consuming mods should initialize the library managers during mod startup (client-side).

```java
// Set custom path for drafts (optional, default is config/khgui/drafts)
DraftManager.initialize(Paths.get("config", "yourmod", "drafts"));

// Open the editor
IEditorContext context = new YourModEditorContext();
Screen editor = KHGuiScreens.createCodeEditorScreen(context);
Minecraft.getInstance().setScreen(editor);
```

---

## 📐 Layout & Themes

### Layout Constants
- `TITLE_BAR_HEIGHT`: 30px
- `ACTIVITY_BAR_WIDTH`: 40px
- `SIDEBAR_WIDTH`: Dynamic (170-400px)
- `STATUS_BAR_HEIGHT`: 22px

### Theme System
KHGui uses a theme system with `ITheme` interface. The default is **CyberPurpleTheme** (Catppuccin Mocha palette).
No colors should be hardcoded in rendering logic; always use `ThemeManager.getCurrentTheme()`.

---

## 🚨 Critical Rules (For AI Context)

1. **Primitive-based rendering** - Use existing KHGui render primitives and visual styles (rectangles, borders, text, icons) instead of adding new screenshot atlases. Replicate the original VS Code UI programmatically.
2. **Library, not app** - KHGui is a framework. It must not contain domain-specific logic (no references to specific scripts or mod mechanics).
3. **60 FPS** - Performance is critical; avoid heavy logic in `render()` calls.
4. **No Maven** - The project is intended to be embedded as a local jar mod (JiJ or Shadowing), not published to a public repository.
5. **Architectury Parity** - Changes MUST be compatible with both Fabric and NeoForge.
6. **Fabric Entrypoints** - The `main` entrypoint in `fabric.mod.json` MUST point to `kasperstudios.khgui.fabric.KHGuiFabric` (which implements `ModInitializer`), NOT the common class `kasperstudios.khgui.KHGui`. Otherwise, it will cause a `LanguageAdapterException` during startup (*[Official Fabric Documentation: Entrypoints](https://fabricmc.net/wiki/documentation:entrypoint)*).

---

## 📝 Current Status

### ✅ Implemented:
- ✅ Custom rendering engine using primitives matching the reference UI
- ✅ Full text editing suite (input, navigation, shortcuts)
- ✅ Decoupled Managers (`DraftManager`, `CodeCompletionManager`)
- ✅ API stable and host-mod ready
- ✅ Theme support (Catppuccin Mocha)
- ✅ Code Completion popup UI
- ✅ Hover Tooltips system

### 🚧 TODO:
1. Panel content rendering (visualizing file trees, debug lists)
2. Mouse selection & Drag-and-drop
3. Undo/Redo system implementation
4. Split view (multiple editor panes)
5. Search & Replace (global and local)