package kasperstudios.khgui.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages activity bar icons (PNG images).
 */
public class IconManager {
    private final Map<String, ResourceLocation> icons = new HashMap<>();
    
    /**
     * Registers an icon for a panel.
     * @param panelId Panel ID
     * @param iconPath Path to icon texture (e.g., "textures/icons/explorer.png")
     */
    public void registerIcon(String panelId, String iconPath) {
        if (iconPath.contains(":")) {
            String[] parts = iconPath.split(":", 2);
            icons.put(panelId, ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]));
        } else {
            icons.put(panelId, ResourceLocation.fromNamespaceAndPath("khgui", iconPath));
        }
    }
    
    /**
     * Registers an icon for a panel using a specific ResourceLocation.
     * @param panelId Panel ID
     * @param texture ResourceLocation of the texture
     */
    public void registerIcon(String panelId, ResourceLocation texture) {
        icons.put(panelId, texture);
    }
    
    /**
     * Renders an icon at the specified position.
     * @param graphics GuiGraphics
     * @param panelId Panel ID
     * @param x X position
     * @param y Y position
     * @param size Icon size (width and height)
     * @param alpha Alpha value (0.0 - 1.0)
     */
    public void renderIcon(GuiGraphics graphics, String panelId, int x, int y, int size, float alpha) {
        ResourceLocation icon = icons.get(panelId);
        if (icon == null) return;
        
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        RenderSystem.enableBlend();
        
        graphics.blit(icon, x, y, 0, 0, size, size, size, size);
        
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }
    
    /**
     * Checks if an icon is registered for a panel.
     */
    public boolean hasIcon(String panelId) {
        return icons.containsKey(panelId);
    }
}
