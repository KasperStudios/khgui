package kasperstudios.khgui.api;

/**
 * Interface for custom panels that can be added to the editor.
 * Examples: File Explorer, Debug Panel, Task Manager, Settings.
 * 
 * Platform-specific implementations should extend this and add rendering
 * methods.
 */
public interface IEditorPanel {

    /**
     * Unique ID for this panel
     */
    String getId();

    /**
     * Display name shown in UI
     */
    String getDisplayName();

    /**
     * Icon for activity bar (emoji or unicode)
     */
    String getIcon();

    /**
     * Called when panel is opened
     */
    default void onOpen() {
    }

    /**
     * Called when panel is closed
     */
    default void onClose() {
    }

    /**
     * Called when panel is resized
     */
    default void onResize(int width, int height) {
    }

    /**
     * Called every tick
     */
    default void tick() {
    }

    /**
     * Handle mouse click event
     */
    default boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height) {
        return false;
    }

    /**
     * Handle key press event
     */
    default boolean keyPressed(int keyCode, int scanCode, int modifiers, int x, int y, int width, int height) {
        return false;
    }

    /**
     * Handle character typed event
     */
    default boolean charTyped(char character, int modifiers, int x, int y, int width, int height) {
        return false;
    }

    /**
     * Handle mouse scrolled event
     */
    default boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, int x, int y, int width,
            int height) {
        return false;
    }

    /**
     * Render the panel content
     */
    void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick, int x, int y,
            int width, int height, ITheme theme);
}
