package kasperstudios.khgui.api;

/**
 * Theme interface for customizing GUI colors.
 * Implement this to create custom themes.
 */
public interface ITheme {
    // Background colors
    int getEditorBackground();
    int getSidebarBackground();
    int getActivityBarBackground();
    int getTitleBarBackground();
    int getStatusBarBackground();
    int getTabBarBackground();
    
    // Accent colors
    int getPrimaryAccent();
    int getSecondaryAccent();
    int getActiveAccent();
    
    // Text colors
    int getPrimaryText();
    int getSecondaryText();
    int getMutedText();
    int getInactiveText();
    
    // UI elements
    int getBorder();
    int getSeparator();
    int getHover();
    int getActiveBackground();
    int getSelection();
    
    // Syntax highlighting
    int getSyntaxKeyword();
    int getSyntaxString();
    int getSyntaxNumber();
    int getSyntaxComment();
    int getSyntaxVariable();
    int getSyntaxFunction();
    
    // Line numbers
    int getLineNumber();
    int getActiveLineNumber();
    
    // Cursor
    int getCursor();

    // Line numbers and Gutter sizing
    default int getGutterWidth() { return 12; }
    default int getLineNumberWidth() { return 32; }
    default int getCodeLeftPadding() { return 2; }
    
    // UI Symbols and Icons
    default String getCloseTabSymbol() { return "x"; }
    default net.minecraft.resources.ResourceLocation getCloseTabIcon() { return null; }

    // Theme name
    String getName();
}
