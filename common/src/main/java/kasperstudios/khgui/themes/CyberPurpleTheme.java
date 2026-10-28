package kasperstudios.khgui.themes;

import kasperstudios.khgui.api.ITheme;

/**
 * Cyber Purple theme - dark purple color scheme.
 */
public class CyberPurpleTheme implements ITheme {
    @Override public int getEditorBackground() { return 0xFF1A1625; }
    @Override public int getSidebarBackground() { return 0xFF1E1A2E; }
    @Override public int getActivityBarBackground() { return 0xFF16131E; }
    @Override public int getTitleBarBackground() { return 0xFF1E1A2E; }
    @Override public int getStatusBarBackground() { return 0xFF7B2CBF; }
    @Override public int getTabBarBackground() { return 0xFF1E1A2E; }
    
    @Override public int getPrimaryAccent() { return 0xFF9D4EDD; }
    @Override public int getSecondaryAccent() { return 0xFFC77DFF; }
    @Override public int getActiveAccent() { return 0xFFE0AAFF; }
    
    @Override public int getPrimaryText() { return 0xFFE0AAFF; }
    @Override public int getSecondaryText() { return 0xFFC77DFF; }
    @Override public int getMutedText() { return 0xFF9D4EDD; }
    @Override public int getInactiveText() { return 0xFF5A4570; }
    
    @Override public int getBorder() { return 0xFF3C2A4D; }
    @Override public int getSeparator() { return 0xFF2D2139; }
    @Override public int getHover() { return 0x339D4EDD; }
    @Override public int getActiveBackground() { return 0x559D4EDD; }
    @Override public int getSelection() { return 0xFF3C2A4D; }
    
    @Override public int getSyntaxKeyword() { return 0xFFE0AAFF; }
    @Override public int getSyntaxString() { return 0xFFC77DFF; }
    @Override public int getSyntaxNumber() { return 0xFFB794F6; }
    @Override public int getSyntaxComment() { return 0xFF7B2CBF; }
    @Override public int getSyntaxVariable() { return 0xFF9D4EDD; }
    @Override public int getSyntaxFunction() { return 0xFFE0AAFF; }
    
    @Override public int getLineNumber() { return 0xFF5A4570; }
    @Override public int getActiveLineNumber() { return 0xFF9D4EDD; }
    
    @Override public int getCursor() { return 0xFFE0AAFF; }
    
    @Override public int getGutterWidth() { return 8; }
    @Override public int getLineNumberWidth() { return 18; }
    @Override public int getCodeLeftPadding() { return 4; }
    
    @Override public String getName() { return "Cyber Purple"; }
}
