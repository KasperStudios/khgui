package kasperstudios.khgui.core.themes;

import kasperstudios.khgui.api.ITheme;

/**
 * Cyber-Purple Theme - Based on KasHub Catppuccin Mocha
 * Soft purple accents with dark backgrounds
 */
public class CyberPurpleTheme implements ITheme {
    
    @Override
    public String getName() {
        return "Cyber Purple";
    }
    
    // Backgrounds - Catppuccin Mocha inspired
    @Override
    public int getEditorBackground() {
        return 0xFF1E1E2E; // Catppuccin base
    }
    
    @Override
    public int getSidebarBackground() {
        return 0xFF181825; // Catppuccin mantle
    }
    
    @Override
    public int getActivityBarBackground() {
        return 0xFF11111B; // Catppuccin crust
    }
    
    @Override
    public int getTitleBarBackground() {
        return 0xFF11111B; // Catppuccin crust
    }
    
    @Override
    public int getStatusBarBackground() {
        return 0xFF11111B; // Catppuccin crust
    }
    
    @Override
    public int getTabBarBackground() {
        return 0xFF181825; // Catppuccin mantle
    }
    
    // Accents - Soft purple
    @Override
    public int getPrimaryAccent() {
        return 0xFFCBA6F7; // Catppuccin mauve
    }
    
    @Override
    public int getSecondaryAccent() {
        return 0xFF89B4FA; // Catppuccin blue
    }
    
    @Override
    public int getActiveAccent() {
        return 0xFFDDBDFF; // Lighter mauve
    }
    
    // Text colors
    @Override
    public int getPrimaryText() {
        return 0xFFCDD6F4; // Catppuccin text
    }
    
    @Override
    public int getSecondaryText() {
        return 0xFFBAC2DE; // Catppuccin subtext1
    }
    
    @Override
    public int getMutedText() {
        return 0xFF6C7086; // Catppuccin overlay0
    }
    
    @Override
    public int getInactiveText() {
        return 0xFF585B70; // Catppuccin surface2
    }
    
    // UI elements
    @Override
    public int getBorder() {
        return 0xFF45475A; // Catppuccin surface1
    }
    
    @Override
    public int getSeparator() {
        return 0xFF313244; // Catppuccin surface0
    }
    
    @Override
    public int getHover() {
        return 0x1AFFFFFF; // Subtle white overlay
    }
    
    @Override
    public int getActiveBackground() {
        return 0xFF45475A; // Catppuccin surface1
    }
    
    @Override
    public int getSelection() {
        return 0x4489B4FA; // Catppuccin blue with alpha
    }
    
    // Syntax highlighting - Catppuccin colors
    @Override
    public int getSyntaxKeyword() {
        return 0xFFCBA6F7; // Mauve for keywords
    }
    
    @Override
    public int getSyntaxString() {
        return 0xFFA6E3A1; // Green for strings
    }
    
    @Override
    public int getSyntaxNumber() {
        return 0xFFFAB387; // Peach for numbers
    }
    
    @Override
    public int getSyntaxComment() {
        return 0xFF6C7086; // Overlay0 for comments
    }
    
    @Override
    public int getSyntaxVariable() {
        return 0xFFF9E2AF; // Yellow for variables
    }
    
    @Override
    public int getSyntaxFunction() {
        return 0xFF89B4FA; // Blue for functions
    }
    
    // Line numbers
    @Override
    public int getLineNumber() {
        return 0xFF6C7086; // Catppuccin overlay0
    }
    
    @Override
    public int getActiveLineNumber() {
        return 0xFFCDD6F4; // Catppuccin text
    }
    
    // Cursor
    @Override
    public int getCursor() {
        return 0xFFF5E0DC; // Catppuccin rosewater
    }
}
