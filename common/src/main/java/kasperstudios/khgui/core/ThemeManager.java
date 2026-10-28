package kasperstudios.khgui.core;

import kasperstudios.khgui.api.ITheme;
import kasperstudios.khgui.themes.CyberPurpleTheme;
import kasperstudios.khgui.themes.DarkTheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages themes for the GUI.
 * Allows registering custom themes and switching between them.
 */
public class ThemeManager {
    private static final List<ITheme> themes = new ArrayList<>();
    private static int currentIndex = 0;
    
    static {
        // Register default themes - Cyber Purple first (default)
        themes.add(new CyberPurpleTheme());
        themes.add(new DarkTheme());
    }
    
    /**
     * Registers a custom theme.
     */
    public static void registerTheme(ITheme theme) {
        themes.add(theme);
    }
    
    /**
     * Sets the active theme by name.
     */
    public static void setTheme(String themeName) {
        for (int i = 0; i < themes.size(); i++) {
            if (themes.get(i).getName().equals(themeName)) {
                currentIndex = i;
                break;
            }
        }
    }
    
    /**
     * Gets the current theme.
     */
    public static ITheme getCurrentTheme() {
        return themes.get(currentIndex);
    }
    
    /**
     * Cycles to the next theme.
     */
    public static ITheme nextTheme() {
        currentIndex = (currentIndex + 1) % themes.size();
        return getCurrentTheme();
    }
    
    /**
     * Cycles to the previous theme.
     */
    public static ITheme previousTheme() {
        currentIndex = (currentIndex - 1 + themes.size()) % themes.size();
        return getCurrentTheme();
    }
    
    /**
     * Gets all registered themes.
     */
    public static List<ITheme> getAllThemes() {
        return new ArrayList<>(themes);
    }
}
