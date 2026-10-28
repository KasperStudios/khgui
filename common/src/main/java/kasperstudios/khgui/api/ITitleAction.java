package kasperstudios.khgui.api;

/**
 * Represents a direct action button in the editor title bar.
 * Unlike menus, these trigger immediately when clicked.
 */
public interface ITitleAction {
    /**
     * Unique identifier for the action.
     */
    String getId();

    /**
     * Label text for the button.
     */
    String getLabel();

    /**
     * Optional icon for the button (emoji or emoji-like string).
     */
    default String getIcon() { return ""; }

    /**
     * Tooltip text.
     */
    default String getTooltip() { return getLabel(); }

    /**
     * Called when the button is clicked.
     */
    void onClick();
    
    /**
     * Background color for the button. If 0, uses default.
     */
    default int getBackgroundColor() { return 0; }
}
