package kasperstudios.khgui.neoforge.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

/**
 * NeoForge command registration for KHGui.
 */
public class KHGuiCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Consuming mods should register their own commands.
        // This class is kept for future library-wide commands if needed.
    }
}
