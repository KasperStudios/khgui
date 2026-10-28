package kasperstudios.khgui.neoforge.client;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class KHGuiNeoForgeClient {
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Register keybindings
        event.enqueueWork(KHGuiKeyBindings::register);
    }
}
