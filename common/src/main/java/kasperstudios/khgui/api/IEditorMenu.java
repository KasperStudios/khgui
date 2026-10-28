package kasperstudios.khgui.api;

import java.util.List;

/**
 * Represents a menu in the title bar (e.g., File, Edit, Help).
 */
public interface IEditorMenu {
    /**
     * Label for the menu.
     */
    String getLabel();

    /**
     * Items in the dropdown.
     */
    List<IMenuItem> getItems();
}
