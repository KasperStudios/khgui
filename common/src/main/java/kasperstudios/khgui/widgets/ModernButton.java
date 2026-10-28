package kasperstudios.khgui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Modern styled button with hover animation, glow effect, and rounded corners.
 * Ported from old ModernButton with Mojang-mapped API.
 */
public class ModernButton extends Button {
    private final int baseColor;
    private boolean isHovered = false;
    private float hoverAnimation = 0f;

    public ModernButton(int x, int y, int width, int height, Component message, OnPress onPress, int color) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.baseColor = color;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        isHovered = isMouseOver(mouseX, mouseY);

        if (isHovered && hoverAnimation < 1f) {
            hoverAnimation = Math.min(1f, hoverAnimation + delta * 0.15f);
        } else if (!isHovered && hoverAnimation > 0f) {
            hoverAnimation = Math.max(0f, hoverAnimation - delta * 0.15f);
        }

        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        int bgColor = interpolateColor(baseColor, brighten(baseColor, 30), hoverAnimation);

        int cornerRadius = 4;

        // Main fills (pseudo rounded corners)
        graphics.fill(x + cornerRadius, y, x + w - cornerRadius, y + h, bgColor);
        graphics.fill(x, y + cornerRadius, x + w, y + h - cornerRadius, bgColor);

        // Corner fills
        graphics.fill(x + 1, y + 1, x + cornerRadius, y + cornerRadius, bgColor);
        graphics.fill(x + w - cornerRadius, y + 1, x + w - 1, y + cornerRadius, bgColor);
        graphics.fill(x + 1, y + h - cornerRadius, x + cornerRadius, y + h - 1, bgColor);
        graphics.fill(x + w - cornerRadius, y + h - cornerRadius, x + w - 1, y + h - 1, bgColor);

        // Glow effect on hover
        if (hoverAnimation > 0) {
            int glowAlpha = (int) (hoverAnimation * 40);
            int glowColor = (glowAlpha << 24) | (baseColor & 0x00FFFFFF);
            graphics.fill(x - 2, y - 2, x + w + 2, y + h + 2, glowColor);
        }

        // Border
        int borderColor = isHovered ? 0xFFFFFFFF : (baseColor | 0xFF000000);
        drawBorder(graphics, x, y, w, h, borderColor);

        // Text
        Font font = Minecraft.getInstance().font;
        int textColor = this.active ? 0xFFFFFFFF : 0xFF888888;
        int textX = x + (w - font.width(getMessage())) / 2;
        int textY = y + (h - 8) / 2;
        graphics.drawString(font, getMessage(), textX, textY, textColor, true);
    }

    private void drawBorder(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x + 2, y, x + w - 2, y + 1, color);
        graphics.fill(x + 2, y + h - 1, x + w - 2, y + h, color);
        graphics.fill(x, y + 2, x + 1, y + h - 2, color);
        graphics.fill(x + w - 1, y + 2, x + w, y + h - 2, color);
    }

    private int interpolateColor(int color1, int color2, float ratio) {
        int a1 = (color1 >> 24) & 0xFF, r1 = (color1 >> 16) & 0xFF, g1 = (color1 >> 8) & 0xFF, b1 = color1 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF, r2 = (color2 >> 16) & 0xFF, g2 = (color2 >> 8) & 0xFF, b2 = color2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * ratio);
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int brighten(int color, int amount) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, ((color >> 16) & 0xFF) + amount);
        int g = Math.min(255, ((color >> 8) & 0xFF) + amount);
        int b = Math.min(255, (color & 0xFF) + amount);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
