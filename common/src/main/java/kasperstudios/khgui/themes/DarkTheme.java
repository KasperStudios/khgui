package kasperstudios.khgui.themes;

import kasperstudios.khgui.api.ITheme;

/**
 * Classic VS Code Dark theme.
 */
public class DarkTheme implements ITheme {
    @Override public int getEditorBackground() { return 0xFF1E1E1E; }
    @Override public int getSidebarBackground() { return 0xFF252526; }
    @Override public int getActivityBarBackground() { return 0xFF333333; }
    @Override public int getTitleBarBackground() { return 0xFF3C3C3C; }
    @Override public int getStatusBarBackground() { return 0xFF007ACC; }
    @Override public int getTabBarBackground() { return 0xFF2D2D2D; }
    
    @Override public int getPrimaryAccent() { return 0xFF007ACC; }
    @Override public int getSecondaryAccent() { return 0xFF0098FF; }
    @Override public int getActiveAccent() { return 0xFF00BCF2; }
    
    @Override public int getPrimaryText() { return 0xFFD4D4D4; }
    @Override public int getSecondaryText() { return 0xFFCCCCCC; }
    @Override public int getMutedText() { return 0xFF888888; }
    @Override public int getInactiveText() { return 0xFF6A6A6A; }
    
    @Override public int getBorder() { return 0xFF3C3C3C; }
    @Override public int getSeparator() { return 0xFF2D2D2D; }
    @Override public int getHover() { return 0x22FFFFFF; }
    @Override public int getActiveBackground() { return 0x33FFFFFF; }
    @Override public int getSelection() { return 0xFF264F78; }
    
    @Override public int getSyntaxKeyword() { return 0xFF569CD6; }
    @Override public int getSyntaxString() { return 0xFFCE9178; }
    @Override public int getSyntaxNumber() { return 0xFFB5CEA8; }
    @Override public int getSyntaxComment() { return 0xFF6A9955; }
    @Override public int getSyntaxVariable() { return 0xFF9CDCFE; }
    @Override public int getSyntaxFunction() { return 0xFFDCDCAA; }
    
    @Override public int getLineNumber() { return 0xFF858585; }
    @Override public int getActiveLineNumber() { return 0xFFC6C6C6; }
    
    @Override public int getCursor() { return 0xFFAEAFAD; }
    
    @Override public int getGutterWidth() { return 8; }
    @Override public int getLineNumberWidth() { return 18; }
    @Override public int getCodeLeftPadding() { return 4; }
    
    @Override public String getName() { return "Dark"; }
}
