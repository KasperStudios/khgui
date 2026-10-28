package kasperstudios.khgui.completion;

import kasperstudios.khgui.api.ICompletionProvider;
import kasperstudios.khgui.api.ICompletionProvider.CompletionItem;
import kasperstudios.khgui.core.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Popup window showing code completion suggestions.
 */
public class CompletionPopup {
    private List<CompletionItem> items = new ArrayList<>();
    private int selectedIndex = 0;
    private boolean visible = false;
    
    private int x, y;
    private int width = 300;
    private int maxHeight = 200;
    private int itemHeight = 20;
    
    private final Font font;
    
    public CompletionPopup() {
        this.font = Minecraft.getInstance().font;
    }
    
    /**
     * Shows the popup with completion items.
     */
    public void show(List<CompletionItem> items, int x, int y) {
        this.items = new ArrayList<>(items);
        this.selectedIndex = 0;
        this.visible = !items.isEmpty();
        this.x = x;
        this.y = y;
    }
    
    /**
     * Hides the popup.
     */
    public void hide() {
        this.visible = false;
        this.items.clear();
    }
    
    /**
     * Checks if popup is visible.
     */
    public boolean isVisible() {
        return visible;
    }
    
    /**
     * Moves selection up.
     */
    public void selectPrevious() {
        if (!items.isEmpty()) {
            selectedIndex = (selectedIndex - 1 + items.size()) % items.size();
        }
    }
    
    /**
     * Moves selection down.
     */
    public void selectNext() {
        if (!items.isEmpty()) {
            selectedIndex = (selectedIndex + 1) % items.size();
        }
    }
    
    /**
     * Gets the currently selected item.
     */
    public CompletionItem getSelectedItem() {
        if (items.isEmpty() || selectedIndex < 0 || selectedIndex >= items.size()) {
            return null;
        }
        return items.get(selectedIndex);
    }
    
    /**
     * Renders the completion popup.
     */
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!visible || items.isEmpty()) return;
        
        var theme = ThemeManager.getCurrentTheme();
        
        // Calculate dimensions
        int displayCount = Math.min(items.size(), maxHeight / itemHeight);
        int height = displayCount * itemHeight + 4;
        
        // Background
        graphics.fill(x, y, x + width, y + height, theme.getSidebarBackground());
        graphics.fill(x, y, x + width, y + 1, theme.getBorder());
        graphics.fill(x, y + height - 1, x + width, y + height, theme.getBorder());
        graphics.fill(x, y, x + 1, y + height, theme.getBorder());
        graphics.fill(x + width - 1, y, x + width, y + height, theme.getBorder());
        
        // Render items
        int startIndex = Math.max(0, selectedIndex - displayCount / 2);
        int endIndex = Math.min(items.size(), startIndex + displayCount);
        
        for (int i = startIndex; i < endIndex; i++) {
            CompletionItem item = items.get(i);
            int itemY = y + 2 + (i - startIndex) * itemHeight;
            boolean isSelected = (i == selectedIndex);
            
            // Selection background
            if (isSelected) {
                graphics.fill(x + 1, itemY, x + width - 1, itemY + itemHeight, theme.getSelection());
            }
            
            // Icon (based on kind)
            String icon = getIconForKind(item.kind);
            int iconColor = getColorForKind(item.kind, theme);
            graphics.drawString(font, icon, x + 6, itemY + 6, iconColor, false);
            
            // Label
            int textColor = isSelected ? theme.getPrimaryText() : theme.getSecondaryText();
            graphics.drawString(font, item.label, x + 24, itemY + 6, textColor, false);
            
            // Detail (right-aligned)
            if (item.detail != null) {
                int detailWidth = font.width(item.detail);
                graphics.drawString(font, item.detail, x + width - detailWidth - 6, itemY + 6, theme.getMutedText(), false);
            }
        }
        
        // Scroll indicator
        if (items.size() > displayCount) {
            int scrollBarHeight = Math.max(20, (displayCount * height) / items.size());
            int scrollBarY = y + 2 + ((selectedIndex * (height - scrollBarHeight - 4)) / items.size());
            graphics.fill(x + width - 6, scrollBarY, x + width - 2, scrollBarY + scrollBarHeight, theme.getPrimaryAccent());
        }
    }
    
    private String getIconForKind(ICompletionProvider.CompletionKind kind) {
        return switch (kind) {
            case METHOD, FUNCTION -> "ƒ";
            case VARIABLE -> "x";
            case FIELD -> "•";
            case CLASS -> "C";
            case KEYWORD -> "K";
            case SNIPPET -> "{}";
            case PROPERTY -> "P";
            case CONSTANT -> "#";
            case ENUM -> "E";
            case MODULE -> "M";
        };
    }
    
    private int getColorForKind(ICompletionProvider.CompletionKind kind, kasperstudios.khgui.api.ITheme theme) {
        return switch (kind) {
            case METHOD, FUNCTION -> theme.getSyntaxFunction();
            case VARIABLE, FIELD -> theme.getSyntaxVariable();
            case CLASS, ENUM -> theme.getSyntaxKeyword();
            case KEYWORD -> theme.getSyntaxKeyword();
            case CONSTANT -> theme.getSyntaxNumber();
            default -> theme.getPrimaryText();
        };
    }
    
    /**
     * Checks if mouse is over the popup.
     */
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!visible) return false;
        int displayCount = Math.min(items.size(), maxHeight / itemHeight);
        int height = displayCount * itemHeight + 4;
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
    
    /**
     * Alias for isMouseOver to match API calls from screens.
     */
    public boolean contains(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY);
    }
    
    /**
     * Gets the item at the specified mouse position.
     */
    public CompletionItem getItemAt(double mouseX, double mouseY) {
        if (!isMouseOver(mouseX, mouseY)) return null;
        
        int displayCount = Math.min(items.size(), maxHeight / itemHeight);
        int startIndex = Math.max(0, selectedIndex - displayCount / 2);
        
        int relativeY = (int)(mouseY - y - 2);
        int clickedIndex = startIndex + (relativeY / itemHeight);
        
        if (clickedIndex >= 0 && clickedIndex < items.size()) {
            return items.get(clickedIndex);
        }
        
        return null;
    }

    /**
     * Handles mouse click on popup.
     */
    public boolean handleClick(double mouseX, double mouseY) {
        if (!isMouseOver(mouseX, mouseY)) return false;
        
        CompletionItem item = getItemAt(mouseX, mouseY);
        if (item != null) {
            // Find the index of this item
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i) == item) {
                    selectedIndex = i;
                    return true;
                }
            }
        }
        
        return false;
    }
}
