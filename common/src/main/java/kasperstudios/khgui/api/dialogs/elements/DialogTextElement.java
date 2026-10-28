package kasperstudios.khgui.api.dialogs.elements;

import kasperstudios.khgui.api.dialogs.IDialogElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class DialogTextElement implements IDialogElement {
    private final String text;
    private final int color;
    private final boolean centered;
    private final Font font;

    public DialogTextElement(String text, int color, boolean centered) {
        this.text = text;
        this.color = color;
        this.centered = centered;
        this.font = Minecraft.getInstance().font;
    }

    @Override
    public void render(GuiGraphics gfx, int x, int y, int width, int mouseX, int mouseY, float delta) {
        if (centered) {
            int tw = font.width(text);
            gfx.drawString(font, text, x + (width - tw) / 2, y + 4, color, false);
        } else {
            gfx.drawString(font, text, x + 8, y + 4, color, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public int getHeight() {
        return 16;
    }
}
