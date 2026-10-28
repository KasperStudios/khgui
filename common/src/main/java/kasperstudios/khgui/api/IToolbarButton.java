package kasperstudios.khgui.api;

/**
 * Custom toolbar button that can be placed in the editor toolbar.
 * Used for actions like "Run Script", "Debug", "Stop", etc.
 */
public interface IToolbarButton {
    /**
     * Unique button ID.
     */
    String getId();
    
    /**
     * Button label or icon text.
     */
    String getLabel();
    
    /**
     * Optional icon path (PNG texture).
     * If null, label will be used.
     */
    default String getIconPath() {
        return null;
    }
    
    /**
     * Tooltip text shown on hover.
     */
    String getTooltip();
    
    /**
     * Called when button is clicked.
     */
    void onClick();
    
    /**
     * Whether button is enabled.
     */
    default boolean isEnabled() {
        return true;
    }
    
    /**
     * Whether button is visible.
     */
    default boolean isVisible() {
        return true;
    }
    
    /**
     * Button color (for background or icon tint).
     * Return 0 for default theme color.
     */
    default int getColor() {
        return 0;
    }
}
