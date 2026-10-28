package kasperstudios.khgui.neoforge;

import kasperstudios.khgui.neoforge.gui.screen.CodeEditorScreen;
import kasperstudios.khgui.api.IEditorContext;
import net.minecraft.client.gui.screens.Screen;

public class KHGuiScreensImpl {
    
    public static Screen createCodeEditorScreen(IEditorContext context) {
        System.out.println("DEBUG: KHGuiScreensImpl.createCodeEditorScreen with context called (NeoForge)");
        return new CodeEditorScreen(context);
    }
}
