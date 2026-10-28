package kasperstudios.khgui;

import kasperstudios.khgui.api.IEditorContext;
import net.minecraft.client.gui.screens.Screen;
import dev.architectury.injectables.annotations.ExpectPlatform;

/**
 * Platform-agnostic screen factory for KHGui.
 * Uses @ExpectPlatform to delegate screen creation to platform-specific
 * implementations.
 */
public class KHGuiScreens {

    
    /**
     * Creates the main code editor screen with custom context.
     * This is the method KasHub should use to provide its own data.
     * 
     * @param context Editor context providing data for display
     * @return Screen instance (platform-specific type)
     */
    @ExpectPlatform
    public static Screen createCodeEditorScreen(IEditorContext context) {
        throw new AssertionError("This method should be replaced by Architectury at runtime");
    }
}
