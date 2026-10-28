package kasperstudios.khgui.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

/**
 * Fabric command registration for KHGui.
 */
public class KHGuiCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        // Consuming mods should register their own commands.
        // This class is kept for future library-wide commands if needed.
    }
}
