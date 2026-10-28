package kasperstudios.khgui.api.dialogs;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Base interface for an element rendered inside a dialog body.
 */
public interface IDialogElement {

    /**
     * Renders the element at the specified position.
     * 
     * @param gfx    The graphics context.
     * @param x      The absolute X coordinate.
     * @param y      The absolute Y coordinate.
     * @param width  The available width.
     * @param mouseX Current mouse X.
     * @param mouseY Current mouse Y.
     * @param delta  Frame delta time.
     */
    void render(GuiGraphics gfx, int x, int y, int width, int mouseX, int mouseY, float delta);

    /**
     * Handles mouse clicks.
     * 
     * @return true if the click was consumed.
     */
    boolean mouseClicked(double mouseX, double mouseY, int button);

    /**
     * Handles key presses for interactive elements (optional).
     */
    default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    /**
     * Handles character input for interactive elements (optional).
     */
    default boolean charTyped(char character, int modifiers) {
        return false;
    }

    /**
     * Gets the required height of this element to compute the layout logic.
     */
    int getHeight();
}
