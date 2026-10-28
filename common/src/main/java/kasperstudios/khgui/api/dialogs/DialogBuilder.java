package kasperstudios.khgui.api.dialogs;

import kasperstudios.khgui.api.ITheme;
import net.minecraft.client.gui.screens.Screen;
import java.util.ArrayList;
import java.util.List;

/**
 * A builder class used to dynamically create custom dialog screens for
 * Minecraft.
 * Allows KasHub/mods to create UI menus without being hardcoded into KHGui.
 */
public class DialogBuilder {
    private final Screen parent;
    private final ITheme theme;
    private String title = "Dialog";
    private int width = 300;
    private int height = 200;
    private final List<IDialogElement> elements = new ArrayList<>();
    private final List<IDialogButton> buttons = new ArrayList<>();
    private Runnable onCloseAction = null;

    public DialogBuilder(Screen parent, ITheme theme) {
        this.parent = parent;
        this.theme = theme;
    }

    public DialogBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public DialogBuilder setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public DialogBuilder addElement(IDialogElement element) {
        this.elements.add(element);
        return this;
    }

    public DialogBuilder addButton(String text, int colorFallback, Runnable onClick) {
        this.buttons.add(new IDialogButton() {
            @Override
            public String getText() {
                return text;
            }

            @Override
            public int getColor(boolean hovered) {
                if (hovered)
                    return brighten(colorFallback, 20);
                return colorFallback;
            }

            @Override
            public void onClick() {
                onClick.run();
            }

            private int brighten(int c, int amt) {
                int a = (c >> 24) & 0xFF;
                int r = Math.min(255, ((c >> 16) & 0xFF) + amt);
                int g = Math.min(255, ((c >> 8) & 0xFF) + amt);
                int b = Math.min(255, (c & 0xFF) + amt);
                return (a << 24) | (r << 16) | (g << 8) | b;
            }
        });
        return this;
    }

    public DialogBuilder setOnClose(Runnable action) {
        this.onCloseAction = action;
        return this;
    }

    public Screen build() {
        return new CustomDialogScreen(parent, title, theme, width, height, elements, buttons, onCloseAction);
    }
}
