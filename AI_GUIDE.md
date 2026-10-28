# 🤖 KHGui AI Development Guide

**Single source of truth for AI agents working on KHGui.**

---

## 🎯 What is KHGui?

**Screenshot-based VS Code GUI framework for Minecraft mods.**

- GUI library (not complete app)
- Used by KasHub mod for script editing
- Multiloader: Fabric, NeoForge via Architectury
- Minecraft 1.21+, Java 21+
- **Default theme: Cyber Purple** (фиолетовая тема)

---

## 🏗️ Architecture

### Core Concept

```
KasHub (app) → uses → KHGui (framework)
     ↓
Provides data via interfaces
     ↓
KHGui renders UI from screenshots
```

**KHGui provides:**
- Screenshot-based UI rendering
- Layout system
- Hit-map for interactions
- Interfaces for extension

**KasHub provides:**
- Script execution
- File management
- Debug data
- Autocomplete
- Panel content

---

## 🔌 Extension Points

### 1. IEditorContext

```java
public interface IEditorContext {
    String getTitleText();           // "KasHub - script.lua"
    List<String> getOpenTabs();      // ["script.lua", "config.json"]
    int getActiveTabIndex();         // 0
    String getSidebarTitle();        // "EXPLORER"
    String getStatusBarText();       // "Ln 1, Col 1  UTF-8  Lua"
    String getLanguage();            // "Lua"
}
```

### 2. IEditorPanel

```java
public interface IEditorPanel {
    String getId();                  // "explorer"
    String getDisplayName();         // "EXPLORER"
    String getIcon();                // "📁"
    void onOpen();                   // Called when panel opens
}
```

### 3. IFileProvider

```java
public interface IFileProvider {
    List<FileNode> getFiles();
    String loadFile(String path);
    void saveFile(String path, String content);
    void deleteFile(String path);
    void renameFile(String oldPath, String newPath);
}
```

### 4. IDebugProvider

```java
public interface IDebugProvider {
    Map<String, Object> getVariables();
    List<String> getCallStack();
    boolean isPaused();
    boolean isRunning();
}
```

### 5. IConsoleProvider

```java
public interface IConsoleProvider {
    List<ConsoleMessage> getMessages();
    void clear();
    void addMessage(String text, MessageType type);
}
```

---

## 📐 Layout System

```
┌─────────────────────────────────────────────────────────┐
│ Title Bar (30px)                                        │
├──┬──────────────────────────┬────────────────────────────┤
│  │ Sidebar Title            │ Tab1 × Tab2 × Tab3         │
│A │ ─────────────────────── │ ──────────────────────────│
│c │                          │                            │
│t │  Panel Content           │  Editor Content            │
│i │  (from IEditorPanel)     │  (code with line numbers)  │
│v │                          │                            │
│i │                          │                            │
│t │                          │                            │
│y │                          │                            │
│  │                          │                            │
│B │                          │                            │
│a │                          │                            │
│r │                          │                            │
│  │                          │                            │
├──┼──────────────────────────┴────────────────────────────┤
│  │ Status Bar (22px)                                    │
└──┴──────────────────────────────────────────────────────┘
```

**Key measurements:**
- Title bar: 30px
- Activity bar: 48px wide
- Sidebar: 180px wide (resizable 170-400px)
- Status bar: 22px
- Tab height: 35px
- All coordinates scale dynamically with screen size

---

## 🎨 Screenshot-Based Rendering

### How it works:

1. **Take clean VS Code screenshot** (`reference.png`)
2. **Slice into regions** (title bar, activity bar, etc.)
3. **Generate atlas** (`vscode_ui.png` + `vscode_ui.json`)
4. **Runtime:** Blit regions based on JSON coordinates

### Atlas JSON format:

```json
{
  "title_bar": { "x": 0, "y": 0, "w": 1920, "h": 43, "slice9": [10, 10, 10, 10] },
  "activity_bar": { "x": 0, "y": 43, "w": 52, "h": 950, "slice9": [2, 2, 2, 2] },
  "sidebar": { "x": 52, "y": 43, "w": 180, "h": 950, "slice9": [2, 2, 2, 2] },
  "tab_bar": { "x": 232, "y": 43, "w": 1688, "h": 37, "slice9": [2, 2, 0, 2] },
  "editor_bg": { "x": 232, "y": 80, "w": 1688, "h": 913, "slice9": [10, 10, 10, 10] },
  "status_bar": { "x": 0, "y": 993, "w": 1920, "h": 27, "slice9": [2, 10, 2, 10] }
}
```

**Note:** Coordinates are from reference 1920x1080 screenshot. At runtime, textures are stretched using slice9 to fit actual screen size.

### Overlays (rendered over textures):

- Code text with syntax highlighting
- Line numbers
- Cursor (blinking)
- Selection highlight
- Tab names
- Title text
- Status text

---

## 🎯 Implementation Rules

### 1. Performance (60 FPS Always)

```java
// ✅ GOOD - cache
private int cachedValue = -1;
public int getValue() {
    if (cachedValue == -1) {
        cachedValue = expensiveCalculation();
    }
    return cachedValue;
}

// ❌ BAD - calculate every frame
public int getValue() {
    return expensiveCalculation(); // 60 times/sec!
}
```

### 2. Bounds Checking

```java
// ✅ GOOD - respect bounds
public void render(GuiGraphics g, int x, int y, int w, int h) {
    if (itemY < y || itemY > y + h) return; // Skip offscreen
    g.fill(x, itemY, x + w, itemY + 20, color);
}

// ❌ BAD - overflow
public void render(GuiGraphics g, int x, int y, int w, int h) {
    g.fill(x, itemY, x + 999, itemY + 20, color); // Overflows!
}
```

### 3. No Hardcoded Colors

```java
// ✅ GOOD - use theme or constants
int color = SyntaxHighlighter.COLOR_KEYWORD;

// ❌ BAD - hardcoded
int color = 0xFF569CD6;
```

---

## 🚀 Quick Start Example

```java
// 1. Create context
IEditorContext context = new MyEditorContext();

// 2. Create screen
Screen editor = KHGuiScreens.createCodeEditorScreen(context);

// 3. Show
Minecraft.getInstance().setScreen(editor);
```

---

## 🔧 Common Tasks

### Open Editor

```java
// With custom context
Screen editor = KHGuiScreens.createCodeEditorScreen(myContext);
Minecraft.getInstance().setScreen(editor);

// Demo mode (uses MockEditorContext)
Screen editor = KHGuiScreens.createCodeEditorScreen();
Minecraft.getInstance().setScreen(editor);
```

### Register Custom Panel

```java
public class MyPanel implements IEditorPanel {
    @Override
    public String getId() { return "mypanel"; }
    
    @Override
    public String getDisplayName() { return "MY PANEL"; }
    
    @Override
    public String getIcon() { return "📋"; }
    
    @Override
    public void onOpen() {
        // Panel opened
    }
}

editor.registerPanel(new MyPanel());
```

---

## 📊 File Structure

```
KHGui/
├── common/
│   ├── api/              # Interfaces for KasHub
│   │   ├── IEditorContext.java
│   │   ├── IEditorPanel.java
│   │   ├── IFileProvider.java
│   │   ├── IDebugProvider.java
│   │   └── IConsoleProvider.java
│   ├── core/             # Core systems
│   │   ├── ThemeManager.java
│   │   └── themes/
│   ├── editor/           # Editor logic
│   │   ├── CodeDocument.java
│   │   └── SyntaxHighlighter.java
│   └── demo/             # Demo/testing
│       ├── MockEditorContext.java
│       └── MockScriptEnvironment.java
├── fabric/               # Fabric implementation
│   └── gui/
│       ├── screen/       # CodeEditorScreen
│       ├── layout/       # GuiLayout
│       ├── renderer/     # CodeRenderer
│       └── atlas/        # UiAtlas
├── neoforge/             # NeoForge implementation
└── tools/
    └── clean_reference.py  # Screenshot cleaning
```

---

## ⚠️ Critical Rules

1. **Screenshot-based** - All UI from textures, not manual components
2. **60 FPS mandatory** - Profile and optimize
3. **Respect bounds** - Never render outside given area
4. **No hardcoded colors** - Use theme system
5. **Library not app** - KHGui provides framework, KasHub provides data
6. **Pixel-perfect** - Must look exactly like VS Code

---

## 🎯 Testing

### Commands

```bash
# Build
./gradlew build

# Run Fabric
./gradlew :fabric:runClient
```

### In-Game

Press `H` key to open demo editor

---

## 📝 Current Status

### ✅ Working:
- Screen opens with keybind H
- Layout system with dynamic scaling
- Atlas loading and rendering
- Code display with line numbers
- API interfaces (IEditorContext, IEditorPanel, ITheme, etc.)
- Demo mode with MockEditorContext
- **Cyber Purple theme (default)**
- Autocomplete (Ctrl+Space)
- Hover tooltips
- PNG icon system
- Resizable sidebar (180px default)

### 🚧 TODO:
- Panel content rendering
- Text selection
- Clipboard operations
- Undo/redo system
- Context menus

---

*Last updated: 2026-02-21*