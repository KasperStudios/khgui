package kasperstudios.khgui.completion;

import kasperstudios.khgui.api.IHoverProvider.HoverInfo;
import kasperstudios.khgui.core.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Scrollable tooltip showing full object/method documentation.
 * Max visible height is capped so long descriptions never crowd the screen.
 * Scroll with mouse wheel when hovering over the tooltip.
 */
public class HoverTooltip {
    private HoverInfo info;
    private boolean visible = false;
    private int x, y;

    // Layout
    private static final int MAX_WIDTH     = 320;
    private static final int MAX_VISIBLE_LINES = 8;   // lines visible at once
    private static final int PADDING       = 8;
    private static final int LINE_MARGIN   = 3;

    // Scroll state
    private int scrollOffset = 0;      // first visible line index
    private List<LineEntry> renderedLines = new ArrayList<>();

    private final Font font;

    private record LineEntry(String text, boolean isTitle) {}

    public HoverTooltip() {
        this.font = Minecraft.getInstance().font;
    }

    /** Shows the tooltip at the given screen position. */
    public void show(HoverInfo info, int x, int y) {
        if (!info.equals(this.info)) {
            // New content — reset scroll
            this.scrollOffset = 0;
            this.info = info;
            rebuildLines();
        }
        this.visible = true;

        var mc = Minecraft.getInstance();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        int w = computeWidth();
        int h = computeHeight();

        if (x + w > screenW - 10) {
            x = Math.max(10, screenW - w - 10);
        }
        if (y + h > screenH - 10) {
            y = Math.max(10, y - h - 16);
        }

        this.x = x;
        this.y = y;
    }

    /** Hides the tooltip. */
    public void hide() {
        this.visible = false;
        this.info = null;
        this.scrollOffset = 0;
        this.renderedLines.clear();
    }

    public boolean isVisible() { return visible; }
    public HoverInfo getInfo() { return info; }

    /** Scrolls content up/down. delta > 0 = scroll down. */
    public void scroll(int delta) {
        int max = Math.max(0, renderedLines.size() - MAX_VISIBLE_LINES);
        scrollOffset = Math.max(0, Math.min(max, scrollOffset + delta));
    }

    /** Returns true if the given screen point is inside the tooltip bounds with a small margin. */
    public boolean contains(int mx, int my) {
        return contains(mx, my, 8);
    }

    public boolean contains(int mx, int my, int margin) {
        if (!visible) return false;
        int w = computeWidth();
        int h = computeHeight();
        return mx >= (x - margin) && mx < (x + w + margin) && my >= (y - margin) && my < (y + h + margin);
    }

    /** Renders the tooltip. */
    public void render(GuiGraphics graphics) {
        if (!visible || info == null || renderedLines.isEmpty()) return;

        var theme = ThemeManager.getCurrentTheme();
        Minecraft mc = Minecraft.getInstance();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int w = computeWidth();
        int lh = font.lineHeight + LINE_MARGIN;
        int visibleCount = Math.min(MAX_VISIBLE_LINES, renderedLines.size());
        int h = visibleCount * lh + PADDING * 2;

        // Keep inside screen
        int drawX = Math.min(x, screenW - w - 4);
        int drawY = (y + h > screenH) ? y - h - 4 : y;

        // Shadow
        graphics.fill(drawX + 3, drawY + 3, drawX + w + 3, drawY + h + 3, 0x55000000);

        // Background
        graphics.fill(drawX, drawY, drawX + w, drawY + h, theme.getSidebarBackground());

        // Top accent bar
        graphics.fill(drawX, drawY, drawX + w, drawY + 2, theme.getPrimaryAccent());

        // Borders (subtle)
        graphics.fill(drawX, drawY, drawX + 1,     drawY + h, theme.getBorder());
        graphics.fill(drawX + w - 1, drawY, drawX + w, drawY + h, theme.getBorder());
        graphics.fill(drawX, drawY + h - 1, drawX + w, drawY + h, theme.getBorder());

        // Lines
        int ty = drawY + PADDING;
        for (int i = scrollOffset; i < scrollOffset + visibleCount && i < renderedLines.size(); i++) {
            LineEntry entry = renderedLines.get(i);
            int color = entry.isTitle() ? theme.getPrimaryText() : theme.getSecondaryText();
            if (entry.text().isEmpty()) {
                ty += lh / 2;
                continue;
            }
            graphics.drawString(font, entry.text(), drawX + PADDING, ty, color, false);
            ty += lh;
        }

        // Scroll indicator
        boolean canScrollUp   = scrollOffset > 0;
        boolean canScrollDown = scrollOffset + visibleCount < renderedLines.size();
        if (canScrollUp || canScrollDown) {
            int indicatorX = drawX + w - PADDING - 4;
            if (canScrollUp)   graphics.drawString(font, "▲", indicatorX, drawY + PADDING,          theme.getMutedText(), false);
            if (canScrollDown) graphics.drawString(font, "▼", indicatorX, drawY + h - PADDING - 8,  theme.getMutedText(), false);

            // Scrollbar track
            int trackH = h - PADDING * 2 - 4;
            int barH   = Math.max(6, trackH * visibleCount / renderedLines.size());
            int barY   = drawY + PADDING + 2 + (scrollOffset * (trackH - barH) / Math.max(1, renderedLines.size() - visibleCount));
            graphics.fill(drawX + w - 3, drawY + PADDING + 2,        drawX + w - 1, drawY + PADDING + 2 + trackH, 0x33FFFFFF);
            graphics.fill(drawX + w - 3, barY, drawX + w - 1, barY + barH, theme.getPrimaryAccent());
        }
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private void rebuildLines() {
        renderedLines.clear();
        if (info == null) return;

        int wrapWidth = MAX_WIDTH - PADDING * 2;

        if (info.title != null) {
            for (String l : wrapText(info.title, wrapWidth)) {
                renderedLines.add(new LineEntry(l, true));
            }
        }
        if (info.description != null && !info.description.isBlank()) {
            renderedLines.add(new LineEntry("", false));  // separator
            // Split by explicit newlines first, then word-wrap each paragraph
            for (String para : info.description.split("\n")) {
                for (String l : wrapText(para.trim(), wrapWidth)) {
                    renderedLines.add(new LineEntry(l, false));
                }
            }
        }
    }

    private int computeWidth() {
        int w = 80;
        for (LineEntry e : renderedLines) w = Math.max(w, font.width(e.text()) + PADDING * 2 + 8);
        return Math.min(w, MAX_WIDTH);
    }

    private int computeHeight() {
        int lh = font.lineHeight + LINE_MARGIN;
        int visibleCount = Math.min(MAX_VISIBLE_LINES, renderedLines.size());
        return visibleCount * lh + PADDING * 2;
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) { lines.add(""); return lines; }

        String[] words = text.split(" ");
        StringBuilder cur = new StringBuilder();
        for (String word : words) {
            String test = cur.isEmpty() ? word : cur + " " + word;
            if (font.width(test) <= maxWidth) {
                if (!cur.isEmpty()) cur.append(' ');
                cur.append(word);
            } else {
                if (!cur.isEmpty()) lines.add(cur.toString());
                cur = new StringBuilder(word);
            }
        }
        if (!cur.isEmpty()) lines.add(cur.toString());
        return lines;
    }
}
