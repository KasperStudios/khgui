package kasperstudios.khgui.fabric;

import kasperstudios.khgui.fabric.gui.screen.CodeEditorScreen;
import kasperstudios.khgui.api.IEditorContext;
import net.minecraft.client.gui.screens.Screen;

public class KHGuiScreensImpl {
    
    public static Screen createCodeEditorScreen(IEditorContext context) {
        System.out.println("DEBUG: KHGuiScreensImpl.createCodeEditorScreen with context called");
        return new CodeEditorScreen(context);
    }
}
