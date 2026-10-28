package kasperstudios.khgui.api.dialogs.elements;

import kasperstudios.khgui.api.dialogs.IDialogElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.function.Consumer;

/**
 * A dialog element that displays a selectable list of items.
 */
public class DialogListElement implements IDialogElement {

    private final List<String> items;
    private final Consumer<String> onSelect;
    private int selectedIndex = -1;
    private int hoveredIndex = -1;
    private final int itemHeight = 22;

    private int lastX, lastY, lastWidth;

    public DialogListElement(List<String> items, Consumer<String> onSelect) {
        this.items = items;
        this.onSelect = onSelect;
    }

    @Override
    public void render(GuiGraphics gfx, int x, int y, int width, int mouseX, int mouseY, float delta) {
        this.lastX = x;
        this.lastY = y;
        this.lastWidth = width;
        
        Font font = Minecraft.getInstance().font;
        hoveredIndex = -1;

        for (int i = 0; i < items.size(); i++) {
            int itemY = y + (i * itemHeight);
            boolean isHovered = mouseX >= x && mouseX < x + width && mouseY >= itemY && mouseY < itemY + itemHeight;
            
            if (isHovered) {
                hoveredIndex = i;
                gfx.fill(x + 2, itemY + 1, x + width - 2, itemY + itemHeight - 1, 0x22FFFFFF);
            }
            
            if (i == selectedIndex) {
                gfx.fill(x + 2, itemY + 1, x + width - 2, itemY + itemHeight - 1, 0x44FFFFFF);
            }

            String text = items.get(i);
            gfx.drawString(font, text, x + 10, itemY + (itemHeight - 8) / 2, 0xFFFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hoveredIndex != -1 && hoveredIndex < items.size()) {
            selectedIndex = hoveredIndex;
            onSelect.accept(items.get(selectedIndex));
            return true;
        }
        return false;
    }

    @Override
    public int getHeight() {
        return items.size() * itemHeight;
    }
    
    public String getSelected() {
        if (selectedIndex >= 0 && selectedIndex < items.size()) {
            return items.get(selectedIndex);
        }
        return null;
    }
}
