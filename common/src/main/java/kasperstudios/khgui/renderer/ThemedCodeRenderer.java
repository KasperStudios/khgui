package kasperstudios.khgui.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import kasperstudios.khgui.editor.CodeDocument;
import kasperstudios.khgui.editor.SyntaxHighlighter;
import kasperstudios.khgui.core.ThemeManager;
import kasperstudios.khgui.api.ITheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Themed code renderer with syntax highlighting, selection, cursor,
 * breakpoints, and execution line highlighting.
 */
public class ThemedCodeRenderer {
    private final CodeDocument document;
    private final Font font;

    public int x, y, width, height;
    public int scrollY = 0;
    public int scrollX = 0;
    public int lineHeight = 10;

    // Breakpoints and debug markers
    private final Set<Integer> breakpoints = new HashSet<>();
    private int executionLine = -1;

    public ThemedCodeRenderer(CodeDocument document) {
        this.document = document;
        this.font = Minecraft.getInstance().font;
        this.lineHeight = font.lineHeight + 2;
    }

    public void setBounds(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    public void toggleBreakpoint(int line) {
        if (breakpoints.contains(line)) {
            breakpoints.remove(line);
        } else {
            breakpoints.add(line);
        }
    }

    public Set<Integer> getBreakpoints() {
        return breakpoints;
    }

    public void setExecutionLine(int line) {
        this.executionLine = line;
    }

    public int getExecutionLine() {
        return executionLine;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, kasperstudios.khgui.api.IEditorContext context) {
        clampScroll();
        graphics.enableScissor(x, y, x + width, y + height);
        try {
            ITheme theme = ThemeManager.getCurrentTheme();
            int lineNumberWidth = theme.getLineNumberWidth();
            int gutterWidth = theme.getGutterWidth();

            int startLine = scrollY / lineHeight;
            int endLine = Math.min(document.getLines().size(), startLine + (height / lineHeight) + 1);

            // Fetch markers from context
            int contextExecLine = context != null ? context.getExecutionLine() - 1 : -1;
            Set<Integer> contextErrors = context != null ? context.getErrorLines() : java.util.Collections.emptySet();
            Set<Integer> contextBreakpoints = context != null ? context.getBreakpoints() : breakpoints;

            // Render line-by-line highlights
            for (int i = startLine; i < endLine; i++) {
                int drawY = y + (i * lineHeight) - scrollY;

                // Current line highlight
                if (i == document.cursorLine) {
                    graphics.fill(x, drawY, x + width, drawY + lineHeight,
                            adjustBrightness(theme.getEditorBackground(), 8));
                }

                // Execution line highlight (purple tint)
                if (i == contextExecLine) {
                    graphics.fill(x, drawY, x + width, drawY + lineHeight, 0x33AA00FF);
                }

                // Gutter markers
                int symbolX = x + 4;
                int centeredY = drawY + (lineHeight - font.lineHeight) / 2;

                if (contextBreakpoints.contains(i + 1)) {
                    graphics.drawString(font, "●", symbolX, centeredY, 0xFFFF4444, false);
                }
                
                if (i == contextExecLine) {
                    graphics.drawString(font, "➜", symbolX, centeredY, 0xFFAA00FF, false);
                }

                if (contextErrors.contains(i + 1)) {
                    graphics.drawString(font, "⚠", symbolX + 2, centeredY, 0xFFFFAA00, false);
                }

                // Line number
                String lineNum = String.valueOf(i + 1);
                int numColor = (i == document.cursorLine) ? theme.getActiveLineNumber() : theme.getLineNumber();
                int numWidth = font.width(lineNum);
                graphics.drawString(font, lineNum, x + gutterWidth + lineNumberWidth - numWidth - 4, drawY, numColor,
                        false);
            }

            // Selection must be rendered BEFORE text to not obscure it
            renderSelection(graphics, gutterWidth + lineNumberWidth, theme);

            // Render code lines with syntax highlighting
            for (int i = startLine; i < endLine; i++) {
                String line = document.getLines().get(i).replace("\r", "");
                int drawY = y + (i * lineHeight) - scrollY;
                int codeX = x + gutterWidth + lineNumberWidth + theme.getCodeLeftPadding() - scrollX;

                List<SyntaxHighlighter.ColoredSegment> segments = SyntaxHighlighter.highlight(line, theme);
                int cx = codeX;
                for (SyntaxHighlighter.ColoredSegment seg : segments) {
                    graphics.drawString(font, seg.text(), cx, drawY, seg.color(), false);
                    cx += font.width(seg.text());
                }
            }

            renderCursor(graphics, gutterWidth + lineNumberWidth, theme);
        } finally {
            graphics.disableScissor();
        }
    }

    private void renderSelection(GuiGraphics graphics, int lineNumberWidth, ITheme theme) {
        if (!document.hasSelection)
            return;

        int startL = document.selectionStartLine;
        int startC = document.selectionStartColumn;
        int endL = document.cursorLine;
        int endC = document.cursorColumn;

        if (startL > endL || (startL == endL && startC > endC)) {
            startL = endL;
            startC = endC;
            endL = document.selectionStartLine;
            endC = document.selectionStartColumn;
        }

        int startY = y + (startL * lineHeight) - scrollY;
        int endY = y + (endL * lineHeight) - scrollY;

        if (endY < y || startY > y + height)
            return;

        int selectionColor = (theme.getSelection() & 0x00FFFFFF) | 0x88000000;

        for (int i = startL; i <= endL; i++) {
            if (i >= document.getLines().size())
                break;

            String line = document.getLines().get(i);
            int lineY = y + (i * lineHeight) - scrollY;

            if (lineY + lineHeight < y || lineY > y + height)
                continue;

            int drawStartC = (i == startL) ? startC : 0;
            int drawEndC = (i == endL) ? endC : line.length() + 1;

            if (drawStartC >= drawEndC && i == endL)
                continue;

            String sanitized = line.replace("\r", "");
            int safeDrawStart = Math.min(drawStartC, sanitized.length());
            int safeDrawEnd = Math.min(drawEndC, sanitized.length());

            String beforeStr = sanitized.substring(0, safeDrawStart);
            String selStr = sanitized.substring(safeDrawStart, safeDrawEnd);

            int selStartX = x + lineNumberWidth + theme.getCodeLeftPadding() + font.width(beforeStr) - scrollX;
            int selWidth = font.width(selStr);

            if (i < endL && drawEndC > sanitized.length()) {
                selWidth += font.width(" ");
            }

            graphics.fill(selStartX, lineY, selStartX + selWidth, lineY + lineHeight, selectionColor);
        }
    }

    private void renderCursor(GuiGraphics graphics, int lineNumberWidth, ITheme theme) {
        try {
            int cursorY = (document.cursorLine * lineHeight) - scrollY;
            if (cursorY >= 0 && cursorY < height) {
                int lineIndex = Math.max(0, Math.min(document.getLines().size() - 1, document.cursorLine));
                String lineObj = document.getLines().get(lineIndex);

                String sanitized = lineObj.replace("\r", "");
                int safeColumn = Math.max(0, Math.min(sanitized.length(), document.cursorColumn));
                String beforeCursor = sanitized.substring(0, safeColumn);
                int cursorX = font.width(beforeCursor) - scrollX;

                if (System.currentTimeMillis() / 500 % 2 == 0) {
                    graphics.fill(x + lineNumberWidth + theme.getCodeLeftPadding() + cursorX, y + cursorY - 1,
                            x + lineNumberWidth + theme.getCodeLeftPadding() + cursorX + 1, y + cursorY + lineHeight - 1,
                            theme.getCursor());
                }
            }
        } catch (Exception e) {
            // Fail silently for rendering glitches
        }
    }

    public int getMaxScrollY() {
        return Math.max(0, (document.getLines().size() * lineHeight) - (height / 2));
    }

    public int getMaxScrollX() {
        int maxW = 0;
        for (String l : document.getLines()) {
            maxW = Math.max(maxW, font.width(l.replace("\r", "")));
        }
        ITheme theme = ThemeManager.getCurrentTheme();
        int codeAreaW = width - (theme.getGutterWidth() + theme.getLineNumberWidth() + theme.getCodeLeftPadding());
        return Math.max(0, maxW - codeAreaW + 40);
    }

    public void clampScroll() {
        scrollY = Math.max(0, Math.min(getMaxScrollY(), scrollY));
        scrollX = Math.max(0, Math.min(getMaxScrollX(), scrollX));
    }

    public void ensureCursorVisible() {
        int cursorTopY = document.cursorLine * lineHeight;
        int cursorBottomY = cursorTopY + lineHeight;

        if (cursorTopY < scrollY) {
            scrollY = cursorTopY;
        } else if (cursorBottomY > scrollY + height) {
            scrollY = cursorBottomY - height;
        }

        int safeLine = Math.max(0, Math.min(document.getLines().size() - 1, document.cursorLine));
        String line = document.getLines().get(safeLine).replace("\r", "");
        int safeCol = Math.max(0, Math.min(line.length(), document.cursorColumn));
        int cursorPixelX = font.width(line.substring(0, safeCol));
        
        ITheme theme = ThemeManager.getCurrentTheme();
        int codeAreaW = width - (theme.getGutterWidth() + theme.getLineNumberWidth() + theme.getCodeLeftPadding());

        if (cursorPixelX < scrollX) {
            scrollX = Math.max(0, cursorPixelX - 20);
        } else if (cursorPixelX > scrollX + codeAreaW - 30) {
            scrollX = cursorPixelX - codeAreaW + 40;
        }
        clampScroll();
    }

    /**
     * Checks if click was inside gutter to toggle breakpoints.
     * @return true if gutter click was handled
     */
    public boolean handleGutterClick(double mouseX, double mouseY) {
        if (mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + height) {
            return false;
        }
        ITheme theme = ThemeManager.getCurrentTheme();
        int gutterWidth = theme.getGutterWidth();
        if (mouseX < x + gutterWidth) {
            int clickedLine = ((int) mouseY - y + scrollY) / lineHeight;
            clickedLine = Math.max(0, Math.min(document.getLines().size() - 1, clickedLine));
            toggleBreakpoint(clickedLine);
            return true;
        }
        return false;
    }

    /**
     * Handles mouse click to position cursor.
     * 
     * @return true if click was handled
     */
    public boolean handleClick(double mouseX, double mouseY) {
        if (mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + height) {
            return false;
        }

        ITheme theme = ThemeManager.getCurrentTheme();
        int gutterWidth = theme.getGutterWidth();
        int lineNumberWidth = theme.getLineNumberWidth();
        int codeX = x + gutterWidth + lineNumberWidth + theme.getCodeLeftPadding();

        // Check if click is in gutter (for breakpoints)
        if (mouseX < x + gutterWidth) {
            int clickedLine = ((int) mouseY - y + scrollY) / lineHeight;
            clickedLine = Math.max(0, Math.min(document.getLines().size() - 1, clickedLine));
            toggleBreakpoint(clickedLine);
            return true;
        }

        // Calculate line
        int clickedLine = ((int) mouseY - y + scrollY) / lineHeight;
        clickedLine = Math.max(0, Math.min(document.getLines().size() - 1, clickedLine));

        // Calculate column
        String line = document.getLines().get(clickedLine).replace("\r", "");
        int clickedColumn = 0;
        int clickX = (int) mouseX - codeX + scrollX;

        for (int i = 0; i <= line.length(); i++) {
            String substr = line.substring(0, i);
            int textWidth = font.width(substr);

            if (textWidth > clickX) {
                clickedColumn = Math.max(0, i - 1);
                break;
            }
            clickedColumn = i;
        }

        document.cursorLine = clickedLine;
        document.cursorColumn = clickedColumn;
        document.hasSelection = false;

        return true;
    }

    private int adjustBrightness(int color, int amount) {
        int a = (color >> 24) & 0xFF;
        int r = Math.max(0, Math.min(255, ((color >> 16) & 0xFF) + amount));
        int g = Math.max(0, Math.min(255, ((color >> 8) & 0xFF) + amount));
        int b = Math.max(0, Math.min(255, (color & 0xFF) + amount));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
