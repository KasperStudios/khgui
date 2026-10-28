package kasperstudios.khgui.neoforge;

import kasperstudios.khgui.KHGui;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import kasperstudios.khgui.neoforge.client.KHGuiNeoForgeClient;
import kasperstudios.khgui.neoforge.client.KHGuiKeyBindings;

@Mod(KHGui.MOD_ID)
public final class KHGuiNeoForge {
    public KHGuiNeoForge(IEventBus modBus) {
        // Run common setup
        KHGui.init();
        
        // Register event handlers explicitly to avoid deprecated @EventBusSubscriber warnings
        modBus.addListener(this::clientSetup);
        modBus.register(KHGuiNeoForgeClient.class);
    }
    
    private void clientSetup(FMLClientSetupEvent event) {
        // Client-side initialization will be done via KHGuiNeoForgeClient
    }
}
