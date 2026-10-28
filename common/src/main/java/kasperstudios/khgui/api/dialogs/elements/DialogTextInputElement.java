/**
 * [dialog_text_input_element]: Dialog Text Input Element
 * REQUEST_CONTEXT: Provide a basic text input element for KHGui dialogs to implement Save As.
 * AGENT_THOUGHTS: Adds a simple interactive element with label, focus handling, and input methods.
 * ---------------------------------------------------------
 */
package kasperstudios.khgui.api.dialogs.elements;

import kasperstudios.khgui.api.ITheme;
import kasperstudios.khgui.api.dialogs.IDialogElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class DialogTextInputElement implements IDialogElement {
    private final String label;
    private final ITheme theme;
    private final Font font;
    private final int height;
    private final int fieldHeight;

    private String value;
    private boolean focused = false;

    private int lastX;
    private int lastY;
    private int lastW;

    public DialogTextInputElement(String label, String initialValue, ITheme theme) {
        this(label, initialValue, theme, 44, 22);
    }

    public DialogTextInputElement(String label, String initialValue, ITheme theme, int height, int fieldHeight) {
        this.label = label;
        this.value = initialValue == null ? "" : initialValue;
        this.theme = theme;
        this.height = height;
        this.fieldHeight = fieldHeight;
        this.font = Minecraft.getInstance().font;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value == null ? "" : value;
    }

    @Override
    public void render(GuiGraphics gfx, int x, int y, int width, int mouseX, int mouseY, float delta) {
        this.lastX = x;
        this.lastY = y;
        this.lastW = width;

        int labelY = y + 4;
        gfx.drawString(font, label, x + 8, labelY, theme.getSecondaryText(), false);

        int inputY = y + 18;
        int inputX = x + 8;
        int inputW = width - 16;
        int bgColor = focused ? adjustBrightness(theme.getEditorBackground(), 10) : theme.getEditorBackground();
        gfx.fill(inputX, inputY, inputX + inputW, inputY + fieldHeight, bgColor);

        if (focused) {
            int accent = theme.getPrimaryAccent();
            gfx.fill(inputX, inputY, inputX + inputW, inputY + 1, accent);
            gfx.fill(inputX, inputY + fieldHeight - 1, inputX + inputW, inputY + fieldHeight, accent);
            gfx.fill(inputX, inputY, inputX + 1, inputY + fieldHeight, accent);
            gfx.fill(inputX + inputW - 1, inputY, inputX + inputW, inputY + fieldHeight, accent);
        }

        String display = value.isEmpty() ? "" : value;
        int textColor = value.isEmpty() ? theme.getMutedText() : theme.getPrimaryText();
        gfx.drawString(font, display, inputX + 6, inputY + 7, textColor, false);

        if (focused && System.currentTimeMillis() % 1000 < 500) {
            int cursorX = inputX + 6 + font.width(value);
            gfx.fill(cursorX, inputY + 5, cursorX + 1, inputY + fieldHeight - 5, theme.getPrimaryText());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int inputX = lastX + 8;
        int inputY = lastY + 18;
        int inputW = lastW - 16;
        boolean inside = mouseX >= inputX && mouseX < inputX + inputW
                && mouseY >= inputY && mouseY < inputY + fieldHeight;
        focused = inside;
        return inside;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused) {
            return false;
        }
        if (keyCode == 259 && !value.isEmpty()) { // BACKSPACE
            value = value.substring(0, value.length() - 1);
            return true;
        }
        if (keyCode == 256) { // ESC
            focused = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        if (!focused) {
            return false;
        }
        if (Character.isLetterOrDigit(character) || character == '_' || character == '-' || character == '.' || character == ' ') {
            value += character;
            return true;
        }
        return false;
    }

    @Override
    public int getHeight() {
        return height;
    }

    private int adjustBrightness(int color, int amount) {
        int a = (color >> 24) & 0xFF;
        int r = Math.max(0, Math.min(255, ((color >> 16) & 0xFF) + amount));
        int g = Math.max(0, Math.min(255, ((color >> 8) & 0xFF) + amount));
        int b = Math.max(0, Math.min(255, (color & 0xFF) + amount));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
