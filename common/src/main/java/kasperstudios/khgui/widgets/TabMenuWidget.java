package kasperstudios.khgui.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tab menu widget using ItemStack icons for compact tab navigation.
 * Ported from old TabMenuWidget.
 */
public class TabMenuWidget extends AbstractWidget {

    public record Tab(ItemStack icon, Component tooltip) {
    }

    @FunctionalInterface
    public interface TabChangeListener {
        void onTabChanged(int newTab);
    }

    private final List<Tab> tabs;
    private final Map<Integer, Boolean> enabled = new HashMap<>();
    private int activeTab = 0;
    private final TabChangeListener listener;

    public TabMenuWidget(int x, int y, int width, int height, List<Tab> tabs, TabChangeListener listener) {
        super(x, y, width, height, Component.empty());
        this.tabs = tabs;
        this.listener = listener;
        for (int i = 0; i < tabs.size(); i++)
            enabled.put(i, true);
    }

    public void setActiveTab(int index) {
        this.activeTab = index;
    }

    public int getActiveTab() {
        return activeTab;
    }

    public void setTabEnabled(int index, boolean value) {
        enabled.put(index, value);
    }

    public boolean isTabEnabled(int index) {
        return enabled.getOrDefault(index, true);
    }

    @Override
    public void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        if (tabs.isEmpty())
            return;
        int tabW = this.width / tabs.size();

        for (int i = 0; i < tabs.size(); i++) {
            int tabX = getX() + i * tabW;
            boolean isActive = i == activeTab;
            boolean isEnabled = enabled.getOrDefault(i, true);
            boolean isHovered = mouseX >= tabX && mouseX < tabX + tabW
                    && mouseY >= getY() && mouseY < getY() + height;

            int bg;
            if (!isEnabled)
                bg = 0xFF111827;
            else if (isActive)
                bg = 0xFF7c3aed;
            else if (isHovered)
                bg = 0xFF5b21b6;
            else
                bg = 0xFF1e1b4b;

            gfx.fill(tabX, getY(), tabX + tabW, getY() + height, bg);

            // Active underline
            if (isActive) {
                gfx.fill(tabX + 2, getY() + height - 2, tabX + tabW - 2, getY() + height, 0xFFCBA6F7);
            }

            // Item icon centered
            int iconX = tabX + (tabW - 16) / 2;
            int iconY = getY() + (height - 16) / 2;
            gfx.renderItem(tabs.get(i).icon(), iconX, iconY);

            // Disabled overlay
            if (!isEnabled) {
                gfx.fill(tabX, getY(), tabX + tabW, getY() + height, 0xAA000000);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible)
            return false;
        if (tabs.isEmpty())
            return false;

        int tabW = this.width / tabs.size();
        int clicked = (int) ((mouseX - getX()) / tabW);
        if (clicked < 0 || clicked >= tabs.size())
            return false;
        if (!enabled.getOrDefault(clicked, true))
            return false;

        if (activeTab != clicked) {
            activeTab = clicked;
            if (listener != null)
                listener.onTabChanged(clicked);
        }
        playDownSound(Minecraft.getInstance().getSoundManager());
        return true;
    }

    /** Returns the tooltip for the tab under the mouse, or null. */
    public Component getHoveredTooltip(int mouseX, int mouseY) {
        if (tabs.isEmpty())
            return null;
        int tabW = this.width / tabs.size();
        for (int i = 0; i < tabs.size(); i++) {
            int tabX = getX() + i * tabW;
            if (mouseX >= tabX && mouseX < tabX + tabW && mouseY >= getY() && mouseY < getY() + height) {
                return tabs.get(i).tooltip();
            }
        }
        return null;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        if (activeTab >= 0 && activeTab < tabs.size()) {
            narration.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE,
                    Component.literal("Tab menu, active: ").append(tabs.get(activeTab).tooltip()));
        }
    }
}
