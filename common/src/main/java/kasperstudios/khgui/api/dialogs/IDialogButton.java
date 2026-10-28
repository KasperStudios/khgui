package kasperstudios.khgui.api.dialogs;

/**
 * Represents a button rendered in the footer of a dialog.
 */
public interface IDialogButton {

    /**
     * Gets the display text for the button.
     */
    String getText();

    /**
     * Gets the button color logic, typically overriding the default border color.
     */
    int getColor(boolean hovered);

    /**
     * Action called when clicked.
     */
    void onClick();
}
