package kasperstudios.khgui.api;

/**
 * Represents a single item in a title bar dropdown menu.
 */
public interface IMenuItem {
    /**
     * Display label for the item.
     */
    String getLabel();

    /**
     * Action to perform when the item is clicked.
     */
    void onClick();

    /**
     * Optional icon path (emoji or texture path "namespace:path").
     */
    default String getIcon() {
        return null;
    }
}
