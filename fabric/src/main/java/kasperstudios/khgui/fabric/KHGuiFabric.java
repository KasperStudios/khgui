package kasperstudios.khgui.fabric;

import kasperstudios.khgui.KHGui;
import net.fabricmc.api.ModInitializer;

public class KHGuiFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        KHGui.init();
    }
}
