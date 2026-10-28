package kasperstudios.khgui.core;

import kasperstudios.khgui.api.IToolbarButton;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages custom toolbar buttons.
 * Buttons are rendered on the right side of the tab bar.
 */
public class ToolbarManager {
    private final List<IToolbarButton> buttons = new ArrayList<>();
    private final IconManager iconManager;
    
    public static final int BUTTON_SIZE = 22; // Уменьшено для 16x16 иконки
    public static final int BUTTON_SPACING = 2; // Меньше отступ
    
    public void clear() {
        buttons.clear();
    }
    
    public ToolbarManager(IconManager iconManager) {
        this.iconManager = iconManager;
    }
    
    /**
     * Registers a toolbar button.
     */
    public void register(IToolbarButton button) {
        if (!buttons.contains(button)) {
            buttons.add(button);
        }
    }
    
    /**
     * Unregisters a toolbar button.
     */
    public void unregister(String buttonId) {
        buttons.removeIf(b -> b.getId().equals(buttonId));
    }
    
    /**
     * Gets all registered buttons.
     */
    public List<IToolbarButton> getButtons() {
        return new ArrayList<>(buttons);
    }
    
    /**
     * Renders toolbar buttons.
     * @param graphics GuiGraphics
     * @param x Right edge X position (buttons render from right to left)
     * @param y Y position
     * @param mouseX Mouse X
     * @param mouseY Mouse Y
     */
    public void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        var theme = ThemeManager.getCurrentTheme();
        int currentX = x;
        
        for (int i = buttons.size() - 1; i >= 0; i--) {
            IToolbarButton button = buttons.get(i);
            if (!button.isVisible()) continue;
            
            currentX -= BUTTON_SIZE;
            
            boolean isHovered = mouseX >= currentX && mouseX < currentX + BUTTON_SIZE &&
                              mouseY >= y && mouseY < y + BUTTON_SIZE;
            boolean isEnabled = button.isEnabled();
            
            // Лёгкий hover - только полупрозрачный overlay, без рамки
            if (isHovered && isEnabled) {
                graphics.fill(currentX, y, currentX + BUTTON_SIZE, y + BUTTON_SIZE, 
                    theme.getHover());
            }
            
            // Иконка 16x16 по центру
            if (button.getIconPath() != null && iconManager.hasIcon(button.getId())) {
                float alpha = isEnabled ? 1.0f : 0.4f;
                int iconX = currentX + (BUTTON_SIZE - 16) / 2;
                int iconY = y + (BUTTON_SIZE - 16) / 2;
                iconManager.renderIcon(graphics, button.getId(), iconX, iconY, 16, alpha);
            } else {
                // Текстовый лейбл по центру
                int textColor = isEnabled ? theme.getPrimaryText() : theme.getMutedText();
                var mc = net.minecraft.client.Minecraft.getInstance();
                int labelWidth = mc.font.width(button.getLabel());
                graphics.drawString(mc.font, button.getLabel(), 
                    currentX + (BUTTON_SIZE - labelWidth) / 2, 
                    y + (BUTTON_SIZE - mc.font.lineHeight) / 2, 
                    textColor, false);
            }
            
            currentX -= BUTTON_SPACING;
        }
    }
    
    /**
     * Handles mouse click on toolbar.
     * @return true if a button was clicked
     */
    public boolean handleClick(double mouseX, double mouseY, int x, int y) {
        int currentX = x;
        
        for (int i = buttons.size() - 1; i >= 0; i--) {
            IToolbarButton button = buttons.get(i);
            if (!button.isVisible()) continue;
            
            currentX -= BUTTON_SIZE;
            
            if (mouseX >= currentX && mouseX < currentX + BUTTON_SIZE &&
                mouseY >= y && mouseY < y + BUTTON_SIZE) {
                if (button.isEnabled()) {
                    button.onClick();
                    return true;
                }
            }
            
            currentX -= BUTTON_SPACING;
        }
        
        return false;
    }
    
    /**
     * Gets tooltip for button under mouse.
     */
    public String getTooltip(double mouseX, double mouseY, int x, int y) {
        int currentX = x;
        
        for (int i = buttons.size() - 1; i >= 0; i--) {
            IToolbarButton button = buttons.get(i);
            if (!button.isVisible()) continue;
            
            currentX -= BUTTON_SIZE;
            
            if (mouseX >= currentX && mouseX < currentX + BUTTON_SIZE &&
                mouseY >= y && mouseY < y + BUTTON_SIZE) {
                return button.getTooltip();
            }
            
            currentX -= BUTTON_SPACING;
        }
        
        return null;
    }
    
    /**
     * Calculates total width needed for all visible buttons.
     */
    public int getTotalWidth() {
        int count = (int) buttons.stream().filter(IToolbarButton::isVisible).count();
        if (count == 0) return 0;
        return count * BUTTON_SIZE + (count - 1) * BUTTON_SPACING;
    }
}
