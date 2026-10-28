package kasperstudios.khgui.panels;

import kasperstudios.khgui.api.IConsoleProvider;
import kasperstudios.khgui.api.ITheme;
import kasperstudios.khgui.core.GuiLayout;
import kasperstudios.khgui.core.ThemeManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class ConsolePanel {
    private final GuiLayout layout;
    private IConsoleProvider provider;
    private Runnable onClose;
    
    private int activeTabIndex = 0;
    private final String[] tabs = {"TERMINAL", "OUTPUT", "PROBLEMS"};
    private int scrollY = 0;
    private boolean autoScroll = true;
    private String terminalInput = "";
    private int cursorPosition = 0;
    private final List<String> commandHistory = new java.util.ArrayList<>();
    private int historyIndex = -1;
    private boolean focused = false;

    public ConsolePanel(GuiLayout layout) {
        this.layout = layout;
    }

    public void setProvider(IConsoleProvider provider) {
        this.provider = provider;
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!layout.isConsoleVisible()) return;

        ITheme theme = ThemeManager.getCurrentTheme();
        Font font = Minecraft.getInstance().font;
        GuiLayout.LayoutRect area = layout.consoleArea;

        // Background
        graphics.fill(area.x, area.y, area.x + area.w, area.y + area.h, theme.getSidebarBackground());
        
        // Top border
        graphics.fill(area.x, area.y, area.x + area.w, area.y + 1, theme.getSelection());

        // Render Tabs
        int tabX = area.x + 20;
        int tabY = area.y + 10;
        for (int i = 0; i < tabs.length; i++) {
            String tab = tabs[i];
            int textWidth = font.width(tab);
            int color = (i == activeTabIndex) ? theme.getPrimaryText() : theme.getSecondaryText();
            
            graphics.drawString(font, tab, tabX, tabY, color, false);
            
            // Active tab indicator (small line under text)
            if (i == activeTabIndex) {
                graphics.fill(tabX, tabY + 12, tabX + textWidth, tabY + 14, theme.getPrimaryAccent());
            }
            
            tabX += textWidth + 25;
        }

        // Close button (X) on the top right
        int closeX = area.x + area.w - 20;
        int closeY = area.y + 10;
        int closeColor = (mouseX >= closeX && mouseX < closeX + 10 && mouseY >= closeY && mouseY < closeY + 10) 
                ? theme.getPrimaryText() : theme.getSecondaryText();
        graphics.drawString(font, "X", closeX, closeY, closeColor, false);

        // Render Content based on active tab
        renderContent(graphics, font, theme, area.x + 20, area.y + 35, area.w - 40, area.h - 55);
    }

    private void renderContent(GuiGraphics graphics, Font font, ITheme theme, int x, int y, int w, int h) {
        if (provider == null) {
            graphics.drawString(font, "No console provider attached.", x, y, theme.getMutedText(), false);
            return;
        }

        String activeTab = tabs[activeTabIndex];
        int lineHeight = font.lineHeight + 2;

        if (activeTab.equals("TERMINAL")) {
            List<String> lines = new java.util.ArrayList<>(provider.getTerminalLines());
            String prompt = provider.getTerminalPrompt();
            
            // Append current input with prompt as the last line
            String currentLine = prompt + terminalInput;
            lines.add(currentLine);
            
            // Handle auto-scroll logic
            int linesHeight = lines.size() * lineHeight;
            int viewHeight = h - 10;
            int maxScroll = Math.max(0, linesHeight - viewHeight);

            if (autoScroll) {
                scrollY = maxScroll;
            } else if (scrollY >= maxScroll) {
                autoScroll = true;
            }

            renderTextList(graphics, font, theme, lines, x, y, w, viewHeight);
            
            // Render Blinking cursor in the scrollable stream
            int lastLineY = y + (lines.size() - 1) * lineHeight - scrollY;
            if (lastLineY + lineHeight > y && lastLineY < y + viewHeight) {
                // Highlight input line slightly
                graphics.fill(x - 5, lastLineY - 1, x + w + 5, lastLineY + lineHeight - 1, 0x11FFFFFF);
                
                if (focused && (System.currentTimeMillis() / 500) % 2 == 0) {
                    int safeCursor = Math.max(0, Math.min(terminalInput.length(), cursorPosition));
                    int cursorX = x + font.width(prompt + terminalInput.substring(0, safeCursor));
                    graphics.fill(cursorX, lastLineY, cursorX + 2, lastLineY + font.lineHeight, theme.getPrimaryAccent());
                }
            }
        } else if (activeTab.equals("OUTPUT")) {
            List<String> lines = provider.getOutputLines();
            renderTextList(graphics, font, theme, lines, x, y, w, h);
        } else if (activeTab.equals("PROBLEMS")) {
            List<IConsoleProvider.ProblemEntry> problems = provider.getProblems();
            if (problems.isEmpty()) {
                graphics.drawString(font, "No problems have been detected in the workspace.", x, y, theme.getSecondaryText(), false);
            } else {
                int currentY = y - scrollY;
                for (IConsoleProvider.ProblemEntry problem : problems) {
                    if (currentY + lineHeight > y && currentY < y + h) {
                        String icon = problem.severity == IConsoleProvider.ProblemEntry.Severity.ERROR ? "!" : "i";
                        int color = problem.severity == IConsoleProvider.ProblemEntry.Severity.ERROR ? theme.getPrimaryAccent() : theme.getSecondaryText();
                        graphics.drawString(font, icon + " " + problem.message + " [" + problem.file + ":" + problem.line + "]", x, currentY, color, false);
                    }
                    currentY += lineHeight;
                }
            }
        }
    }

    private void renderTextList(GuiGraphics graphics, Font font, ITheme theme, List<String> lines, int x, int y, int w, int h) {
        if (lines.isEmpty()) return;

        int lineHeight = font.lineHeight + 2;
        int maxScroll = Math.max(0, lines.size() * lineHeight - h);
        
        if (scrollY > maxScroll) {
            scrollY = maxScroll;
        }

        graphics.enableScissor(x, y, x + w, y + h);
        try {
            int currentY = y - scrollY;
            for (String line : lines) {
                if (currentY + lineHeight > y && currentY < y + h) {
                    graphics.drawString(font, line.replace("\r", ""), x, currentY, theme.getSecondaryText(), false);
                }
                currentY += lineHeight;
            }
        } finally {
            graphics.disableScissor();
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!layout.isConsoleVisible()) return false;
        GuiLayout.LayoutRect area = layout.consoleArea;

        // Check tabs
        int tabX = area.x + 20;
        int tabY = area.y + 10;
        Font font = Minecraft.getInstance().font;
        for (int i = 0; i < tabs.length; i++) {
            int textWidth = font.width(tabs[i]);
            if (mouseX >= tabX && mouseX < tabX + textWidth && mouseY >= tabY && mouseY < tabY + 12) {
                activeTabIndex = i;
                scrollY = 0; // Reset scroll on tab switch
                return true;
            }
            tabX += textWidth + 25;
        }

        // Check close button
        int closeX = area.x + area.w - 20;
        int closeY = area.y + 10;
        if (mouseX >= closeX && mouseX < closeX + 10 && mouseY >= closeY && mouseY < closeY + 10) {
            layout.toggleConsole();
            if (onClose != null) onClose.run();
            return true;
        }

        if (area.contains(mouseX, mouseY)) {
            focused = true;
            return true; // Consume click inside console
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollYEvent) {
        if (!layout.isConsoleVisible() || !layout.consoleArea.contains(mouseX, mouseY)) return false;
        
        // Basic scrolling
        scrollY = Math.max(0, scrollY - (int)(scrollYEvent * 20)); 
        
        // Disable auto-scroll if user scrolls up manually
        if (scrollYEvent > 0) {
            autoScroll = false;
        }
        
        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!layout.isConsoleVisible() || !focused) return false;
        
        String activeTab = tabs[activeTabIndex];
        if (activeTab.equals("TERMINAL")) {
            if (keyCode == 257 || keyCode == 335) { // Enter or Keypad Enter
                if (provider != null && !terminalInput.isEmpty()) {
                    provider.submitTerminalCommand(terminalInput);
                    commandHistory.add(terminalInput);
                    if (commandHistory.size() > 50) commandHistory.remove(0);
                    historyIndex = -1;
                    autoScroll = true; // Auto-scroll on command submit
                }
                terminalInput = "";
                cursorPosition = 0;
                return true;
            } else if (keyCode == 265) { // Up Arrow
                if (!commandHistory.isEmpty()) {
                    if (historyIndex == -1) historyIndex = commandHistory.size() - 1;
                    else if (historyIndex > 0) historyIndex--;
                    terminalInput = commandHistory.get(historyIndex);
                    cursorPosition = terminalInput.length();
                }
                return true;
            } else if (keyCode == 264) { // Down Arrow
                if (historyIndex != -1) {
                    if (historyIndex < commandHistory.size() - 1) {
                        historyIndex++;
                        terminalInput = commandHistory.get(historyIndex);
                    } else {
                        historyIndex = -1;
                        terminalInput = "";
                    }
                    cursorPosition = terminalInput.length();
                }
                return true;
            } else if (keyCode == 263) { // Left Arrow
                cursorPosition = Math.max(0, cursorPosition - 1);
                return true;
            } else if (keyCode == 262) { // Right Arrow
                cursorPosition = Math.min(terminalInput.length(), cursorPosition + 1);
                return true;
            } else if (keyCode == 268) { // Home
                cursorPosition = 0;
                return true;
            } else if (keyCode == 269) { // End
                cursorPosition = terminalInput.length();
                return true;
            } else if (keyCode == 259) { // Backspace
                if (cursorPosition > 0 && !terminalInput.isEmpty()) {
                    terminalInput = terminalInput.substring(0, cursorPosition - 1) + terminalInput.substring(cursorPosition);
                    cursorPosition--;
                }
                return true;
            } else if (keyCode == 261) { // Delete
                if (cursorPosition < terminalInput.length()) {
                    terminalInput = terminalInput.substring(0, cursorPosition) + terminalInput.substring(cursorPosition + 1);
                }
                return true;
            } else if (keyCode == 86 && (modifiers & 2) != 0) { // Ctrl + V
                String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
                if (clipboard != null) {
                    String sanitized = clipboard.replace("\r", "");
                    cursorPosition = Math.max(0, Math.min(terminalInput.length(), cursorPosition));
                    terminalInput = terminalInput.substring(0, cursorPosition) + sanitized + terminalInput.substring(cursorPosition);
                    cursorPosition += sanitized.length();
                }
                return true;
            }
            return true; // Consume any other key when focused on terminal
        } else {
            // For other tabs (PROBLEMS, OUTPUT, etc.), consume arrow keys for scrolling
            if (keyCode == 265) { // Up
                scrollY = Math.max(0, scrollY - 14);
                return true;
            } else if (keyCode == 264) { // Down
                scrollY += 14;
                return true;
            }
            // Consume key so it doesn't bleed into the code editor while focused on console
            return true;
        }
    }

    public boolean charTyped(char character, int modifiers) {
        if (!layout.isConsoleVisible() || !focused) return false;
        
        String activeTab = tabs[activeTabIndex];
        if (activeTab.equals("TERMINAL")) {
            // Filter common control characters if needed
            if (character >= 32 && character != 127) {
                cursorPosition = Math.max(0, Math.min(terminalInput.length(), cursorPosition));
                terminalInput = terminalInput.substring(0, cursorPosition) + character + terminalInput.substring(cursorPosition);
                cursorPosition++;
                return true;
            }
        }
        // Consume character if console is focused so it doesn't type into code behind
        return true;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean isFocused() {
        return focused;
    }
}
