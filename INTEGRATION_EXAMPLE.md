# KHGui Integration Example

This guide shows how to integrate KHGui into your Minecraft mod.

## 1. Add Dependency

KHGui should be embedded as a local jar dependency.

### Fabric (build.gradle)

```gradle
dependencies {
    modImplementation files("libs/khgui-fabric-1.0.0.jar")
    include files("libs/khgui-fabric-1.0.0.jar")
}
```

### NeoForge (build.gradle)

```gradle
dependencies {
    implementation files("libs/khgui-neoforge-1.0.0.jar")
}
```

## 2. Implement IEditorContext

Create a class that provides data to the editor:

```java
package com.yourmod;

import kasperstudios.khgui.api.IEditorContext;
import java.util.List;

public class YourEditorContext implements IEditorContext {
    private String currentFile = "script.lua";
    private List<String> openTabs = List.of("script.lua", "config.json");
    private int activeTab = 0;
    
    @Override
    public String getTitleText() {
        return "YourMod - " + currentFile;
    }
    
    @Override
    public List<String> getOpenTabs() {
        return openTabs;
    }
    
    @Override
    public int getActiveTabIndex() {
        return activeTab;
    }
    
    @Override
    public String getSidebarTitle() {
        return "EXPLORER";
    }
    
    @Override
    public String getStatusBarText() {
        return "Ln 1, Col 1  UTF-8  Lua";
    }
    
    @Override
    public String getLanguage() {
        return "Lua";
    }

    @Override
    public String getContent() {
        return "-- Current file content";
    }

    @Override
    public void save(String content) {
        // Implement your save logic here
        System.out.println("Saving: " + content);
    }

    @Override
    public void closeTab(int index) {
        // Implement your tab closing logic here
        System.out.println("Closing tab: " + index);
    }
}
```

## 3. Open the Editor

```java
import kasperstudios.khgui.KHGuiScreens;
import net.minecraft.client.Minecraft;

// Create your context
IEditorContext context = new YourEditorContext();

// Open the editor
Screen editor = KHGuiScreens.createCodeEditorScreen(context);
Minecraft.getInstance().setScreen(editor);
```

## 4. Register Custom Panels (Optional)

Create custom sidebar panels:

```java
import kasperstudios.khgui.api.IEditorPanel;

public class YourCustomPanel implements IEditorPanel {
    @Override
    public String getId() {
        return "yourpanel";
    }
    
    @Override
    public String getDisplayName() {
        return "YOUR PANEL";
    }
    
    @Override
    public String getIcon() {
        return "⚙️";
    }
    
    @Override
    public void onOpen() {
        System.out.println("Your panel opened!");
        // Load panel data, etc.
    }
}
```

Register it when creating the screen:

```java
Screen editor = KHGuiScreens.createCodeEditorScreen(context);
if (editor instanceof CodeEditorScreen codeEditor) {
    codeEditor.getPanelManager().register(new YourCustomPanel());
}
```

## 5. Implement File Provider (Optional)

For file tree functionality:

```java
import kasperstudios.khgui.api.IFileProvider;
import java.util.List;

public class YourFileProvider implements IFileProvider {
    @Override
    public List<FileNode> getFiles() {
        // Return your file tree structure
        return List.of(
            new FileNode("scripts/", true),
            new FileNode("scripts/main.lua", false),
            new FileNode("config.json", false)
        );
    }
    
    @Override
    public String loadFile(String path) {
        // Load file content
        return "-- Your file content";
    }
    
    @Override
    public void saveFile(String path, String content) {
        // Save file content
    }
    
    @Override
    public void deleteFile(String path) {
        // Delete file
    }
    
    @Override
    public void renameFile(String oldPath, String newPath) {
        // Rename file
    }
}
```

## 6. Implement Debug Provider (Optional)

For debug panel:

```java
import kasperstudios.khgui.api.IDebugProvider;
import java.util.Map;
import java.util.List;

public class YourDebugProvider implements IDebugProvider {
    @Override
    public Map<String, Object> getVariables() {
        return Map.of(
            "player", "Steve",
            "health", 20,
            "position", "100, 64, 200"
        );
    }
    
    @Override
    public List<String> getCallStack() {
        return List.of(
            "main.lua:15",
            "utils.lua:42",
            "init.lua:8"
        );
    }
    
    @Override
    public boolean isPaused() {
        return false;
    }
    
    @Override
    public boolean isRunning() {
        return true;
    }
}
```

## 7. Implement Console Provider (Optional)

For console output:

```java
import kasperstudios.khgui.api.IConsoleProvider;
import java.util.List;
import java.util.ArrayList;

public class YourConsoleProvider implements IConsoleProvider {
    private List<ConsoleMessage> messages = new ArrayList<>();
    
    @Override
    public List<ConsoleMessage> getMessages() {
        return messages;
    }
    
    @Override
    public void clear() {
        messages.clear();
    }
    
    @Override
    public void addMessage(String text, MessageType type) {
        messages.add(new ConsoleMessage(text, type));
    }
}
```

## Complete Example

```java
package com.yourmod;

import kasperstudios.khgui.KHGuiScreens;
import kasperstudios.khgui.api.IEditorContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class YourModIntegration {
    
    public static void openEditor() {
        // Create context with your data
        IEditorContext context = new YourEditorContext();
        
        // Open the editor
        Screen editor = KHGuiScreens.createCodeEditorScreen(context);
        Minecraft.getInstance().setScreen(editor);
    }
    
    // Call this from your keybinding or command
    public static void registerKeybinding() {
        // Fabric example
        KeyMapping openEditorKey = new KeyMapping(
            "key.yourmod.open_editor",
            GLFW.GLFW_KEY_E,
            "category.yourmod"
        );
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openEditorKey.consumeClick()) {
                openEditor();
            }
        });
    }
}
```

## Tips

1. **Context Updates**: Your IEditorContext should return current data each time methods are called
2. **Panel Registration**: Register panels before opening the screen
3. **Resource Management**: Clean up resources when the screen closes
4. **Thread Safety**: Ensure your providers are thread-safe if accessed from multiple threads
5. **Performance**: Cache expensive operations in your providers

## Demo Mode

KHGui includes a demo mode for testing. Press `H` in-game to see it in action.

## Need Help?

- Check [AI_GUIDE.md](AI_GUIDE.md) for technical details
- See [devnotes.md](devnotes.md) for current status
- Open an issue on GitHub for bugs or questions
