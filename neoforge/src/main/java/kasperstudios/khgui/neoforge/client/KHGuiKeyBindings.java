package kasperstudios.khgui.neoforge.client;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public class KHGuiKeyBindings {
    
    public static void register() {
        // Core KHGui does not register any default keybindings.
        // Consuming mods should register their own keybindings and call KHGuiScreens.
    }
    
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            // No default keybindings to register
        }
    }
}
