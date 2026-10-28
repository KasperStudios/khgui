package kasperstudios.khgui.api.dialogs;

import kasperstudios.khgui.api.ITheme;
import net.minecraft.client.gui.screens.Screen;
import java.util.List;

/**
 * Interface representing a configured dialog built via DialogBuilder.
 */
public interface IDialog {

    /**
     * Gets the parent screen to return to when closed.
     */
    Screen getParent();

    /**
     * Gets the title of the dialog.
     */
    String getDialogTitle();

    /**
     * Gets the theme used to render the dialog.
     */
    ITheme getTheme();

    /**
     * Gets the width of the dialog window.
     */
    int getWidth();

    /**
     * Gets the height of the dialog window.
     */
    int getHeight();

    /**
     * Gets all registered elements within the dialog.
     */
    List<IDialogElement> getElements();

    /**
     * Adds an element to the dialog dynamic layout.
     */
    void addElement(IDialogElement element);

    /**
     * Gets all registered footer buttons.
     */
    List<IDialogButton> getButtons();

    /**
     * Gets the custom close action.
     */
    Runnable getOnClose();
}
