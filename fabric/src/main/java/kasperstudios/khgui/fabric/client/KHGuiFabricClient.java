package kasperstudios.khgui.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import kasperstudios.khgui.fabric.command.KHGuiCommand;

public final class KHGuiFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register KHGui commands
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            KHGuiCommand.register(dispatcher);
        });

        // Register keybindings
        KHGuiKeyBindings.register();
    }
}
